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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.storage.setting.Setting;

public abstract class BaseTrace2FilterToMemoryBuffer{
	private static final Logger logger = LoggerFactory.getLogger(BaseTrace2FilterToMemoryBuffer.class);

	private static final int MAX_TRACES = 1_000;

	public final MemoryBuffer<Trace2BundleAndHttpRequestRecordDto> buffer;
	private final Setting<Boolean> shouldRunSetting;

	public BaseTrace2FilterToMemoryBuffer(String bufferName, Setting<Boolean> shouldRun){
		this.buffer = new MemoryBuffer<>(bufferName, MAX_TRACES);
		this.shouldRunSetting = shouldRun;
	}

	public Optional<String> offer(Trace2BundleAndHttpRequestRecordDto dto){
		return offerDtoToBuffer(dto);
	}

	private Optional<String> offerDtoToBuffer(Trace2BundleAndHttpRequestRecordDto dto){
		if(!shouldRunSetting.get()){
			return Optional.empty();
		}
		if(!buffer.offer(dto)){
			logger.warn("error offering trace entity buffer={} traceparent={}", buffer.getName(),
					dto.traceBundleDto.traceDto.traceparent);
			return Optional.empty();
		}
		return Optional.of(buffer.getName());
	}

}
