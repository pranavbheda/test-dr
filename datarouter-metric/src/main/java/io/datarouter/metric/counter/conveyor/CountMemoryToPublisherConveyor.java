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
package io.datarouter.metric.counter.conveyor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class CountMemoryToPublisherConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(CountMemoryToPublisherConveyor.class);

	private static final int POLL_LIMIT = 5;

	private final MemoryBuffer<Map<Long,Map<String,Long>>> buffer;
	private final CountPublisher countPublisher;

	public CountMemoryToPublisherConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<Map<Long,Map<String,Long>>> buffer,
			ExceptionRecorder exceptionRecorder,
			CountPublisher countPublisher,
			ConveyorGauges conveyorGauges){
		super(name, shouldRun, () -> false, exceptionRecorder, conveyorGauges);
		this.buffer = buffer;
		this.countPublisher = countPublisher;
	}

	@Override
	public ProcessBatchResult processBatch(){
		//this normally runs more frequently than the publisher, but polling > 1 allows catching up just in case
		Instant beforePeek = Instant.now();
		List<Map<Long,Map<String,Long>>> dtos = buffer.pollMultiWithLimit(POLL_LIMIT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		dtos.forEach(this::publishCounts);
		//process as many as possible if shutting down
		//or continue processing immediately if this batch was full
		return new ProcessBatchResult(isShuttingDown() || dtos.size() == POLL_LIMIT);
	}

	private void publishCounts(Map<Long,Map<String,Long>> counts){
		try{
			int numCounts = Scanner.of(counts.values())
					.map(Map::size)
					.reduce(0, Integer::sum);
			logger.info("counts numPeriods={}, numNames={}", counts.size(), numCounts);
			countPublisher.publish(counts);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, numCounts);
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
		}
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}