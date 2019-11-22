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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterAdditionalAdministrators;
import io.datarouter.storage.config.DatarouterAdditionalAdministratorsSupplier;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.dispatcher.DatarouterWebRouteSet;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.file.FilesRoot;
import io.datarouter.web.filter.StaticFileFilter;
import io.datarouter.web.filter.https.HttpsFilter;
import io.datarouter.web.filter.requestcaching.GuiceRequestCachingFilter;
import io.datarouter.web.listener.AppListenersClasses;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterAppListenersClasses;
import io.datarouter.web.listener.DatarouterShutdownAppListener;
import io.datarouter.web.listener.ExecutorsAppListener;
import io.datarouter.web.listener.HttpClientAppListener;
import io.datarouter.web.listener.InitializeEagerClientsAppListener;
import io.datarouter.web.listener.JspWebappListener;
import io.datarouter.web.listener.NoJavaSessionWebAppListener;
import io.datarouter.web.listener.TomcatWebAppNamesWebAppListener;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.DatarouterSessionDao.DatarouterSessionDaoParams;
import io.datarouter.web.user.authenticate.PermissionRequestAdditionalEmails;
import io.datarouter.web.user.authenticate.PermissionRequestAdditionalEmailsSupplier;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserInfo;
import io.datarouter.web.user.session.service.UserSessionService;

public class DatarouterWebPlugin extends BaseWebPlugin{

	private final DatarouterService datarouterService;
	// TODO change binding for a noop implementation.
	private final Class<? extends FilesRoot> filesClass;
	private final Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass;
	private final Class<? extends CurrentUserSessionInfo> currentUserSessionInfoClass;
	private final Class<? extends UserInfo> userInfoClass;
	private final Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
	private final Class<? extends ExceptionRecorder> exceptionRecorderClass;
	private final Set<String> additionalAdministrators;
	private final Set<String> additionalPermissionRequestEmails;
	private final List<Class<? extends DatarouterAppListener>> appListenerClasses;
	private final Class<? extends RoleManager> roleManagerClass;
	private final Class<? extends UserSessionService> userSessionServiceClass;

	// only used to get simple data from plugin
	private DatarouterWebPlugin(DatarouterWebDaoModule daosModuleBuilder){
		this(null, null, null, null, null, null, null, null, null, null, null, null, daosModuleBuilder);
	}

