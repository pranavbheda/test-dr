/*
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
package io.datarouter.nodewatch.service;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.web.user.session.RequestAwareCurrentSessionInfoFactory.RequestAwareCurrentSessionInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NodewatchChangelogService{

	@Inject
	private ChangelogRecorder changelogRecorder;

	public void recordTable(
			RequestAwareCurrentSessionInfo currentSessionInfo,
			String clientName,
			String tableName,
			String action){
		var dto = new DatarouterChangelogDtoBuilder(
				DatarouterNodewatchPlugin.NAME,
				clientName + "." + tableName,
				action,
				currentSessionInfo.getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
	}

	public void recordMigrateMetadata(
			RequestAwareCurrentSessionInfo currentSessionInfo,
			String sourceNode,
			String targetNode){
		var dto = new DatarouterChangelogDtoBuilder(
				DatarouterNodewatchPlugin.NAME,
				String.format("from %s to %s", sourceNode, targetNode),
				"migrate metadata",
				currentSessionInfo.getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
	}
}
