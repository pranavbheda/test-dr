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
package io.datarouter.web.test;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.instrumentation.test.TestableService;

public interface TestableServiceClassRegistry extends Supplier<List<Class<? extends TestableService>>>{


	class DefaultTestableServiceClassRegistry implements TestableServiceClassRegistry{

		private final List<Class<? extends TestableService>> testableServiceClasses;

		public DefaultTestableServiceClassRegistry(List<Class<? extends TestableService>> testableServiceClasses){
			this.testableServiceClasses = testableServiceClasses;
		}

		@Override
		public List<Class<? extends TestableService>> get(){
			return testableServiceClasses;
		}

	}

}
