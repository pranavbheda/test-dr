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
package io.datarouter.storage.setting.constant;

import io.datarouter.storage.setting.type.BooleanSetting;

public class ConstantBooleanSetting extends ConstantSetting<Boolean> implements BooleanSetting{

	private final boolean value;

	public static final ConstantBooleanSetting FALSE = new ConstantBooleanSetting(false);
	public static final ConstantBooleanSetting TRUE = new ConstantBooleanSetting(true);

	public ConstantBooleanSetting(boolean value){
		this.value = value;
	}

	@Override
	public Boolean getValue(){
		return value;
	}

}