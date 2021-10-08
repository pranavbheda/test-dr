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
package io.datarouter.web.listener;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.management.NotificationBroadcaster;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.MxBeans;

@Singleton
public class GcNotificationReceiver implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(GcNotificationReceiver.class);

	@Override
	public void onStartUp(){
		for(GarbageCollectorMXBean gc : MxBeans.GCS){
			((NotificationBroadcaster)gc).addNotificationListener((notification, $) -> {
				var gcNotifInfo = GarbageCollectionNotificationInfo.from((CompositeData)notification.getUserData());
				GcInfo gcInfo = gcNotifInfo.getGcInfo();
				Map<String,MemoryUsage> memoryUsageBeforeGc = gcInfo.getMemoryUsageBeforeGc();
				Map<String,MemoryUsage> emoryUsageAfterGc = gcInfo.getMemoryUsageAfterGc();
				Map<String,Long> byteChangeByPool = Scanner.of(memoryUsageBeforeGc.entrySet())
						.toMap(Entry::getKey,
								entry -> emoryUsageAfterGc.get(entry.getKey()).getUsed() - entry.getValue().getUsed());
				String memoryPoolChange = Scanner.of(byteChangeByPool.entrySet())
						.include(entry -> entry.getKey().startsWith("G1"))
						.map(entry -> entry.getKey().replace(' ', '_') + "_change="
								+ entry.getValue() / 1024 / 1024 + "M")
						.collect(Collectors.joining(" "));
				// minus sign because we estimate the allocation to be the opposite of the reclamation
				long estimatedAllocatedByte = -Scanner.of(byteChangeByPool.entrySet())
						.include(entry -> entry.getKey().startsWith("G1"))
						.streamLongs(Entry::getValue)
						.sum();
				Counters.inc("estimatedAllocatedByte", estimatedAllocatedByte);
				logger.info(""
						+ " gcName=\"" + gc.getName() + "\""
						+ " gcCount=" + gc.getCollectionCount()
						+ " notifId=" + notification.getSequenceNumber()
						+ " gcId=" + gcInfo.getId()
						+ " getGcAction=\"" + gcNotifInfo.getGcAction() + "\""
						+ " getGcCause=\"" + gcNotifInfo.getGcCause() + "\""
						+ " durationMs=" + gcInfo.getDuration()
						+ " " + memoryPoolChange
						+ " estimatedAllocatedMB=" + estimatedAllocatedByte / 1024 / 1024);
			}, null, null);
		}
	}

}
