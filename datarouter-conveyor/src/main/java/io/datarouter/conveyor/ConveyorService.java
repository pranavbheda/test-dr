/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.conveyor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.config.DatarouterConveyorTraceSettings;
import io.datarouter.conveyor.exception.ConveyorExceptionCategory;
import io.datarouter.conveyor.trace.ConveyorTraceBuffer;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.instrumentation.trace.ConveyorTraceAndTaskExecutorBundleDto;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.instrumentation.trace.Trace2SpanDto;
import io.datarouter.instrumentation.trace.Trace2ThreadDto;
import io.datarouter.instrumentation.trace.TraceSaveReasonType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;
import io.datarouter.util.PlatformMxBeans;
import io.datarouter.util.tracer.DatarouterTracer;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.ExceptionTool;

@Singleton
public class ConveyorService{
	private static final Logger logger = LoggerFactory.getLogger(ConveyorService.class);

	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private ServerName serverName;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ConveyorTraceBuffer traceBuffer;
	@Inject
	private DatarouterConveyorTraceSettings traceSettings;

	public void run(ConveyorConfiguration configuration, ConveyorRunnable conveyor){
		if(!shouldRun(conveyor)){
			return;
		}

		int iteration = 0;
		long start = System.currentTimeMillis();
		Optional<ExceptionRecordDto> exceptionRecord = Optional.empty();
		try{
			while(shouldRun(conveyor)){
				iteration++;
				Long traceCreated = Trace2Dto.getCurrentTimeInNs();
				var traceContext = new W3TraceContext(Trace2Dto.getCurrentTimeInNs());
				Tracer tracer = new DatarouterTracer(serverName.get(), null, traceContext);
				boolean saveCpuTime = traceSettings.saveTraceCpuTime.get();
				tracer.setSaveThreadCpuTime(saveCpuTime);
				TracerThreadLocal.bindToThread(tracer);
				tracer.createAndStartThread(conveyor.getName() + " conveyor process", Trace2Dto.getCurrentTimeInNs());
				Long mainThreadCpuTimeBegin = saveCpuTime ? PlatformMxBeans.THREAD.getCurrentThreadCpuTime() : null;

				boolean finishedOneProcess = false;
				boolean errored = false;
				try{
					ProcessResult result = configuration.process(conveyor);
					ConveyorCounters.incProcessBatch(conveyor);
					if(!result.shouldContinueImmediately()){
						break; // usually when there's no message in the queue
					}
					finishedOneProcess = true;
				}catch(Throwable e){
					errored = true;
					boolean interrupted = ExceptionTool.isInterrupted(e);
					if(interrupted){
						ConveyorCounters.incInterrupted(conveyor);
						try{
							configuration.interrupted(conveyor);
						}catch(Exception ex){
							logger.error("interuption handling failed", ex);
						}
					}else{
						ConveyorCounters.incException(conveyor);
					}
					logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable "
							+ "interrupted={}" + (configuration.compactExceptionLogging().get() ? " {}" : ""),
							interrupted, e);
					if(!interrupted){
						exceptionRecord = exceptionRecorder.tryRecordException(e, getClass().getName(),
								ConveyorExceptionCategory.CONVEYOR);
					}
				}finally{
					long traceEnded = Trace2Dto.getCurrentTimeInNs();
					Long mainThreadCpuTimeEnded = saveCpuTime ? PlatformMxBeans.THREAD.getCurrentThreadCpuTime() : null;
					Traceparent traceparent = tracer.getTraceContext().get().getTraceparent();
					Trace2ThreadDto rootThread = null;
					if(tracer.getCurrentThreadId() != null){
						rootThread = ((DatarouterTracer)tracer).getCurrentThread();
						((DatarouterTracer)tracer).setCurrentThread(null);
					}

					Optional<Long> totalCpuTimeNs = Optional.empty();
					if(saveCpuTime){
						Long totalCpuTimeFromOtherThreads = tracer.getThreadQueue().stream()
								.mapToLong(thread -> thread.getCpuTimeEndedNs() - thread.getCpuTimeCreatedNs())
								.sum();
						totalCpuTimeNs = Optional.of(mainThreadCpuTimeEnded - mainThreadCpuTimeBegin
								+ totalCpuTimeFromOtherThreads);
					}
					Optional<Long> totalCpuTimeMs = totalCpuTimeNs
							.map(TimeUnit.NANOSECONDS::toMillis);
					totalCpuTimeMs.ifPresent(totalTimeMs -> ConveyorCounters.incTotalCpuTime(conveyor, totalTimeMs));

					List<TraceSaveReasonType> saveReasons = new ArrayList<>();
					Trace2Dto trace2 = new Trace2Dto(
							traceparent,
							null,
							serviceName.get(),
							conveyor.getName(),
							null,
							traceCreated,
							traceEnded,
							serviceName.get(),
							tracer.getDiscardedThreadCount(),
							tracer.getThreadQueue().size(),
							mainThreadCpuTimeBegin,
							mainThreadCpuTimeEnded,
							0L,
							0L,
							saveReasons);
					Long traceDurationMs = trace2.getDurationInMs();
					if(traceSettings.saveTraces.get()){
						if(finishedOneProcess && traceDurationMs > traceSettings.saveTracesOverMs.get().toMillis()){
							saveReasons.add(TraceSaveReasonType.DURATION);
						}
						if(totalCpuTimeMs.orElse(-1L) > traceSettings.saveTracesCpuOverMs.get().toMillis()){
							saveReasons.add(TraceSaveReasonType.CPU);
						}
						if(errored){
							saveReasons.add(TraceSaveReasonType.ERROR);
						}
					}

					if(!saveReasons.isEmpty()){
						List<Trace2ThreadDto> threads = new ArrayList<>(tracer.getThreadQueue());
						List<Trace2SpanDto> spans = new ArrayList<>(tracer.getSpanQueue());
						if(rootThread != null){
							rootThread.setTotalSpanCount(spans.size());
							threads.add(rootThread); // force to save the rootThread even though the queue could be full
						}
						Optional<TaskExecutorRecordDto> executorRecord = exceptionRecord
								.map(ExceptionRecordDto::id)
								.map(id -> new TaskExecutorRecordDto(
										new Ulid().value(),
										traceparent.traceId,
										traceparent.parentId,
										id));
						var bundle = new ConveyorTraceAndTaskExecutorBundleDto(new Trace2BundleDto(trace2, threads,
								spans), executorRecord);
						traceBuffer.offer(bundle)
								.map(name -> "saved to " + name)
								.ifPresent(destination -> logger.warn("Trace {} for name={}."
										+ " traceparent={}"
										+ " durationMs={}"
										+ " totalCpuTimeMs={}"
										+ " saveReasons={}"
										+ " numThreads={}"
										+ " numSpans={}",
										destination,
										conveyor.getName(),
										traceparent,
										traceDurationMs,
										totalCpuTimeMs.orElse(null),
										saveReasons,
										threads.size(),
										spans.size()));

					}
				}

			}
			long duration = System.currentTimeMillis() - start;
			ConveyorCounters.incFinishDrain(conveyor);
			logger.info("drain finished for conveyor={} duration={} iterations={} ", conveyor.getName(), duration,
					iteration);
		}finally{
			TracerThreadLocal.clearFromThread();
		}
	}

	private static boolean shouldRun(ConveyorRunnable conveyor){
		return !Thread.currentThread().isInterrupted() && conveyor.shouldRun();
	}

}
