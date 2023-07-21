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
package io.datarouter.web.handler.validator;

import java.lang.reflect.Method;

import io.datarouter.httpclient.endpoint.java.BaseEndpoint;
import jakarta.inject.Singleton;

// Cannot use PluginInjector since this class is instantiated early
public interface HandlerAccountCallerValidator{

	void validate(String accountName, BaseEndpoint<?,?> endpoint);

	void validate(String accountName, Method method);

	@Singleton
	class NoOpHandlerAccountCallerValidator implements HandlerAccountCallerValidator{

		@Override
		public void validate(String accountName, BaseEndpoint<?,?> endpoint){
		}

		@Override
		public void validate(String accountName, Method method){
		}

	}

}
