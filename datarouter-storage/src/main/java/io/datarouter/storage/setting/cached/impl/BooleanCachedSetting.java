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
package io.datarouter.storage.setting.cached.impl;

import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.storage.setting.type.BooleanSetting;
import io.datarouter.util.BooleanTool;

public class BooleanCachedSetting extends CachedSetting<Boolean> implements BooleanSetting{

	public BooleanCachedSetting(SettingFinder finder, String name, DefaultSettingValue<Boolean> defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public boolean isValid(String value){
		return BooleanTool.isBoolean(value);
	}

}
