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
package io.datarouter.gcp.spanner.op.read;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.KeySet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.tuple.Range;

public class SpannerGetRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseReadOp<D>{

	protected final Collection<Range<PK>> ranges;
	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public SpannerGetRangesOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecRegistry codecRegistry){
		this(client, fieldInfo, ranges, config, codecRegistry, fieldInfo.getTableName());
	}

	protected SpannerGetRangesOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<Range<PK>> ranges,
			Config config,
			SpannerFieldCodecRegistry codecRegistry,
			String tableName){
		super(client, config, codecRegistry, tableName);
		this.ranges = ranges;
		this.fieldInfo = fieldInfo;
	}

	@Override
	public List<D> wrappedCall(){
		return callClient(fieldInfo.getFieldColumnNames(), fieldInfo.getFields(), fieldInfo.getDatabeanSupplier());
	}

	@Override
	public KeySet buildKeySet(){
		KeySet.Builder keySetBuilder = KeySet.newBuilder();
		if(ranges == null || ranges.isEmpty()){
			keySetBuilder.setAll();
		}else{
			ranges.stream()
					.map(this::convertRange)
					.forEach(keySetBuilder::addRange);
		}
		return keySetBuilder.build();
	}

}