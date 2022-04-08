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
package io.datarouter.model.field.imp.list;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.datarouter.model.field.ListFieldKey;

public class IntListFieldKey extends ListFieldKey<Integer,List<Integer>,IntListFieldKey>{

	public IntListFieldKey(String name){
		super(name, new TypeToken<List<Integer>>(){});
	}

	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public List<Integer> getSampleValue(){
		return List.of();
	}

}