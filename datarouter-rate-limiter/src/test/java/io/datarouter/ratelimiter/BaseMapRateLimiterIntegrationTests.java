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
package io.datarouter.ratelimiter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.ratelimiter.CacheRateLimiterConfig.CacheRateLimiterConfigBuilder;
import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.util.time.ZoneIds;

@Guice(moduleFactory = RateLimiterTestNgModuleFactory.class)
public class BaseMapRateLimiterIntegrationTests{

	@Inject
	private BaseTallyDao tallyDao;

	private Instant getCal(){
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse("2010-03-04T16:30:49", LocalDateTime::from)
				.with(ChronoField.MILLI_OF_SECOND, 32)
				.atZone(ZoneIds.UTC)
				.toInstant();
	}

	@Test
	public void testGetTimeStr(){
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.DAYS).getTimeStr(getCal()), "2010-02-28T00:00:00Z");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.HOURS).getTimeStr(getCal()), "2010-03-04T15:00:00Z");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.MINUTES).getTimeStr(getCal()), "2010-03-04T16:30:00Z");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.SECONDS).getTimeStr(getCal()), "2010-03-04T16:30:45Z");
		Assert.assertEquals(makeTestRateLimiter(TimeUnit.MILLISECONDS).getTimeStr(getCal()),
				"2010-03-04T16:30:49.030Z");
	}

	private BaseCacheRateLimiter makeTestRateLimiter(TimeUnit unit){
		var config = new CacheRateLimiterConfigBuilder("MapRateLimiterIntegrationTests")
				.setMaxAverageRequests(0L)
				.setMaxSpikeRequests(0L)
				.setNumIntervals(0)
				.setBucketTimeInterval(5, unit)
				.build();
		return new BaseTallyCacheRateLimiter(tallyDao, config);
	}

}
