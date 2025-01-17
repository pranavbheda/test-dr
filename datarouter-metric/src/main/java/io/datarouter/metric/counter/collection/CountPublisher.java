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
package io.datarouter.metric.counter.collection;

import java.util.Map;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.counter.collection.DatarouterCountCollector.CountCollectorStats;

public interface CountPublisher{

	PublishingResponseDto publishStats(Map<Long,Map<String,CountCollectorStats>> counts);

	public static class NoOpCountPublisher implements CountPublisher{

		@Override
		public PublishingResponseDto publishStats(Map<Long,Map<String,CountCollectorStats>> counts){
			return PublishingResponseDto.NO_OP;
		}

	}

}
