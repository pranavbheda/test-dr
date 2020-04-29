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
package io.datarouter.auth.storage.useraccountmap;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap.DatarouterUserAccountMapFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterUserAccountMapDao extends BaseDao implements BaseDatarouterUserAccountMapDao{

	public static class DatarouterUserAccountMapDaoParams extends BaseDaoParams{

		public DatarouterUserAccountMapDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<DatarouterUserAccountMapKey,DatarouterUserAccountMap> node;

	@Inject
	public DatarouterUserAccountMapDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterUserAccountMapDaoParams params){
		super(datarouter);
		node = nodeFactory.create(
				params.clientId,
				DatarouterUserAccountMap::new,
				DatarouterUserAccountMapFielder::new)
				.buildAndRegister();
	}

	@Override
	public void deleteMulti(Collection<DatarouterUserAccountMapKey> keys){
		node.deleteMulti(keys);
	}

	@Override
	public void put(DatarouterUserAccountMap databean){
		node.put(databean);
	}

	@Override
	public void putMulti(Collection<DatarouterUserAccountMap> databeans){
		node.putMulti(databeans);
	}

	@Override
	public Scanner<DatarouterUserAccountMapKey> scanKeysWithPrefix(DatarouterUserAccountMapKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	@Override
	public boolean exists(DatarouterUserAccountMapKey key){
		return node.exists(key);
	}

}