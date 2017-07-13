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
package io.datarouter.util.bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.Assert;

public class BooleanByteTool{

	private static final int NULL = -2;

	private static byte[] getBytesNullable(Boolean value){
		if(value == null){
			return new byte[]{NULL};
		}
		return getBytes(value);
	}

	private static Boolean fromBytesNullable(final byte[] bytes, final int offset){
		if(Arrays.equals(bytes, new byte[]{NULL})){
			return null;
		}
		return fromBytes(bytes, offset);
	}

	public static byte[] getBytes(boolean value){
		return value ? new byte[]{-1} : new byte[]{0};
	}

	/**
	 * @return the number of bytes written to the array
	 */
	public static int toBytes(boolean value, byte[] bytes, int offset){
		bytes[offset] = value ? (byte)-1 : (byte)0;
		return 1;
	}

	public static boolean fromBytes(byte[] bytes, int offset){
		return bytes[offset] != 0;
	}

	public static byte[] getBooleanByteArray(List<Boolean> valuesWithNulls){
		if(valuesWithNulls == null){
			return null;
		}
		byte[] out = new byte[valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); i++){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i, 1);
		}
		return out;
	}

	public static List<Boolean> fromBooleanByteArray(final byte[] bytes, final int startIdx){
		int numBooleans = bytes.length - startIdx;
		List<Boolean> bools = new ArrayList<>();
		byte[] arrayToCopy = new byte[1];
		for(int i = 0; i < numBooleans; i++){
			System.arraycopy(bytes, i + startIdx, arrayToCopy, 0, 1);
			bools.add(fromBytesNullable(arrayToCopy, 0));
		}
		return bools;
	}

	/************************** tests ************************************/

	public static class BooleanByteToolTests{
		@Test
		public void testGetBytes(){
			Assert.assertEquals(getBytes(false)[0], 0);
			Assert.assertEquals(getBytes(true)[0], -1);
		}

		@Test
		public void testFromBytes(){
			Assert.assertEquals(fromBytes(new byte[]{0}, 0), false);
			Assert.assertEquals(fromBytes(new byte[]{1, 0}, 1), false);
			Assert.assertEquals(fromBytes(new byte[]{-37}, 0), true);
		}

		@Test
		public void testToFromByteArray(){
			boolean one = true;
			boolean two = false;
			boolean three = false;

			List<Boolean> booleans = new ArrayList<>();
			booleans.add(one);
			booleans.add(null);
			booleans.add(null);
			booleans.add(two);
			booleans.add(three);

			byte[] booleanBytes = getBooleanByteArray(booleans);
			List<Boolean> result = fromBooleanByteArray(booleanBytes, 0);
			Assert.assertEquals(result, booleans);

		}
	}
}
