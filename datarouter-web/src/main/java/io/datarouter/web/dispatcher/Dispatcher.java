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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;

import io.datarouter.httpclient.security.SecurityValidationResult;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.OptionalTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.ServletContextProvider;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.params.MultipartParams;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.SamlService;
import io.datarouter.web.user.authenticate.saml.SamlSettings;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.util.http.ResponseTool;

@Singleton
public class Dispatcher{

	private static final String JSESSIONID_PATH_PARAM = ";jsessionid=";

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private ServletContextProvider servletContextProvider;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private SamlService samlService;
	@Inject
	private SamlSettings samlSettings;

	public boolean handleRequestIfUrlMatch(HttpServletRequest request, HttpServletResponse response,
			BaseRouteSet routeSet) throws ServletException{
		String uri = request.getRequestURI();
		if(!uri.startsWith(servletContextProvider.get().getContextPath() + routeSet.getUrlPrefix())){
			return false;
		}
		BaseHandler handler = null;
		String afterContextPath = uri.substring(servletContextProvider.get().getContextPath().length());
		if(afterContextPath.contains(JSESSIONID_PATH_PARAM)){
			afterContextPath = afterContextPath.substring(0, afterContextPath.indexOf(JSESSIONID_PATH_PARAM));
		}
		for(DispatchRule rule : routeSet.getDispatchRules()){
			if(rule.getPattern().matcher(afterContextPath).matches()){
				SecurityValidationResult securityCheckResult = rule.applySecurityValidation(request);
				request = securityCheckResult.getWrappedRequest();
				if(!securityCheckResult.isSuccess()){
					// TODO improve this. Right now it returns 404 and log "dispatcher could not find Handler for /uri"
					// while it's more an "access denied"
					return false;
				}
				if(authenticationConfig.useDatarouterAuthentication() && !rule.checkRoles(request)){
					handleMissingRoles(request, response);
					return true;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				break;
			}
		}

		Class<? extends BaseHandler> defaultHandlerClass = routeSet.getDefaultHandlerClass();
		if(handler == null){
			if(defaultHandlerClass == null){
				return false;// url not found
			}
			handler = injector.getInstance(defaultHandlerClass);
		}

		handler.setRequest(request);
		handler.setResponse(response);
		handler.setServletContext(servletContextProvider.get());
		Params params = parseParams(request, handler.getDefaultMultipartCharset());
		handler.setParams(params);
		handler.handleWrapper();
		return true;
	}

	private Params parseParams(HttpServletRequest request, Charset defaultCharset) throws ServletException{
		if(isMultipart(request)){
			try{
				return new MultipartParams(request, defaultCharset);
			}catch(FileUploadException e){
				throw new ServletException(e);
			}
		}
		return new Params(request);
	}

	private void handleMissingRoles(HttpServletRequest request, HttpServletResponse response){
		Optional<DatarouterSession> session = DatarouterSessionManager.getFromRequest(request);
		boolean isAnonymous = OptionalTool.mapOrElse(session, DatarouterSession::isAnonymous, true);
		if(isAnonymous){
			String url = request.getRequestURL() + "?" + StringTool.nullSafe(request.getQueryString());

			if(samlSettings.getShouldProcess()){
				samlService.redirectToIdentityProvider(request, response);
			}else{
				sessionManager.addTargetUrlCookie(response, url);
				ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath()
						+ authenticationConfig.getSigninPath());
			}
			return;
		}

		boolean isRequestorOnly = OptionalTool.mapOrElse(session, DatarouterSession::isRequestorOnly, false);
		if(isRequestorOnly){
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath()
					+ authenticationConfig.getPermissionRequestPath());
			return;
		}

		try{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private static boolean isMultipart(HttpServletRequest request){
		return request.getContentType() != null
				&& request.getContentType().toLowerCase().contains("multipart/form-data");
	}

}
