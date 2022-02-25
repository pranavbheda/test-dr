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
package io.datarouter.metric.counter;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

@Singleton
public class CountBlobDao{

	@Inject
	private CountBlobDirectorySupplier directory;

	public String read(String filename){
		return new String(directory.getCountBlobDirectory().read(PathbeanKey.of(filename)), StandardCharsets.UTF_8);
	}

	public void write(CountBlobDto countBlobDto){
		PathbeanKey key = PathbeanKey.of(countBlobDto.ulid);
		directory.getCountBlobDirectory().write(key, countBlobDto.serializeToBytes());
	}

	public Scanner<String> scanKeys(){
		return directory.getCountBlobDirectory().scanKeys(Subpath.empty())
				.map(PathbeanKey::getPathAndFile);
	}

	public void delete(String filename){
		directory.getCountBlobDirectory().delete(PathbeanKey.of(filename));
	}

	public void deleteAll(){
		directory.getCountBlobDirectory().deleteAll(Subpath.empty());
	}

}
