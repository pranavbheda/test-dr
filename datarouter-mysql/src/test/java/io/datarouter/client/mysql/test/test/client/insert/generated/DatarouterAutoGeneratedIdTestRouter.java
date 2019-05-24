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
package io.datarouter.client.mysql.test.test.client.insert.generated;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBean;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBean.PutOpIdGeneratedManagedTestBeanFielder;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBeanKey;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBean;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBean.PutOpIdGeneratedRandomTestBeanFielder;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBeanKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.TestRouter;
import io.datarouter.storage.test.TestDatarouterProperties;

@Singleton
public class DatarouterAutoGeneratedIdTestRouter extends BaseRouter implements TestRouter{

	public final SortedMapStorage<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>
			putOpIdGeneratedManaged;
	public final SortedMapStorage<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean>
			putOpIdGeneratedRandom;

	@Inject
	public DatarouterAutoGeneratedIdTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory){
		super(datarouter, datarouterProperties, nodeFactory, datarouterSettings);

		putOpIdGeneratedManaged = createAndRegister(DatarouterMysqlTestClientids.MYSQL,
				PutOpIdGeneratedManagedTestBean::new, PutOpIdGeneratedManagedTestBeanFielder::new);
		putOpIdGeneratedRandom = createAndRegister(DatarouterMysqlTestClientids.MYSQL,
				PutOpIdGeneratedRandomTestBean::new, PutOpIdGeneratedRandomTestBeanFielder::new);
	}

}
