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
package io.datarouter.bytes.blockfile.enums;

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public enum BlockfileHeaderKey{

	CHECKSUM_ALGORITHM("CHECKSUM_ALGORITHM"),
	// Track the length in case we lose access to the algorithm in which case we can skip checksums.
	CHECKSUM_LENGTH("CHECKSUM_LENGTH"),
	COMPRESSOR("COMPRESSOR"),
	USER_DICTIONARY("USER_DICTIONARY");

	public final byte[] bytes;

	private BlockfileHeaderKey(String keyString){
		this.bytes = StringCodec.UTF_8.encode(keyString);
	}

}
