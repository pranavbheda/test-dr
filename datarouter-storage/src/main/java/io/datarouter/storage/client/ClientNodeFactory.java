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
package io.datarouter.storage.client;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.router.Router;

public interface ClientNodeFactory{

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createWrappedNode(NodeParams<PK,D,F> nodeParams);

	<EK extends EntityKey<EK>,E extends Entity<EK>> EntityNode<EK,E> createEntityNode(NodeFactory nodeFactory,
			Router router, EntityNodeParams<EK,E> entityNodeParams, ClientId clientId);

	<EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK,D,F> createWrappedSubEntityNode(EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> nodeParams);

}
