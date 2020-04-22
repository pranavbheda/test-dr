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
package io.datarouter.websocket.config;

import java.util.List;

import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.DatarouterServletGuiceModule;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.websocket.auth.GuiceWebSocketAuthenticationFilter;
import io.datarouter.websocket.endpoint.WebSocketServices;
import io.datarouter.websocket.service.DefaultServerAddressProvider;
import io.datarouter.websocket.service.GuiceWebSocketConfig;
import io.datarouter.websocket.service.ServerAddressProvider;
import io.datarouter.websocket.session.PushServiceSettings;
import io.datarouter.websocket.session.PushServiceSettingsSupplier;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao.DatarouterWebSocketDaoParams;
import io.datarouter.websocket.storage.subscription.DatarouterWebSocketSubscriptionDao;
import io.datarouter.websocket.storage.subscription.DatarouterWebSocketSubscriptionDao.DatarouterWebSocketSubscriptionDaoParams;

public class DatarouterWebSocketPlugin extends BaseJobPlugin{

	private final Class<? extends WebSocketServices> webSocketServices;
	private final Class<? extends ServerAddressProvider> serverAddressProvider;

	private final String pushServiceCipherKey;
	private final String pushServiceSalt;
	private final String pushServiceApiKey;

	private DatarouterWebSocketPlugin(
			Class<? extends WebSocketServices> webSocketServices,
			Class<? extends ServerAddressProvider> serverAddressProvider,
			DatarouterWebSocketDaoDaoModule daosModule,
			String pushServiceCipherKey,
			String pushServiceSalt,
			String pushServiceApiKey){
		this.webSocketServices = webSocketServices;
		this.serverAddressProvider = serverAddressProvider;
		this.pushServiceCipherKey = pushServiceCipherKey;
		this.pushServiceSalt = pushServiceSalt;
		this.pushServiceApiKey = pushServiceApiKey;

		addSettingRoot(DatarouterWebSocketSettingRoot.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.TOOLS, new DatarouterWebSocketPaths().datarouter.websocketTool,
				"WebSocket");
		addTriggerGroup(DatarouterWebSocketTriggerGroup.class);
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH,
				GuiceWebSocketAuthenticationFilter.class));
		addRouteSet(DatarouterWebSocketApiRouteSet.class);
		setDaosModule(daosModule);
	}

	@Override
	public String getName(){
		return "DatarouterWebSocket";
	}

	@Override
	public void configure(){
		bind(WebSocketServices.class).to(webSocketServices);
		bind(ServerAddressProvider.class).to(serverAddressProvider);
		bind(PushServiceSettingsSupplier.class)
				.toInstance(new PushServiceSettings(pushServiceCipherKey, pushServiceSalt, pushServiceApiKey));
	}

	public static class DatarouterWebSocketPluginBuilder{

		private final DatarouterWebSocketDaoDaoModule daoModule;
		private final Class<? extends WebSocketServices> webSocketServices;
		private final String pushServiceCipherKey;
		private final String pushServiceSalt;
		private final String pushServiceApiKey;

		private Class<? extends ServerAddressProvider> serverAddressProvider = DefaultServerAddressProvider.class;

		public DatarouterWebSocketPluginBuilder(
				ClientId websocketClientId,
				Class<? extends WebSocketServices> webSocketServicesClass,
				@SuppressWarnings("unused")// found at runtime. ides will show this as unused
				Class<? extends GuiceWebSocketConfig> webSocketConfig,
				String pushServiceCipherKey,
				String pushServiceSalt,
				String pushServiceApiKey){
			this.daoModule = new DatarouterWebSocketDaoDaoModule(websocketClientId);
			this.webSocketServices = webSocketServicesClass;
			this.pushServiceCipherKey = pushServiceCipherKey;
			this.pushServiceSalt = pushServiceSalt;
			this.pushServiceApiKey = pushServiceApiKey;
		}

		public DatarouterWebSocketPluginBuilder withServerAddressProviderClass(
				Class<? extends ServerAddressProvider> serverAddressProviderClass){
			this.serverAddressProvider = serverAddressProviderClass;
			return this;
		}

		public DatarouterWebSocketPlugin build(){
			return new DatarouterWebSocketPlugin(
					webSocketServices,
					serverAddressProvider,
					daoModule,
					pushServiceCipherKey,
					pushServiceSalt,
					pushServiceApiKey);
		}

	}

	public static class DatarouterWebSocketDaoDaoModule extends DaosModuleBuilder{

		private final ClientId webSocketClientId;

		public DatarouterWebSocketDaoDaoModule(ClientId webSocketClientId){
			this.webSocketClientId = webSocketClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterWebSocketSessionDao.class,
					DatarouterWebSocketSubscriptionDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterWebSocketDaoParams.class).toInstance(new DatarouterWebSocketDaoParams(webSocketClientId));
			bind(DatarouterWebSocketSubscriptionDaoParams.class).toInstance(
					new DatarouterWebSocketSubscriptionDaoParams(webSocketClientId));
		}

	}

}
