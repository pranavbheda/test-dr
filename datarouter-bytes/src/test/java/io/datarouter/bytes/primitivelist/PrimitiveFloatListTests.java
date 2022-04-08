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
package io.datarouter.bytes.primitivelist;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveFloatListTests{

	@Test
	public void testEquals(){
		List<Float> first = List.of(1f, 2f);
		var second = new PrimitiveFloatList(new float[]{-1, 1, 2, 100}, 1, 3);
		Assert.assertEquals(first, second);
		Assert.assertEquals(second, first);
	}

	@Test
	public void testSetByIndex(){
		var list = new PrimitiveFloatList(new float[]{-2, 1, 2, 3, 4, 5, 6, 7, 100}, 1, 8);
		list.set(6, 700f);
		Assert.assertEquals(list.size(), 7);
		list.set(2, 300f);
		list.set(0, -100f);
		Assert.assertEquals(list.size(), 7);
		Assert.assertEquals(list, new PrimitiveFloatList(new float[]{-100, 2, 300, 4, 5, 6, 700}));
		Assert.assertEquals(list.get(0).floatValue(), -100f);
		Assert.assertEquals(list.get(2).floatValue(), 300f);
		Assert.assertEquals(list.get(6).floatValue(), 700f);
	}

	@Test
	public void testCollectionsSort(){
		var list = new PrimitiveFloatList(new float[]{-2, -1, 5, 3, 4, 0, 2, 1, 100}, 2, 8);
		Collections.sort(list);
		Assert.assertEquals(list, new PrimitiveFloatList(new float[]{0, 1, 2, 3, 4, 5}));
	}

	@Test
	public void testSubList(){
		float[] array = {-2, -1, 0, 1, 2, 3, 4};
		var list = new PrimitiveFloatList(array, 2, 5);
		Assert.assertEquals(list, List.of(0f, 1f, 2f));
		var subList = list.subList(1, 3);
		Assert.assertEquals(subList, List.of(1f, 2f));
	}

}
