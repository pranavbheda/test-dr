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
package io.datarouter.bytes.kvfile;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class KvFileTests{

	private record TestKv(
			String key,
			int version,
			KvFileOp op,
			String value){

		static final StringCodec STRING_CODEC = StringCodec.UTF_8;
		static final Codec<TestKv,KvFileEntry> CODEC = Codec.of(
				testKv -> KvFileEntry.create(
						STRING_CODEC.encode(testKv.key),
						ComparableIntCodec.INSTANCE.encode(testKv.version),
						testKv.op,
						STRING_CODEC.encode(testKv.value)),
				binaryKv -> new TestKv(
						STRING_CODEC.decode(binaryKv.key()),
						ComparableIntCodec.INSTANCE.decode(binaryKv.version()),
						binaryKv.op(),
						STRING_CODEC.decode(binaryKv.value())));
	}

	private static final KvFileCodec<TestKv> KV_FILE_CODEC = new KvFileCodec<>(TestKv.CODEC);

	private static final List<TestKv> KVS = List.of(
			new TestKv("", 0, KvFileOp.PUT, ""),// 0 data bytes, 1 version byte
			new TestKv("a", 1, KvFileOp.DELETE, ""),// 2 data bytes, 1 version byte
			new TestKv("bb", 2, KvFileOp.PUT, "bb"),// 4 data bytes, 1 version byte
			new TestKv("ccc", 135, KvFileOp.PUT, "ccc"));// 6 data bytes, 2 version bytes

	private static final int KEY_META_LENGTH = 4;// 1 byte per item
	private static final int KEY_DATA_LENGTH = 0 + 1 + 2 + 3;
	private static final int KEY_LENGTH = KEY_META_LENGTH + KEY_DATA_LENGTH;

	private static final int VERSION_META_LENGTH = 4;// 1 byte per item
	private static final int VERSION_DATA_LENGTH = 4 * 4;// 4 bytes per item
	private static final int VERSION_LENGTH = VERSION_META_LENGTH + VERSION_DATA_LENGTH;

	private static final int OP_META_LENGTH = 0;// no overhead, fixed length per entry
	private static final int OP_DATA_LENGTH = 4;// 1 byte per item
	private static final int OP_LENGTH = OP_META_LENGTH + OP_DATA_LENGTH;

	private static final int VALUE_META_LENGTH = 4;// 1 byte per item
	private static final int VALUE_DATA_LENGTH = 0 + 0 + 2 + 3;
	private static final int VALUE_LENGTH = VALUE_META_LENGTH + VALUE_DATA_LENGTH;

	private static final int TOTAL_META_LENGTH = 4;// 1 byte per entry for the total length of the entry
	private static final int TOTAL_DATA_LENGTH = KEY_LENGTH + VERSION_LENGTH + OP_LENGTH + VALUE_LENGTH;
	private static final int TOTAL_LENGTH = TOTAL_META_LENGTH + TOTAL_DATA_LENGTH;

	@Test
	private void testWrite(){
		byte[] bytes = KV_FILE_CODEC.toByteArray(KVS);
		Assert.assertEquals(bytes.length, TOTAL_LENGTH);
	}

	@Test
	private void testRead(){
		byte[] bytes = KV_FILE_CODEC.toByteArray(KVS);
		List<TestKv> actual = KV_FILE_CODEC.decodeMulti(bytes).list();
		Assert.assertEquals(KVS, actual);
	}

	@Test
	private void testReadInputStream(){
		byte[] bytes = KV_FILE_CODEC.toByteArray(KVS);
		var reader = new KvFileReader(new ByteArrayInputStream(bytes));
		List<TestKv> actual = reader.scanEntries()
				.map(KV_FILE_CODEC::decode)
				.list();
		Assert.assertEquals(KVS, actual);
	}

}
