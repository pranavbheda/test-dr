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
package io.datarouter.secret.handler;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.handler.SecretHandlerOpRequestDto.SecretOpDto;
import io.datarouter.web.user.session.service.Session;

/**
 * Use this interface to enable {@link Secret} ops through {@link SecretHandler}.
 */
public interface SecretHandlerPermissions{

	boolean isAuthorized(Session session, SecretOpDto secretOp);

	static class NoOpSecretHandlerPermissions implements SecretHandlerPermissions{

		@Override
		public boolean isAuthorized(Session session, SecretOpDto secretOp){
			return false;
		}

	}

}