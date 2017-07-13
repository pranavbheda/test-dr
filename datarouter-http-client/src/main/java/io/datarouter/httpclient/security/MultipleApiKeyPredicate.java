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
package io.datarouter.httpclient.security;

import java.util.Collection;
import java.util.HashSet;

public class MultipleApiKeyPredicate implements ApiKeyPredicate{

	private final String apiKey;
	private final HashSet<String> keys;

	public MultipleApiKeyPredicate(String apiKey, Collection<String> otherApiKeys){
		this.apiKey = apiKey;
		this.keys = new HashSet<>(otherApiKeys.size() + 1);
		this.keys.add(apiKey);
		this.keys.addAll(otherApiKeys);
	}

	@Override
	public boolean check(String parameter){
		return keys.contains(parameter);
	}

	@Override
	public String getApiKey(){
		return apiKey;
	}

}
