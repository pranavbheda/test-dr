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
package io.datarouter.auth.web;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthFiles;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterAccountAvailableEndpointsProvider;
import io.datarouter.auth.service.DatarouterAccountCounters;
import io.datarouter.auth.service.DatarouterAccountCredentialService;
import io.datarouter.auth.service.DatarouterAccountCredentialService.DatarouterAccountSecretCredentialKeypairDto;
import io.datarouter.auth.service.DatarouterAccountCredentialService.SecretCredentialDto;
import io.datarouter.auth.service.DefaultDatarouterAccountAvailableEndpointsProvider;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import io.datarouter.web.user.session.service.Session;

public class DatarouterAccountManagerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountManagerHandler.class);

	private final BaseDatarouterAccountDao datarouterAccountDao;
	private final BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao;
	private final DatarouterAccountCredentialService acccountCredentialService;
	private final DatarouterProperties datarouterProperties;
	private final DatarouterAuthFiles files;
	private final DatarouterAccountAvailableEndpointsProvider datarouterAccountAvailableEndpointsProvider;
	private final Bootstrap4ReactPageFactory reactPageFactory;
	private final ChangelogRecorder changelogRecorder;
	private final MetricLinkBuilder metricLinkBuilder;
	private final CurrentUserSessionInfoService currentSessionInfoService;
	private final String path;

	@Inject
	public DatarouterAccountManagerHandler(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao,
			DatarouterAccountCredentialService acccountCredentialService,
			DatarouterProperties datarouterProperties,
			DatarouterAuthFiles files,
			DatarouterAuthPaths paths,
			DefaultDatarouterAccountAvailableEndpointsProvider defaultDatarouterAccountAvailableEndpointsProvider,
			Bootstrap4ReactPageFactory reactPageFactory,
			ChangelogRecorder changelogRecorder,
			MetricLinkBuilder metricLinkBuilder,
			CurrentUserSessionInfoService currentSessionInfoService){
		this(datarouterAccountDao,
				datarouterAccountPermissionDao,
				acccountCredentialService,
				datarouterProperties,
				files,
				defaultDatarouterAccountAvailableEndpointsProvider,
				reactPageFactory,
				changelogRecorder,
				metricLinkBuilder,
				currentSessionInfoService,
				paths.admin.accounts.toSlashedString());
	}

	protected DatarouterAccountManagerHandler(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao,
			DatarouterAccountCredentialService acccountCredentialService,
			DatarouterProperties datarouterProperties,
			DatarouterAuthFiles files,
			DatarouterAccountAvailableEndpointsProvider datarouterAccountAvailableEndpointsProvider,
			Bootstrap4ReactPageFactory reactPageFactory,
			ChangelogRecorder changelogRecorder,
			MetricLinkBuilder metricLinkBuilder,
			CurrentUserSessionInfoService currentSessionInfoService,
			String path){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterAccountPermissionDao = datarouterAccountPermissionDao;
		this.acccountCredentialService = acccountCredentialService;
		this.datarouterProperties = datarouterProperties;
		this.files = files;
		this.datarouterAccountAvailableEndpointsProvider = datarouterAccountAvailableEndpointsProvider;
		this.reactPageFactory = reactPageFactory;
		this.changelogRecorder = changelogRecorder;
		this.metricLinkBuilder = metricLinkBuilder;
		this.currentSessionInfoService = currentSessionInfoService;

		this.path = path;
	}

	@Handler(defaultHandler = true)
	public Mav index(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter Account Manager")
				.withRequires(DatarouterWebRequireJs.SORTTABLE)
				.withReactScript(files.js.accountManagerJsx)
				.withJsStringConstant("REACT_BASE_PATH", request.getContextPath() + path + "/")
				.buildMav();
	}

	@Handler
	public List<DatarouterAccountDetails> list(){
		return getDetailsForAccounts(datarouterAccountDao.scan().list());
	}

	@Handler
	public DatarouterAccountDetails getDetails(String accountName){
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetails add(String accountName){
		Require.isFalse(accountName.isEmpty());
		String creator = getSessionInfo().getRequiredSession().getUsername();
		var account = new DatarouterAccount(accountName, new Date(), creator);
		datarouterAccountDao.put(account);
		logAndRecordAction(accountName, "add");
		return getDetailsForAccounts(List.of(account)).get(0);
	}

	@Handler
	public DatarouterAccountDetails toggleUserMappings(String accountName){
		return updateAccount(accountName, DatarouterAccount::toggleUserMappings, "toggleUserMappings");
	}

	@Handler
	public void delete(String accountName){
		DatarouterAccountPermissionKey prefix = new DatarouterAccountPermissionKey(accountName);
		datarouterAccountPermissionDao.deleteWithPrefix(prefix);
		acccountCredentialService.deleteAllCredentialsForAccount(accountName, getSessionInfo().getRequiredSession());
		DatarouterAccountKey accountKey = new DatarouterAccountKey(accountName);
		datarouterAccountDao.delete(accountKey);
		logAndRecordAction(accountName, "delete");
	}

	@Handler
	public DatarouterAccountDetails addCredential(String accountName){
		Require.isFalse(accountName.isEmpty());
		String creatorUsername = getSessionInfo().getRequiredSession().getUsername();
		acccountCredentialService.createCredential(accountName, creatorUsername);
		logAndRecordAction(accountName, "add credential");
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetails deleteCredential(String apiKey, String accountName){
		acccountCredentialService.deleteCredential(apiKey);
		logAndRecordAction(accountName, "delete credential");
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetailsAndKeypair addSecretCredential(String accountName){
		Require.isFalse(accountName.isEmpty());
		Session session = getSessionInfo().getRequiredSession();
		String creatorUsername = session.getUsername();
		var secretOpReason = WebSecretOpReason.manualOp(session, getClass().getSimpleName());
		var keypair = acccountCredentialService.createSecretCredential(accountName, creatorUsername, secretOpReason);
		logAndRecordAction(accountName, "add secret credential");
		return new DatarouterAccountDetailsAndKeypair(getDetailsForAccountName(accountName), keypair);
	}

	@Handler
	public DatarouterAccountDetails deleteSecretCredential(String secretName, String accountName){
		var secretOpReason = WebSecretOpReason.manualOp(getSessionInfo().getRequiredSession(), getClass()
				.getSimpleName());
		acccountCredentialService.deleteSecretCredential(secretName, secretOpReason);
		logAndRecordAction(accountName, "delete secret credential");
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetails setCredentialActivation(@RequestBody SetCredentialActivationDto dto){
		Require.isFalse(dto.accountName.isEmpty());
		Require.notNull(dto.active);
		if(dto.secretName != null && StringTool.notEmptyNorWhitespace(dto.secretName)){
			acccountCredentialService.setSecretCredentialActivation(dto.secretName, dto.active);
		}else if(dto.apiKey != null && StringTool.notEmptyNorWhitespace(dto.apiKey)){
			acccountCredentialService.setCredentialActivation(dto.apiKey, dto.active);
		}else{
			throw new RuntimeException("apiKey or secretName is required");
		}
		return getDetails(dto.accountName);
	}

	@Handler
	public List<String> getAvailableEndpoints(){
		List<String> availableEndpoints = new ArrayList<>();
		availableEndpoints.add(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		availableEndpoints.addAll(datarouterAccountAvailableEndpointsProvider.getAvailableEndpoints());
		return availableEndpoints;
	}

	@Handler
	public DatarouterAccountDetails addPermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.put(new DatarouterAccountPermission(accountName, endpoint));
		logAndRecordAction(accountName, "addPermission");
		return getDetails(accountName);
	}

	@Handler
	public DatarouterAccountDetails deletePermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.delete(new DatarouterAccountPermissionKey(accountName, endpoint));
		logAndRecordAction(accountName, "deletePermission");
		return getDetails(accountName);
	}

	@Handler
	public boolean isServerTypeDev(){
		return StringTool.equalsCaseInsensitive(
				datarouterProperties.getServerTypeString(),
				ServerType.DEV.getPersistentString());
	}

	private DatarouterAccountDetails updateAccount(
			String accountName,
			Consumer<DatarouterAccount> updateFunction,
			String logMessage){
		DatarouterAccount account = datarouterAccountDao.get(new DatarouterAccountKey(accountName));
		updateFunction.accept(account);
		datarouterAccountDao.put(account);
		logAndRecordAction(accountName, logMessage);
		return getDetailsForAccountName(accountName);
	}

	private List<DatarouterAccountDetails> getDetailsForAccounts(List<DatarouterAccount> accounts){
		ZoneId zoneId = currentSessionInfoService.getZoneId(request);
		Set<String> accountNames = Scanner.of(accounts)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(HashSet::new);

		var credentialsByAccountName = acccountCredentialService.getCredentialsByAccountName(accountNames, zoneId);
		var secretCredentialsByAccountName = acccountCredentialService.getSecretCredentialsByAccountName(accountNames,
				zoneId);
		var permissionsByAccountName = Scanner.of(accountNames)
				.map(DatarouterAccountPermissionKey::new)
				.listTo(datarouterAccountPermissionDao::scanKeysWithPrefixes)
				.map(TextPermission::create)
				.groupBy(permission -> permission.accountName);

		return Scanner.of(accounts)
				.map(account -> new AccountDto(account, zoneId))
				.map(account -> getDetailsForAccount(
						account,
						credentialsByAccountName.get(account.accountName),
						secretCredentialsByAccountName.get(account.accountName),
						permissionsByAccountName.get(account.accountName)))
				.list();
	}

	private DatarouterAccountDetails getDetailsForAccount(AccountDto account, List<AccountCredentialDto> credentials,
			List<SecretCredentialDto> secretCredentials, List<TextPermission> permissions){
		String counterName = DatarouterCounters.PREFIX + " " + DatarouterAccountCounters.ACCOUNT + " "
				+ DatarouterAccountCounters.NAME + " " + account.accountName;
		String metricLink = metricLinkBuilder.exactMetricLink(counterName);
		return new DatarouterAccountDetails(account, credentials, secretCredentials, permissions, metricLink);
	}

	public DatarouterAccountDetails getDetailsForAccountName(String accountName){
		DatarouterAccount account = datarouterAccountDao.get(new DatarouterAccountKey(accountName));
		return getDetailsForAccounts(List.of(account)).get(0);
	}

	private void logAndRecordAction(String account, String action){
		recordChangelog("DatarouterAccount", account, action);
		logger.warn("account={} action={} by={}", account, action, getCurrentUsername());
	}

	private String getCurrentUsername(){
		return getSessionInfo().getNonEmptyUsernameOrElse("unknown");
	}

	private void recordChangelog(String changelogType, String name, String action){
		changelogRecorder.record(
				changelogType,
				name,
				action,
				getCurrentUsername());
	}

	public static class DatarouterAccountDetailsAndKeypair{

		public final DatarouterAccountDetails details;
		public final DatarouterAccountSecretCredentialKeypairDto keypair;

		public DatarouterAccountDetailsAndKeypair(DatarouterAccountDetails details,
				DatarouterAccountSecretCredentialKeypairDto keypair){
			this.details = details;
			this.keypair = keypair;
		}

	}

	public static class DatarouterAccountDetails{

		public final AccountDto account;
		public final List<AccountCredentialDto> credentials;
		public final List<SecretCredentialDto> secretCredentials;
		public final List<TextPermission> permissions;
		public final String metricLink;
		public final String error;

		public DatarouterAccountDetails(AccountDto account, List<AccountCredentialDto> credentials,
				List<SecretCredentialDto> secretCredentials, List<TextPermission> permissions, String metricLink){
			this.account = account;
			this.credentials = credentials == null ? List.of() : credentials;
			this.secretCredentials = secretCredentials == null ? List.of() : secretCredentials;
			this.permissions = permissions == null ? List.of() : permissions;
			this.metricLink = metricLink;
			this.error = null;
		}

		public DatarouterAccountDetails(String error){
			this.account = null;
			this.credentials = null;
			this.secretCredentials = null;
			this.permissions = null;
			this.metricLink = null;
			this.error = error;
		}

	}

	public static class AccountDto{

		public final String accountName;
		public final String created;
		public final String creator;
		public final String lastUsed;
		public final Boolean enableUserMappings;

		public AccountDto(DatarouterAccount account, ZoneId zoneId){
			this.accountName = account.getKey().getAccountName();
			this.created = account.getCreatedDate(zoneId);
			this.creator = account.getCreator();
			this.lastUsed = account.getLastUsedDate(zoneId);
			this.enableUserMappings = account.getEnableUserMappings();
		}

	}

	public static class AccountCredentialDto{

		public final String apiKey;
		public final String secretKey;
		public final String accountName;
		public final String created;
		public final String creatorUsername;
		public final String lastUsed;
		public final Boolean active;

		public AccountCredentialDto(DatarouterAccountCredential credential, ZoneId zoneId){
			this.apiKey = credential.getKey().getApiKey();
			this.secretKey = credential.getSecretKey();
			this.accountName = credential.getAccountName();
			this.created = credential.getCreatedDate(zoneId);
			this.creatorUsername = credential.getCreatorUsername();
			this.lastUsed = credential.getLastUsedDate(zoneId);
			this.active = credential.getActive();
		}

	}

	public static class AvailableRouteSet{

		public final String name;
		public final String className;
		public final List<String> rules;

		public AvailableRouteSet(String name, String className, List<String> rules){
			this.name = name;
			this.className = className;
			this.rules = rules;
		}

	}

	public static class TextPermission{

		public final String accountName;
		public final String endpoint;

		public TextPermission(String accountName, String endpoint){
			this.accountName = accountName;
			this.endpoint = endpoint;
		}

		public static TextPermission create(DatarouterAccountPermissionKey permission){
			return new TextPermission(permission.getAccountName(), permission.getEndpoint());
		}

	}

	public static class SetCredentialActivationDto{

		public final String apiKey;
		public final String secretName;
		public final Boolean active;
		public final String accountName;

		public SetCredentialActivationDto(String apiKey, String secretName, Boolean active, String accountName){
			this.apiKey = apiKey;
			this.secretName = secretName;
			this.active = active;
			this.accountName = accountName;
		}

	}

}
