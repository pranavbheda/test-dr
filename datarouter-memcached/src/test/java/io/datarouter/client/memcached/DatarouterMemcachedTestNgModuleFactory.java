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
package io.datarouter.client.memcached;

import java.util.Arrays;

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.TestDatarouterProperties;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;
import io.datarouter.web.config.DatarouterWebTestGuiceModule;

public class DatarouterMemcachedTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterMemcachedTestNgModuleFactory(){
		super(Arrays.asList(
				new DatarouterWebTestGuiceModule(),
				new DatarouterWebGuiceModule(),
				new MemcachedGuiceModule()));
	}

	public static class MemcachedGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bind(DatarouterProperties.class).to(MemcachedDatarouterProperties.class);
			bindDefault(ServerTypeDetector.class, NoOpServerTypeDetector.class);
		}

	}

	public static class MemcachedDatarouterProperties extends TestDatarouterProperties{

		@Override
		public String getDatarouterPropertiesFileLocation(){
			return getTestConfigDirectory() + "/memcached.properties";
		}

	}

}
