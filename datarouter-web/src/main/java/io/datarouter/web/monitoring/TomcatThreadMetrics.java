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
package io.datarouter.web.monitoring;

import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.util.JmxTool;
import io.datarouter.web.port.CompoundPortIdentifier;
import jakarta.inject.Singleton;

@Singleton
public class TomcatThreadMetrics{

	public List<TomcatThreadsJspDto> getTomcatPoolMetrics(){
		var query = JmxTool.newObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=ThreadPool,name=*");
		return JmxTool.SERVER.queryNames(query, null).stream()
				.map(poolMxBean -> {
					int currentThreadCount = (int)JmxTool.getAttribute(poolMxBean, "currentThreadCount");
					int currentThreadsBusy = (int)JmxTool.getAttribute(poolMxBean, "currentThreadsBusy");
					String poolMxBeanName = poolMxBean.getKeyProperty("name");
					String poolName = poolMxBeanName.substring(1, poolMxBeanName.length() - 1);
					return new TomcatThreadsJspDto(poolName, currentThreadCount, currentThreadsBusy);
				})
				.collect(Collectors.toList());
	}

}