	private DatarouterWebPlugin(
			DatarouterService datarouterService,
			Class<? extends FilesRoot> filesClass,
			Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass,
			Class<? extends CurrentUserSessionInfo> currentUserSessionInfoClass,
			Class<? extends UserInfo> userInfoClass,
			Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass,
			Class<? extends ExceptionRecorder> exceptionRecorderClass,
			Set<String> additionalAdministrators,
			Set<String> additionalPermissionRequestEmails,
			List<Class<? extends DatarouterAppListener>> appListenerClasses,
			Class<? extends RoleManager> roleManagerClass,
			Class<? extends UserSessionService> userSessionServiceClass,
			DatarouterWebDaoModule daosModuleBuilder){
		addRouteSet(DatarouterWebRouteSet.class);
		addSettingRoot(DatarouterWebSettingRoot.class);
		setDaosModuleBuilder(daosModuleBuilder);

		addAppListener(ExecutorsAppListener.class);
		addAppListener(HttpClientAppListener.class);
		addAppListener(DatarouterShutdownAppListener.class);
		addAppListener(InitializeEagerClientsAppListener.class);

		addWebListener(TomcatWebAppNamesWebAppListener.class);
		addWebListener(JspWebappListener.class);
		addWebListener(NoJavaSessionWebAppListener.class);

		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, StaticFileFilter.class));
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH,
				GuiceRequestCachingFilter.class));
		addFilterParams(new FilterParams(false, DatarouterServletGuiceModule.ROOT_PATH, HttpsFilter.class));

		this.datarouterService = datarouterService;
		this.filesClass = filesClass;
		this.authenticationConfigClass = authenticationConfigClass;
		this.currentUserSessionInfoClass = currentUserSessionInfoClass;
		this.userInfoClass = userInfoClass;
		this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
		this.exceptionRecorderClass = exceptionRecorderClass;
		this.additionalAdministrators = additionalAdministrators;
		this.additionalPermissionRequestEmails = additionalPermissionRequestEmails;
		this.appListenerClasses = appListenerClasses;
		this.roleManagerClass = roleManagerClass;
		this.userSessionServiceClass = userSessionServiceClass;
	}

	@Override
	public void configure(){
		bind(FilesRoot.class).to(filesClass);
		bindDefault(ExceptionRecorder.class, exceptionRecorderClass);
		bind(DatarouterService.class).toInstance(datarouterService);

		bindActualNullSafe(DatarouterAuthenticationConfig.class, authenticationConfigClass);
		bindActualNullSafe(CurrentUserSessionInfo.class, currentUserSessionInfoClass);
		bindActualNullSafe(UserInfo.class, userInfoClass);
		bindActualNullSafe(ExceptionHandlingConfig.class, exceptionHandlingConfigClass);
		bindActualInstanceNullSafe(DatarouterAdditionalAdministratorsSupplier.class,
				new DatarouterAdditionalAdministrators(additionalAdministrators));
		bindActualInstanceNullSafe(PermissionRequestAdditionalEmailsSupplier.class,
				new PermissionRequestAdditionalEmails(additionalPermissionRequestEmails));
		Collections.reverse(appListenerClasses); // move web's listeners to the top
		bindActualInstance(AppListenersClasses.class, new DatarouterAppListenersClasses(appListenerClasses));
		bindActualNullSafe(RoleManager.class, roleManagerClass);
		bindActualNullSafe(UserSessionService.class, userSessionServiceClass);
	}

	public List<Class<? extends DatarouterAppListener>> getFinalAppListeners(){
		return appListenerClasses;
	}

	private <T> void bindActualNullSafe(Class<T> type, Class<? extends T> actualClass){
		if(actualClass != null){
			bindActual(type, actualClass);
		}
	}

	private <T> void bindActualInstanceNullSafe(Class<T> type, T actualInstance){
		if(actualInstance != null){
			bindActualInstance(type, actualInstance);
		}
	}

	public static class DatarouterWebDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterSessionClientId;

		public DatarouterWebDaoModule(ClientId datarouterSessionClientId){
			this.datarouterSessionClientId = datarouterSessionClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DatarouterSessionDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterSessionDaoParams.class)
					.toInstance(new DatarouterSessionDaoParams(datarouterSessionClientId));
		}

	}

	public static class DatarouterWebPluginBuilder{

		private final DatarouterService datarouterService;
		private final ClientId defaultClientId;
		private DatarouterWebDaoModule daoModule;

		private Class<? extends FilesRoot> filesClass;
		private Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass;
		private Class<? extends CurrentUserSessionInfo> currentUserSessionInfoClass;
		private Class<? extends UserInfo> userInfoClass;
		private Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass;
		private Class<? extends ExceptionRecorder> exceptionRecorderClass;
		private Set<String> additionalAdministrators = Collections.emptySet();
		private Set<String> additionalPermissionRequestEmails = Collections.emptySet();
		private List<Class<? extends DatarouterAppListener>> appListenerClasses;
		private Class<? extends RoleManager> roleManagerClass;
		private Class<? extends UserSessionService> userSessionServiceClass;

		public DatarouterWebPluginBuilder(DatarouterService datarouterService, ClientId defaultClientId){
			this.datarouterService = datarouterService;
			this.defaultClientId = defaultClientId;
		}

		public DatarouterWebPluginBuilder setFilesClass(Class<? extends FilesRoot> filesClass){
			this.filesClass = filesClass;
			return this;
		}

		public DatarouterWebPluginBuilder setDatarouterAuthConfig(
				Class<? extends DatarouterAuthenticationConfig> authenticationConfigClass){
			this.authenticationConfigClass = authenticationConfigClass;
			return this;
		}

		public DatarouterWebPluginBuilder setCurrentUserSessionInfoClass(
				Class<? extends CurrentUserSessionInfo> currentUserSessionInfoClass){
			this.currentUserSessionInfoClass = currentUserSessionInfoClass;
			return this;
		}

		public DatarouterWebPluginBuilder setUserInfoClass(Class<? extends UserInfo> userInfoClass){
			this.userInfoClass = userInfoClass;
			return this;
		}

		public DatarouterWebPluginBuilder setExceptionHandlingClass(
				Class<? extends ExceptionHandlingConfig> exceptionHandlingConfigClass){
			this.exceptionHandlingConfigClass = exceptionHandlingConfigClass;
			return this;
		}

		public DatarouterWebPluginBuilder setExceptionRecorderClass(
				Class<? extends ExceptionRecorder> exceptionRecorderClass){
			this.exceptionRecorderClass = exceptionRecorderClass;
			return this;
		}

		public DatarouterWebPluginBuilder setAdditionalAdministrators(Set<String> additionalAdministrators){
			this.additionalAdministrators = additionalAdministrators;
			return this;
		}

		public DatarouterWebPluginBuilder setAdditionalPermissionRequestEmails(
				Set<String> additionalPermissionRequestEmails){
			this.additionalPermissionRequestEmails = additionalPermissionRequestEmails;
			return this;
		}

		public DatarouterWebPluginBuilder setRoleManagerClass(Class<? extends RoleManager> roleManagerClass){
			this.roleManagerClass = roleManagerClass;
			return this;
		}

		public DatarouterWebPluginBuilder setUserSesssionServiceClass(
				Class<? extends UserSessionService> userSessionServiceClass){
			this.userSessionServiceClass = userSessionServiceClass;
			return this;
		}

		public DatarouterWebPluginBuilder setAppListenerClasses(
				List<Class<? extends DatarouterAppListener>> appListenerClasses){
			this.appListenerClasses = appListenerClasses;
			return this;
		}

		public DatarouterWebPluginBuilder setDaoModule(DatarouterWebDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterWebPlugin getSimplePluginData(){
			return new DatarouterWebPlugin(daoModule != null ? daoModule : new DatarouterWebDaoModule(defaultClientId));
		}

		public DatarouterWebPlugin build(){

			return new DatarouterWebPlugin(
					datarouterService,
					filesClass,
					authenticationConfigClass,
					currentUserSessionInfoClass,
					userInfoClass,
					exceptionHandlingConfigClass,
					exceptionRecorderClass,
					additionalAdministrators,
					additionalPermissionRequestEmails,
					appListenerClasses,
					roleManagerClass,
					userSessionServiceClass,
					daoModule == null ? new DatarouterWebDaoModule(defaultClientId) : daoModule);
		}

	}

}
