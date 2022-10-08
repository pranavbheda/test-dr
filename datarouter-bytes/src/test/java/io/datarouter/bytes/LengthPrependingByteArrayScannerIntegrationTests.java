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
package io.datarouter.bytes;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class LengthPrependingByteArrayScannerIntegrationTests{

	@Test
	public void testRoundtrip(){
		var stringList = List.of("hello", "a", "", "list");
		var intermediateBytes = new ArrayList<byte[]>();
		var roundtripped = Scanner.of(stringList)
				.map(StringCodec.UTF_8::encode)
				.apply(PrependLengthByteArrayScanner::of)
				.each(intermediateBytes::add)
				.concat(ExtractFromPrependedLengthByteArrayScanner::of)
				.map(StringCodec.UTF_8::decode)
				.list();
		Assert.assertEquals(roundtripped, stringList);
		var intermediateStrings = Scanner.of(intermediateBytes)
				.map(StringCodec.UTF_8::decode)
				.list();
		Assert.assertNotEquals(intermediateStrings, roundtripped);
	}

}
