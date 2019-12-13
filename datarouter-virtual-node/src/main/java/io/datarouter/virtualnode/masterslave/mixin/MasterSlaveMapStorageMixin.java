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
package io.datarouter.virtualnode.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.virtualnode.masterslave.MasterSlaveNode;

public interface MasterSlaveMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MasterSlaveNode<PK,D,F,N>, MapStorage<PK,D>{

	@Override
	default void delete(PK key, Config config){
		getMaster().delete(key, config);
	}

	@Override
	default void deleteAll(Config config){
		getMaster().deleteAll(config);
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		getMaster().deleteMulti(keys, config);
	}

	@Override
	default void put(D databean, Config config){
		getMaster().put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		getMaster().putMulti(databeans, config);
	}

	@Override
	default boolean exists(PK key, Config config){
		return chooseSlaveOrElseMaster(config).exists(key, config);
	}

	@Override
	default D get(PK key, Config config){
		return chooseSlaveOrElseMaster(config).get(key, config);
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		return chooseSlaveOrElseMaster(config).getMulti(keys, config);
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		return chooseSlaveOrElseMaster(config).getKeys(keys, config);
	}

}
