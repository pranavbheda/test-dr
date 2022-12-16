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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.scanner.Scanner;

@Singleton
public class CountMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(CountMemoryToPublisherConveyorConfiguration.class);
	private static final int POLL_LIMIT = 5;

	@Inject
	private CountBuffers buffers;
	@Inject
	private CountPublisher countPublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		//this normally runs more frequently than the publisher, but polling > 1 allows catching up just in case
		Instant beforePeek = Instant.now();
		List<Map<Long,Map<String,Long>>> dtos = buffers.countBuffer.pollMultiWithLimit(POLL_LIMIT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		if(dtos.isEmpty()){
			return new ProcessResult(false);
		}
		dtos.forEach(counts -> publishCounts(counts, conveyor));
		//process as many as possible if shutting down
		//or continue processing immediately if this batch was full
		return new ProcessResult(conveyor.isShuttingDown() || dtos.size() == POLL_LIMIT);
	}

	private void publishCounts(Map<Long,Map<String,Long>> counts, ConveyorRunnable conveyor){
		try{
			int numCounts = Scanner.of(counts.values())
					.map(Map::size)
					.reduce(0, Integer::sum);
			logger.info("counts numPeriods={}, numNames={}", counts.size(), numCounts);
			countPublisher.publish(counts);
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, numCounts);
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
		}
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

	@Override
	public long delaySeconds(){
		return 1L;
	}

}