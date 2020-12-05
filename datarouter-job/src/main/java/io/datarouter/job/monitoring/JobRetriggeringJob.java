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
package io.datarouter.job.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.job.storage.clusterjoblock.ClusterJobLockKey;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.util.Count;

public class JobRetriggeringJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobRetriggeringJob.class);

	private static final Duration THRESHOLD = Duration.ofMinutes(30);

	@Inject
	private DatarouterInjector injector;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;
	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private LongRunningTaskService longRunningTaskService;
	@Inject
	private DatarouterClusterJobLockDao clusterJobLockDao;

	private final Count
			total = new Count("total"),
			usesLocking = new Count("usesLocking"),
			notRunningAgainSoon = new Count("notRunningAgainSoon"),
			shouldRun = new Count("shouldRun"),
			notLocked = new Count("notLocked"),
			hasLastCompletionTime = new Count("hasLastCompletionTime"),
			retriggered = new Count("retriggered");
	private final List<Count> counts = List.of(
			total,
			usesLocking,
			notRunningAgainSoon,
			shouldRun,
			notLocked,
			hasLastCompletionTime,
			retriggered);

	@Override
	public void run(TaskTracker tracker){
		Scanner.of(injector.getInstances(triggerGroupClasses.get()))
				.concatIter(BaseTriggerGroup::getJobPackages)
				.each(total::increment)
				.include(JobPackage::usesLocking)
				.each(usesLocking::increment)
				.exclude(this::runningAgainSoon)// avoid LongRunningTask scan for frequent jobs
				.each(notRunningAgainSoon::increment)
				.include(JobPackage::shouldRun)
				.each(shouldRun::increment)
				.exclude(this::isLocked)
				.each(notLocked::increment)
				.forEach(this::retriggerIfNecessary);
		logger.warn(Count.toString(counts));
	}

	private boolean runningAgainSoon(JobPackage jobPackage){
		return jobPackage.getNextValidTimeAfter(new Date())
				.map(nextTrigger -> Duration.between(Instant.now(), nextTrigger.toInstant()))
				.map(delay -> delay.compareTo(THRESHOLD) < 0)
				.orElse(true);
	}

	private boolean isLocked(JobPackage jobPackage){
		var key = new ClusterJobLockKey(jobPackage.jobClass.getSimpleName());
		return clusterJobLockDao.exists(key);
	}

	private void retriggerIfNecessary(JobPackage jobPackage){
		Optional<Date> lastCompletionTime = longRunningTaskService.findLastSuccessDate(jobPackage.jobClass
				.getSimpleName());
		if(lastCompletionTime.isEmpty()){
			return;
		}
		hasLastCompletionTime.increment();

		//getNextValidTimeAfter should be present, because only non-manual jobs get scheduled
		Date nextValidTime = jobPackage.getNextValidTimeAfter(lastCompletionTime.get()).get();
		if(new Date().after(nextValidTime)){
			jobScheduler.scheduleRetriggeredJob(jobPackage, nextValidTime);
			retriggered.increment();
		}
	}

}
