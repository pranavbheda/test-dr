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
package io.datarouter.metric.counter;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.metric.config.MaxMetricBlobSize;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.util.UlidTool;

@Singleton
public class CountBlobService implements CountPublisher{
	private static final Logger logger = LoggerFactory.getLogger(CountBlobService.class);

	private final CountBlobDirectoryDao countBlobDirectoryDao;
	private final CountBlobQueueDao countBlobQueueDao;
	private final DatarouterCountSettingRoot countSettings;
	private final MetricBlobPublishingSettings metricBlobPublishingSettings;
	private final ServiceName serviceName;
	private final ServerName serverName;
	private final MaxMetricBlobSize maxMetricBlobSize;

	@Inject
	public CountBlobService(CountBlobDirectoryDao countBlobDao, CountBlobQueueDao countBlobQueueDao,
			DatarouterCountSettingRoot countSettings, MetricBlobPublishingSettings metricBlobPublishingSettings,
			ServiceName serviceName, ServerName serverName, MaxMetricBlobSize maxMetricBlobSize){
		this.countBlobDirectoryDao = countBlobDao;
		this.countBlobQueueDao = countBlobQueueDao;
		this.countSettings = countSettings;
		this.metricBlobPublishingSettings = metricBlobPublishingSettings;
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.maxMetricBlobSize = maxMetricBlobSize;
	}

	@Override
	public PublishingResponseDto add(Map<Long,Map<String,Long>> counts){
		CountBlobDto dto = new CountBlobDto(
				UlidTool.nextUlid(),
				serviceName.get(),
				serverName.get(),
				counts,
				metricBlobPublishingSettings.getApiKey());
		if(countSettings.saveCountBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			dto.serializeToStrings(maxMetricBlobSize.get())
					.map(blob -> new StringQueueMessage(dto.ulid, blob))
					.flush(blobs -> {
						if(blobs.size() > 1){
							logger.warn("writing size={} blobs with key={}", blobs.size(), dto.ulid);
						}else{
							logger.info("writing size={} blobs with key={}", blobs.size(), dto.ulid);
						}
						countBlobQueueDao.putMulti(blobs);
					});
		}else{
			logger.info("writing key={}", dto.ulid);
			countBlobDirectoryDao.write(dto);
		}
		return PublishingResponseDto.SUCCESS;
	}

}
