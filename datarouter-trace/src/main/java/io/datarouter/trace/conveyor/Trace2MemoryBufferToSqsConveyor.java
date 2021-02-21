/**
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
package io.datarouter.trace.conveyor;

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.storage.BaseTraceQueueDao;
import io.datarouter.web.exception.ExceptionRecorder;

public class Trace2MemoryBufferToSqsConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(Trace2MemoryBufferToSqsConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final Supplier<Boolean> shouldBufferInSqs;
	private final BaseTraceQueueDao traceQueueDao;
	private final MemoryBuffer<Trace2BundleDto> buffer;
	private final Gson gson;

	public Trace2MemoryBufferToSqsConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			Supplier<Boolean> shouldBufferInSqs,
			MemoryBuffer<Trace2BundleDto> buffer,
			BaseTraceQueueDao traceQueueDao,
			Gson gson,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.shouldBufferInSqs = shouldBufferInSqs;
		this.traceQueueDao = traceQueueDao;
		this.buffer = buffer;
		this.gson = gson;
	}

	public void processTraceEntityDtos(List<Trace2BundleDto> dtos){
		if(shouldBufferInSqs.get()){
			Scanner.of(dtos).map(this::toMessage).flush(traceQueueDao::putMulti);
		}
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<Trace2BundleDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			processTraceEntityDtos(dtos);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
			return new ProcessBatchResult(true);
		}catch(RuntimeException putMultiException){
			List<Traceparent> ids = Scanner.of(dtos).map(dto -> dto.traceDto.traceparent).list();
			logger.warn("exception sending trace to sqs ids={}", ids, putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);//backoff for a bit
		}
	}

	protected ConveyorMessage toMessage(Trace2BundleDto dto){
		return new ConveyorMessage(dto.traceDto.traceparent.toString(), gson.toJson(dto));
	}

}