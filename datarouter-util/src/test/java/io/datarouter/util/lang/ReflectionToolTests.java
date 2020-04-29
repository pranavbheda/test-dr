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
package io.datarouter.util.lang;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReflectionToolTests{

	public static class DummyDto{
		public final Object field0;
		public final int field1;
		public final Double field2;

		public DummyDto(Object field0, int field1, Double field2){
			this.field0 = field0;
			this.field1 = field1;
			this.field2 = field2;
		}
	}

	public static class ExtensionDto extends DummyDto{
		public final long field3;

		public ExtensionDto(Object field0, int field1, Double field2, long field3){
			super(field0, field1, field2);
			this.field3 = field3;
		}
	}

	@Test
	public void testCanParamsCallParamTypes(){
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(Arrays.asList(4), Arrays.asList(int.class)));
		Assert.assertTrue(ReflectionTool.canParamsCallParamTypes(Arrays.asList(4), Arrays.asList(Integer.class)));
		Assert.assertFalse(ReflectionTool.canParamsCallParamTypes(Arrays.asList("a"), Arrays.asList(int.class)));
	}

	@Test
	public void testCreateWithParameters(){
		Object[] params0 = new Object[]{new Object(), 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, Arrays.asList(params0)));

		Object[] params1 = new Object[]{"stringy", 3, 5.5d};
		Assert.assertNotNull(ReflectionTool.createWithParameters(DummyDto.class, Arrays.asList(params1)));
	}

	@Test(expectedExceptions = {Exception.class})
	public void testCreateWithParametersInvalid(){
		Object[] params0 = new Object[]{new Object(), "square peg", 5.5d};
		DummyDto dummyDto = ReflectionTool.createWithParameters(DummyDto.class, Arrays.asList(params0));
		Assert.assertNotNull(dummyDto);
	}

	@Test
	public void testGetDeclaredFieldsFromAncestors(){
		Assert.assertEquals(ReflectionTool.getDeclaredFieldsFromAncestors(ExtensionDto.class).size(), 3);
	}

	@Test
	public void testGetDeclaredFieldsIncludingAncestors(){
		Assert.assertEquals(ReflectionTool.getDeclaredFieldsIncludingAncestors(ExtensionDto.class).size(), 4);
	}

	@Test
	public void testGetDeclaredFields(){
		Assert.assertEquals(ReflectionTool.getDeclaredFields(ExtensionDto.class).size(), 1);
	}

}