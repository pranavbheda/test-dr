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
package io.datarouter.secret.service;


import java.time.Duration;

import io.datarouter.instrumentation.refreshable.BaseMemoizedRefreshableSupplier;
import io.datarouter.secret.op.SecretOpReason;

public class RefreshableSecretStringSupplier extends BaseMemoizedRefreshableSupplier<String>{

	private final String secretName;
	private final SecretService secretService;

	public RefreshableSecretStringSupplier(String secretName, SecretService secretService){
		this(secretName, secretService, Duration.ofSeconds(30L));
	}

	public RefreshableSecretStringSupplier(String secretName, SecretService secretService, Duration minimumTtl){
		this(secretName, secretService, minimumTtl, minimumTtl);
	}

	public RefreshableSecretStringSupplier(String secretName, SecretService secretService, Duration minimumTtl,
			Duration attemptInterval){
		super(minimumTtl, attemptInterval);
		this.secretName = secretName;
		this.secretService = secretService;
		refresh();
	}

	@Override
	protected String readNewValue(){
		return secretService.read(secretName, String.class, SecretOpReason.automatedOp(getClass().getSimpleName()));
	}

	@Override
	protected String getIdentifier(){
		return secretName;
	}

}