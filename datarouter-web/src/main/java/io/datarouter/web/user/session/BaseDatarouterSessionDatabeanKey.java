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
package io.datarouter.web.user.session;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.BasePrimaryKey;
import io.datarouter.util.collection.ListTool;

public class BaseDatarouterSessionDatabeanKey<
		PK extends BaseDatarouterSessionDatabeanKey<PK>>
extends BasePrimaryKey<PK>{

	private String sessionToken;

	public static class FieldKeys{
		public static final StringFieldKey sessionToken = new StringFieldKey("sessionToken");
	}

	@Override
	public List<Field<?>> getFields(){
		return ListTool.createArrayList(new StringField(FieldKeys.sessionToken, sessionToken));
	}

	public BaseDatarouterSessionDatabeanKey(){
	}

	public BaseDatarouterSessionDatabeanKey(String sessionToken){
		this.sessionToken = sessionToken;
	}

	public void setSessionToken(String sessionToken){
		this.sessionToken = sessionToken;
	}

	public String getSessionToken(){
		return sessionToken;
	}

}
