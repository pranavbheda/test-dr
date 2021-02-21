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
package io.datarouter.trace.conveyor.local;

import java.util.List;

import com.google.gson.Gson;

import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.trace.conveyor.BaseTraceMemoryToSqsConveyor;
import io.datarouter.trace.storage.BaseDatarouterTraceDao;
import io.datarouter.trace.storage.BaseTraceQueueDao;
import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.trace.Trace;
import io.datarouter.web.exception.ExceptionRecorder;

public class TraceMemoryToSqsConveyor extends BaseTraceMemoryToSqsConveyor{

	private final Setting<Boolean> shouldBufferInSqs;
	private final BaseTraceQueueDao traceQueueDao;
	private final BaseDatarouterTraceDao traceDao;

	public TraceMemoryToSqsConveyor(
			String name,
			Setting<Boolean> shouldRunSetting,
			Setting<Boolean> shouldBufferInSqs,
			MemoryBuffer<TraceEntityDto> buffer,
			BaseTraceQueueDao traceQueueDao,
			BaseDatarouterTraceDao traceDao,
			Gson gson,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRunSetting, buffer, gson, exceptionRecorder);
		this.shouldBufferInSqs = shouldBufferInSqs;
		this.traceQueueDao = traceQueueDao;
		this.traceDao = traceDao;
	}

	@Override
	public void processTraceEntityDtos(List<TraceEntityDto> dtos){
		if(shouldBufferInSqs.get()){
			Scanner.of(dtos).map(this::toMessage).flush(traceQueueDao::putMulti);
		}else{
			// smaller puts are easier on the db
			for(TraceEntityDto dto : dtos){
				traceDao.putMulti(
						Scanner.of(dto.traceThreadDtos).map(TraceThread::new).list(),
						Scanner.of(dto.traceSpanDtos).map(TraceSpan::new).list(),
						new Trace(dto.traceDto));
			}
		}
	}

}