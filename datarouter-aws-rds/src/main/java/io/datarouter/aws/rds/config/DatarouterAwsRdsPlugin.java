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
package io.datarouter.aws.rds.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.aws.rds.service.AuroraAvailabilityZoneProvider;
import io.datarouter.aws.rds.service.AuroraAvailabilityZoneProvider.GenericAvailabilityZoneProvider;
import io.datarouter.aws.rds.service.AuroraClientIdProvider;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.GenericAuroraClientIdProvider;
import io.datarouter.aws.rds.service.AwsTags;
import io.datarouter.aws.rds.service.AwsTags.NoOpAwsTags;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration.NoOpDatabaseAdministrationConfiguration;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAwsRdsPlugin extends BaseJobPlugin{

	private final Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration;
	private final Class<? extends AwsTags> awsTags;
	private final List<ClientId> auroraClientIds;
	private final String availabilityZone;

	private DatarouterAwsRdsPlugin(
			Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration,
			Class<? extends AwsTags> awsTags,
			List<ClientId> auroraClientIds,
			String availabilityZone){
		this.databaseAdministrationConfiguration = databaseAdministrationConfiguration;
		this.awsTags = awsTags;
		this.auroraClientIds = auroraClientIds;
		this.availabilityZone = availabilityZone;
		addRouteSet(DatarouterAwsRdsRouteSet.class);
		addTriggerGroup(DatarouterAwsRdsTriggerGroup.class);
		addSettingRoot(DatarouterAwsRdsSettingRoot.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.MONITORING,
				new DatarouterAwsPaths().datarouter.auroraInstances, "Aurora Clients");
	}

	@Override
	public String getName(){
		return "DatarouterAws";
	}

	@Override
	public void configure(){
		bind(DatabaseAdministrationConfiguration.class).to(databaseAdministrationConfiguration);
		bind(AwsTags.class).to(awsTags);
		bind(AuroraClientIdProvider.class).toInstance(new GenericAuroraClientIdProvider(auroraClientIds));
		bind(AuroraAvailabilityZoneProvider.class).toInstance(new GenericAvailabilityZoneProvider(availabilityZone));
	}

	public static class DatarouterAwsRdsPluginBuilder{

		private Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration
				= NoOpDatabaseAdministrationConfiguration.class;
		private Class<? extends AwsTags> awsTags = NoOpAwsTags.class;
		private List<ClientId> auroraClientIds = new ArrayList<>();
		private String availabilityZone = "";

		public DatarouterAwsRdsPluginBuilder withDatabaseAdministratorConfigurationClass(
				Class<? extends DatabaseAdministrationConfiguration> databaseAdministrationConfiguration){
			this.databaseAdministrationConfiguration = databaseAdministrationConfiguration;
			return this;
		}

		public DatarouterAwsRdsPluginBuilder withAwsTagClass(Class<? extends AwsTags> awsTags){
			this.awsTags = awsTags;
			return this;
		}

		public DatarouterAwsRdsPluginBuilder addAuroraClientId(ClientId clientId){
			this.auroraClientIds.add(clientId);
			return this;
		}

		public DatarouterAwsRdsPluginBuilder addAuroraAvailabilityZone(String availabilityZone){
			this.availabilityZone = availabilityZone;
			return this;
		}

		public DatarouterAwsRdsPlugin build(){
			return new DatarouterAwsRdsPlugin(
					databaseAdministrationConfiguration,
					awsTags,
					auroraClientIds,
					availabilityZone);
		}

	}

}
