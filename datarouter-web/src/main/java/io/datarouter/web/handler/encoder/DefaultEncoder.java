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
package io.datarouter.web.handler.encoder;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.mav.Mav;

@Singleton
public class DefaultEncoder implements HandlerEncoder{

	@Inject
	private MavEncoder mavEncoder;
	@Inject
	private InputStreamHandlerEncoder inputStreamHandlerEncoder;
	@Inject
	private JsonEncoder jsonEncoder;

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException{
		if(result == null){
			return;
		}
		if(result instanceof Mav){
			mavEncoder.finishRequest(result, servletContext, response, request);
		}else if(result instanceof InputStream){
			inputStreamHandlerEncoder.finishRequest(result, servletContext, response, request);
		}else{
			jsonEncoder.finishRequest(result, servletContext, response, request);
		}
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request) throws IOException{
		jsonEncoder.sendExceptionResponse(exception, servletContext, response, request);
	}

}
