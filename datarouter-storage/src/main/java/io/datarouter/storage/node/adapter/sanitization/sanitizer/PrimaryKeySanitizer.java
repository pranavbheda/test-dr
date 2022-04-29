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
package io.datarouter.storage.node.adapter.sanitization.sanitizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;

public class PrimaryKeySanitizer{
	private static final Logger logger = LoggerFactory.getLogger(PrimaryKeySanitizer.class);

	public static void checkForNullPrimaryKeyValues(PrimaryKey<?> pk){
		for(Field<?> field : pk.getFields()){
			if(field.getKey().getAutoGeneratedType().isGenerated()){
				continue;
			}else if(field.getValue() == null){
				String fieldName = field.getPrefixedName();
				throw new RuntimeException("null value detected for field=" + fieldName + " in PK=" + pk);
			}
		}
	}

	public static void logForNullPrimaryKeyValues(PrimaryKey<?> pk){
		for(Field<?> field : pk.getFields()){
			if(field.getValue() == null){
				String fieldName = field.getPrefixedName();
				if(logger.isDebugEnabled()){
					logger.warn("null value key={} field={}", pk, fieldName, new Exception());
				}else{
					logger.warn("null value key={} field={}", pk, fieldName);
				}
			}
		}
	}

}
