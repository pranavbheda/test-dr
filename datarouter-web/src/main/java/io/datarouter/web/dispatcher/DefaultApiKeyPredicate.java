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
package io.datarouter.web.dispatcher;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.util.http.RequestTool;

public class DefaultApiKeyPredicate implements ApiKeyPredicate{

	private final Supplier<String> apiKeySupplier;

	public DefaultApiKeyPredicate(Supplier<String> apiKeySupplier){
		this.apiKeySupplier = apiKeySupplier;
	}

	@Override
	public Pair<Boolean,String> check(DispatchRule rule, HttpServletRequest request){
		String apiKeyCandidate = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		if(apiKeySupplier.get().equals(apiKeyCandidate)){
			return new Pair<>(true, "");
		}
		return new Pair<>(false, "no match for " + ApiKeyPredicate.obfuscate(apiKeyCandidate));
	}

}
