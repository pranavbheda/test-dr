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
package io.datarouter.trace.storage.entity;

import java.util.ArrayList;

import io.datarouter.trace.storage.span.TraceSpan;
import io.datarouter.trace.storage.thread.TraceThread;
import io.datarouter.trace.storage.trace.Trace;
import io.datarouter.util.collection.CollectionTool;

public class TraceEntity extends BaseTraceEntity<TraceEntityKey>{

	public TraceEntity(){
		super(null);
	}

	public TraceEntity(TraceEntityKey key){
		super(key);
	}

	@Override
	public Trace getTrace(){
		return CollectionTool.getFirst(getDatabeansForQualifierPrefix(Trace.class, QUALIFIER_PREFIX_Trace));
	}

	@Override
	public ArrayList<TraceThread> getTraceThreads(){
		return getListDatabeansForQualifierPrefix(TraceThread.class, QUALIFIER_PREFIX_TraceThread);
	}

	@Override
	public ArrayList<TraceSpan> getTraceSpans(){
		return getListDatabeansForQualifierPrefix(TraceSpan.class, QUALIFIER_PREFIX_TraceSpan);
	}

}