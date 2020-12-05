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
package io.datarouter.tasktracker.storage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.tasktracker.storage.LongRunningTask.LongRunningTaskFielder;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterLongRunningTaskDao extends BaseDao{

	public static class DatarouterLongRunningTaskDaoParams extends BaseRedundantDaoParams{

		public DatarouterLongRunningTaskDaoParams(List<ClientId> clientId){
			super(clientId);
		}

	}

	private final SortedMapStorageNode<LongRunningTaskKey,LongRunningTask,LongRunningTaskFielder> node;

	@Inject
	public DatarouterLongRunningTaskDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterLongRunningTaskDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<LongRunningTaskKey,LongRunningTask,LongRunningTaskFielder> node =
							nodeFactory.create(clientId, LongRunningTask::new, LongRunningTaskFielder::new)
							.withIsSystemTable(true)
							.disableNodewatchPercentageAlert()
							.buildAndRegister();
					return node;
					})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	public SortedMapStorageNode<LongRunningTaskKey,LongRunningTask,LongRunningTaskFielder> getNode(){
		return node;
	}

	public Scanner<LongRunningTask> scan(){
		return node.scan();
	}

	public Scanner<LongRunningTask> scan(Range<LongRunningTaskKey> range){
		return node.scan(range);
	}

	public Scanner<LongRunningTask> scanWithPrefix(LongRunningTaskKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public void deleteBatched(Scanner<LongRunningTaskKey> keys){
		node.deleteBatched(keys);
	}

}
