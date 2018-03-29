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
package io.datarouter.web.user.authenticate;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.SamlService;
import io.datarouter.web.user.authenticate.saml.SamlSettings;

public class DatarouterSigninHandler extends BaseHandler{

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private SamlSettings samlSettings;
	@Inject
	private SamlService samlService;

	@Handler(defaultHandler = true)
	protected Mav showForm(){
		if(samlSettings.getShouldProcess()){
			samlService.redirectToIdentityProvider(request, response);
			return null;
		}
		Mav mav = new Mav("/jsp/authentication/signinForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}

}
