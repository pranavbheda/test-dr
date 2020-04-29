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
package io.datarouter.nodewatch.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.nodewatch.job.NodewatchConfigurationScanJob;
import io.datarouter.nodewatch.job.TableCountJob;
import io.datarouter.nodewatch.job.TableSamplerJob;
import io.datarouter.nodewatch.job.TableSizeMonitoringJob;

@Singleton
public class DatarouterNodewatchTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterNodewatchTriggerGroup(DatarouterNodewatchSettingRoot settings){
		super("DatarouterNodewatch");
		registerLocked(
				"43 8/" + TableSamplerJob.SCHEDULING_INTERVAL.toMinutes() + " * * * ?",
				settings.tableSamplerJob,
				TableSamplerJob.class,
				true);
		registerLocked(
				"33 25 3/4 * * ?",
				settings.tableCountJob,
				TableCountJob.class,
				true);
		registerLocked(
				"0 0 14 ? * MON-FRI",
				settings.tableSizeMonitoringJob,
				TableSizeMonitoringJob.class,
				true);
		registerLocked(
				"0 0 14 ? * MON,TUE,WED,THU,FRI *",
				settings.runConfigurationScanReportEmailJob,
				NodewatchConfigurationScanJob.class,
				true);
	}

}