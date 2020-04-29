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
package io.datarouter.metric.counter.collection.archive;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.metric.DatarouterMetricExecutors.DatarouterCountArchiveFlushSchedulerDbExecutor;
import io.datarouter.metric.DatarouterMetricExecutors.DatarouterCountArchiveFlusherDbExecutor;
import io.datarouter.metric.DatarouterMetricExecutors.DatarouterCountArchiveFlusherMemoryExecutor;
import io.datarouter.metric.counter.setting.DatarouterCountSettingRoot;

@Singleton
public class CountArchiveFlusherFactory{

	@Inject
	private DatarouterCountArchiveFlushSchedulerDbExecutor flushSchedulerDb;
	@Inject
	private DatarouterCountArchiveFlusherMemoryExecutor flushSchedulerMemory;
	@Inject
	private DatarouterCountArchiveFlusherDbExecutor flushExecutorDb;
	@Inject
	private DatarouterCountSettingRoot settings;

	public CountArchiveFlusher createMemoryFlusher(String name, long flushPeriodMs){
		return new CountArchiveFlusher(name, flushPeriodMs, flushSchedulerMemory, flushSchedulerDb, settings);
	}

	public CountArchiveFlusher createDbFlusher(String name, long flushPeriodMs){
		return new CountArchiveFlusher(name, flushPeriodMs, flushSchedulerDb, flushExecutorDb, settings);
	}

}