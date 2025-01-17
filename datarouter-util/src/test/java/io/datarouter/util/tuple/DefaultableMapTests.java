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
package io.datarouter.util.tuple;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultableMapTests{

	private DefaultableMap<String,String> map;

	@BeforeMethod
	public void setup(){
		map = new DefaultableMap<>(new HashMap<>());
		map.put("str", "str");
		map.put("bool", "true");
		map.put("double", "1.234");
		map.put("int", "6");
	}

	@Test
	public void test(){
		Assert.assertTrue(map.getBoolean("bool", false));
		Assert.assertTrue(map.getBoolean("boola", true));
		Assert.assertTrue(map.getDouble("double", 0.1).equals(1.234));
		Assert.assertTrue(map.getInteger("int", 1).equals(6));
		Assert.assertTrue(map.getInteger("inta", 1).equals(1));
	}

}
