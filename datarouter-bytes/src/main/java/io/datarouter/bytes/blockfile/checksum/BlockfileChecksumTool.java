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
package io.datarouter.bytes.blockfile.checksum;

import java.util.Arrays;
import java.util.zip.Checksum;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;

public class BlockfileChecksumTool{

	public static byte[] checksum32(Checksum checksum, byte[] data){
		checksum.update(data);
		//4 byte unsigned value (as opposed to signed int).  All useful data is in the rightmost 4 bytes.
		long value = checksum.getValue();
		byte[] longBytes = RawLongCodec.INSTANCE.encode(value);
		return Arrays.copyOfRange(longBytes, 4, 8);
	}

}
