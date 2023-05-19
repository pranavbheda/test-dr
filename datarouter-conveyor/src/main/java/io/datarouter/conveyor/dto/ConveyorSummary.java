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
package io.datarouter.conveyor.dto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import io.datarouter.conveyor.ConveyorProcessor;
import io.datarouter.scanner.Scanner;

public record ConveyorSummary(
		String name,
		ThreadPoolExecutor executor,
		boolean shouldRun,
		int maxAllowedThreadCount){

	public static List<ConveyorSummary> summarize(Map<String,ConveyorProcessor> entries){
		return Scanner.of(entries.entrySet())
				.map(entry -> new ConveyorSummary(
						entry.getKey(),
						entry.getValue().getExecutorService(),
						entry.getValue().shouldConveyorRun(),
						entry.getValue().getMaxAllowedThreadCount()))
				.list();
	}

}
