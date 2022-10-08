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
package io.datarouter.filesystem.snapshot.reader.record;

import java.util.Arrays;
import java.util.Comparator;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;

public record SnapshotRecord(
		long id,
		byte[] key,
		byte[] value,
		byte[][] columnValues){

	public static final Comparator<SnapshotRecord> KEY_COMPARATOR = Comparator.comparing(
			snapshotRecord -> snapshotRecord.key,
			Arrays::compareUnsigned);

	public SnapshotRecord(long id, SnapshotEntry entry){
		this(id, entry.key(), entry.value(), entry.columnValues);
	}

	public SnapshotEntry entry(){
		return new SnapshotEntry(key, value, columnValues);
	}

	public SnapshotLeafRecord leafRecord(){
		return new SnapshotLeafRecord(id, key, value);
	}

}
