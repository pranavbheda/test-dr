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
package io.datarouter.client.mysql.caseinsensitive;

import java.util.Arrays;
import java.util.List;

import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class CaseInsensitiveTestDatabean
extends BaseDatabean<CaseInsensitiveTestPrimaryKey,CaseInsensitiveTestDatabean>{

	private CaseInsensitiveTestPrimaryKey key;

	public static class CaseInsensitiveTestFielder
	extends BaseDatabeanFielder<CaseInsensitiveTestPrimaryKey,CaseInsensitiveTestDatabean>{

		public CaseInsensitiveTestFielder(){
			super(CaseInsensitiveTestPrimaryKey.class);
			addOption(MysqlCollation.utf8mb4_unicode_ci);
		}

		@Override
		public List<Field<?>> getNonKeyFields(CaseInsensitiveTestDatabean databean){
			return Arrays.asList();
		}

	}

	public CaseInsensitiveTestDatabean(){
		this(null);
	}

	public CaseInsensitiveTestDatabean(String stringField){
		this.key = new CaseInsensitiveTestPrimaryKey(stringField);
	}

	@Override
	public Class<CaseInsensitiveTestPrimaryKey> getKeyClass(){
		return CaseInsensitiveTestPrimaryKey.class;
	}

	@Override
	public CaseInsensitiveTestPrimaryKey getKey(){
		return key;
	}

}
