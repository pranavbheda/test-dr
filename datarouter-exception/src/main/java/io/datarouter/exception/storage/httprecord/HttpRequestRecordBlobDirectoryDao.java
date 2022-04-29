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
package io.datarouter.exception.storage.httprecord;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.exception.dto.HttpRequestRecordBlobDto;
import io.datarouter.exception.storage.exceptionrecord.HttpRequestRecordBlobDirectorySupplier;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

@Singleton
public class HttpRequestRecordBlobDirectoryDao{

	@Inject
	private HttpRequestRecordBlobDirectorySupplier directory;

	public String read(String filename){
		return StringCodec.UTF_8.decode(directory.getHttpRequestRecordBlobDirectory().read(PathbeanKey.of(filename)));
	}

	public void write(HttpRequestRecordBlobDto countBlobDto, String ulid){
		PathbeanKey key = PathbeanKey.of(ulid);
		directory.getHttpRequestRecordBlobDirectory().write(key, countBlobDto.serializeToBytes());
	}

	public Scanner<String> scanKeys(){
		return directory.getHttpRequestRecordBlobDirectory().scanKeys(Subpath.empty())
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getHttpRequestRecordBlobDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getHttpRequestRecordBlobDirectory().deleteAll(Subpath.empty());
	}

}
