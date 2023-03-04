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
package io.datarouter.web.handler.types;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.endpoint.java.EndpointTool;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.endpoint.param.IgnoredField;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.json.JsonSerializer;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class LinkDecoder implements JsonAwareHandlerDecoder{
	private static final Logger logger = LoggerFactory.getLogger(LinkDecoder.class);

	private final JsonSerializer deserializer;

	@Inject
	public LinkDecoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer deserializer){
		this.deserializer = deserializer;
	}

	@Override
	public Object[] decode(HttpServletRequest request, Method method){
		Map<String,String[]> queryParams = request.getParameterMap();
		Parameter[] parameters = method.getParameters();
		Class<?> linkType = parameters[0].getType();
		if(!LinkTool.paramIsLinkObject(method)){
			throw new RuntimeException("object needs to extend BaseLink");
		}

		// populate the fields with baseLink with dummy values and then repopulate in getArgsFromEndpointObject
		@SuppressWarnings("unchecked")
		BaseLink<?> baseLink = ReflectionTool.createWithoutNoArgs((Class<? extends BaseLink<?>>)linkType);

		String body = null;
		if(EndpointTool.findRequestBody(baseLink.getClass().getFields()).isPresent()){
			body = RequestTool.getBodyAsString(request);
			if(StringTool.isEmpty(body)){
				return null;
			}
		}
		Object[] args = null;
		try{
			args = getArgsFromEndpointObject(queryParams, baseLink, body, method);
		}catch(IllegalArgumentException | IllegalAccessException ex){
			logger.warn("", ex);
		}
		return args;
	}

	private Object[] getArgsFromEndpointObject(
			Map<String,String[]> queryParams,
			BaseLink<?> baseLink,
			String body,
			Method method)
	throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = baseLink.getClass().getFields();
		for(Field field : fields){
			if(Modifier.isStatic(field.getModifiers())){
				continue;
			}
			IgnoredField ignoredField = field.getAnnotation(IgnoredField.class);
			if(ignoredField != null){
				continue;
			}
			field.setAccessible(true);

			String parameterName = EndpointTool.getFieldName(field);
			Type parameterType = field.getType();
			String[] queryParam = queryParams.get(parameterName);

			if(field.isAnnotationPresent(RequestBody.class)){
				Object requestBody = decodeType(body, field.getGenericType());
				field.set(baseLink, requestBody);
				if(requestBody instanceof Collection<?> requestBodyCollection){
					// Datarouter handler method batch <Handler.class.simpleName> <methodName>
					@SuppressWarnings("unchecked")
					Class<? extends BaseHandler> handlerClass = (Class<? extends BaseHandler>)method
							.getDeclaringClass();
					HandlerMetrics.incRequestBodyCollectionSize(
							handlerClass,
							method,
							requestBodyCollection.size());
				}
				continue;
			}

			boolean isOptional = field.getType().isAssignableFrom(Optional.class);

			// pre-emptively try to check if the parameter is actually a form-encoded array and normalize the name
			boolean isArray = parameterType instanceof Class && ((Class<?>)parameterType).isArray();
			if(isArray && queryParam == null && !parameterName.endsWith("[]")){
				parameterName += "[]";
				queryParam = queryParams.get(parameterName);
			}

			if(queryParam == null && !isOptional){
				return null;
			}

			boolean isFormEncodedArray = queryParam != null
					&& (queryParam.length > 1 || parameterName.endsWith("[]"))
					&& isArray;

			if(isFormEncodedArray){
				Class<?> componentClass = ((Class<?>)parameterType).getComponentType();
				Object typedArray = Array.newInstance(componentClass, queryParam.length);
				for(int index = 0; index < queryParam.length; index++){
					Array.set(typedArray, index, decodeType(queryParam[index], componentClass));
				}
				field.set(baseLink, typedArray);
				continue;
			}

			if(isOptional && !queryParams.containsKey(parameterName)){
				field.set(baseLink, Optional.empty());
				continue;
			}

			String parameterValue = queryParam == null ? null : queryParam[0];
			if(isOptional){
				if(parameterValue == null){
					field.set(baseLink, Optional.empty());
				}else{
					Type type = EndpointTool.extractParameterizedType(field);
					var optionalValue = decodeType(parameterValue, type);
					field.set(baseLink, Optional.of(optionalValue));
				}
			}else{
				field.set(baseLink, decodeType(parameterValue, parameterType));
			}
		}

		Object[] args = new Object[1];
		args[0] = baseLink;
		return args;
	}

	@Override
	public JsonSerializer getJsonSerializer(){
		return deserializer;
	}

}
