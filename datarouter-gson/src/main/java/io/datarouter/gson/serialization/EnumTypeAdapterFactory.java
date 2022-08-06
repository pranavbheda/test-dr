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
package io.datarouter.gson.serialization;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Intercepts all enum serialization, preventing accidental serialization of enums without explicitly specifying
 * an encoding.
 *
 * This will log unknown enums by default but can throw an exception if configured with rejectUnregistered()
 */
public abstract class EnumTypeAdapterFactory implements TypeAdapterFactory{
	private static final Logger logger = LoggerFactory.getLogger(EnumTypeAdapterFactory.class);

	private static final Set<String> loggedTypes = ConcurrentHashMap.newKeySet();

	private final Map<Type,TypeAdapter<?>> adapterByType = new HashMap<>();
	private boolean allowUnregistered = false;

	protected <T> EnumTypeAdapterFactory register(Class<T> cls, TypeAdapter<T> typeAdapter){
		if(!cls.isEnum()){
			String message = String.format("%s is not an enum", cls.getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		adapterByType.put(cls, typeAdapter);
		return this;
	}

	protected EnumTypeAdapterFactory allowUnregistered(){
		allowUnregistered = true;
		return this;
	}

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
		if(!type.getRawType().isEnum()){
			return null;
		}
		@SuppressWarnings("unchecked")
		TypeAdapter<T> typeAdapter = (TypeAdapter<T>)adapterByType.get(type.getType());
		if(typeAdapter == null){
			if(allowUnregistered){
				if(loggedTypes.add(type.toString())){
					String message = String.format(
							"Warning: please register a TypeAdapter for %s.  Currently serializing by enum.name()",
							type);
					logger.warn(message);
				}
			}else{
				String message = String.format(
						"Error: please register a TypeAdapter for %s",
						type);
				throw new IllegalArgumentException(message);
			}
		}
		return typeAdapter;
	}

}