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
package io.datarouter.web.config;

import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.path.PathsRoot;

@Singleton
public class DatarouterWebPaths extends PathNode implements PathsRoot{

	public static final String DATAROUTER = "datarouter";

	public final AdminPaths admin = branch(AdminPaths::new, "admin");
	public final PathNode consumer = leaf("consumer");
	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, DATAROUTER);
	public final PathNode keepalive = leaf("keepalive");
	public final PathNode resetPassword = leaf("resetPassword");
	public final PathNode resetPasswordSubmit = leaf("resetPasswordSubmit");
	public final SigninPaths signin = branch(SigninPaths::new, "signin");
	public final PathNode signout = leaf("signout");
	public final SignupPaths signup = branch(SignupPaths::new, "signup");

	public static class AdminPaths extends PathNode{
		public final PathNode accounts = leaf("accounts");
		public final PathNode createUser = leaf("createUser");
		public final PathNode createUserSubmit = leaf("createUserSubmit");
		public final PathNode editUser = leaf("editUser");
		public final PathNode editUserSubmit = leaf("editUserSubmit");
		public final PathNode listUsers = leaf("listUsers");
		public final PathNode viewUsers = leaf("viewUsers");
	}

	public static class SigninPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

	public static class SignupPaths extends PathNode{
		public final PathNode submit = leaf("submit");
	}

	public static class DatarouterPaths extends PathNode{
		public final ClientPaths client = branch(ClientPaths::new, "client");
		public final PathNode executors = leaf("executors");
		public final PathNode memory = leaf("memory");
		public final NodesPaths nodes = branch(NodesPaths::new, "nodes");
		public final PathNode testApi = leaf("testApi");
		public final PathNode webappInstances = leaf("webappInstances");
		public final PathNode ipDetection = leaf("ipDetection");
		public final PathNode deployment = leaf("deployment");
		public final PathNode settings = leaf("settings");
		public final PathNode shutdown = leaf("shutdown");
	}

	public static class ClientPaths extends PathNode{
		public final PathNode inspectClient = leaf("inspectClient");
		public final PathNode initClient = leaf("initClient");
		public final PathNode initAllClients = leaf("initAllClients");
	}

	public static class NodesPaths extends PathNode{
		public final PathNode browseData = leaf("browseData");
		public final PathNode deleteData = leaf("deleteData");
		public final PathNode getData = leaf("getData");
		public final PathNode search = leaf("search");
	}

}
