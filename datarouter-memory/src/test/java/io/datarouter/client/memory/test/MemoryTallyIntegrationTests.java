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
package io.datarouter.client.memory.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.storage.test.tally.BaseCacheTallyIntegrationTests;

@Guice(moduleFactory = DatarouterMemoryTestNgModuleFactory.class)
public class MemoryTallyIntegrationTests extends BaseCacheTallyIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterMemoryTestClientIds.MEMORY);
	}

	@Override
	@Test
	public void testLongKey(){
	}

	@Override
	@Test
	public void testNullKeys(){
	}

	@Override
	@Test
	public void testVacuum(){
		//memory does not support vacuum operation
	}

}
