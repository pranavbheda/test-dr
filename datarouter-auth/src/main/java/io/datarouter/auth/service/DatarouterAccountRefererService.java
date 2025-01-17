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
package io.datarouter.auth.service;

import java.time.Duration;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.util.cache.LoadingCache;
import io.datarouter.util.cache.LoadingCache.LoadingCacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountRefererService{

	private final LoadingCache<String,RefererCheck> checkByAccountName;

	@Inject
	public DatarouterAccountRefererService(DatarouterAccountDao accountDao){
		checkByAccountName = new LoadingCacheBuilder<String,RefererCheck>()
				.withName("AccountRefererCheck")
				.withExpireTtl(Duration.ofMinutes(1))
				.withLoadingFunction(accountName -> {
					DatarouterAccount account = accountDao.get(new DatarouterAccountKey(accountName));
					String referer = account.getReferrer();
					if(referer == null){
						return $ -> new DatarouterAccountRefererCheck(true, false);
					}
					return request -> {
						String reqReferer = request.getHeader(HttpHeaders.REFERER);
						boolean allowed = reqReferer != null && reqReferer.startsWith(referer);
						return new DatarouterAccountRefererCheck(allowed, true);
					};
				})
				.build();
	}

	public DatarouterAccountRefererCheck validateAccountReferer(String accountName, HttpServletRequest request){
		return checkByAccountName.getOrThrow(accountName).apply(request);
	}

	public record DatarouterAccountRefererCheck(
			boolean allowed,
			boolean hasRefererValidation){
	}

	//unnecessary, just to shorten type definition
	private interface RefererCheck extends Function<HttpServletRequest,DatarouterAccountRefererCheck>{}

}
