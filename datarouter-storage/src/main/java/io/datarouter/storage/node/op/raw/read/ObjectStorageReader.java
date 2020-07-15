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
package io.datarouter.storage.node.op.raw.read;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.NodeOps;

/**
 * Methods for reading from an object store such as the filesystem or S3.
 */
public interface ObjectStorageReader
extends NodeOps<PathbeanKey,Pathbean>{

	public static final String OP_exists = "exists";
	public static final String OP_list = "list";
	public static final String OP_listWithPrefix = "listWithPrefix";

	boolean exists(PathbeanKey key);
	Scanner<PathbeanKey> scanKeys();
	Scanner<Pathbean> scan();

}
