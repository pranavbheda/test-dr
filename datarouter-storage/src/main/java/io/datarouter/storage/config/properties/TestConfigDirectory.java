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
package io.datarouter.storage.config.properties;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;

//Eventually this won't rely on DatarouterProperties. It is temporary while we break up DatarouterProperties
//so we don't have to do multiple major refactors with every split.
@Singleton
public class TestConfigDirectory implements Supplier<String>{

	private final String configDirectory;

	@Inject
	private TestConfigDirectory(DatarouterProperties datarouterProperties){
		this.configDirectory = datarouterProperties.getTestConfigDirectory();
	}

	@Override
	public String get(){
		return configDirectory;
	}

}
