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
package io.datarouter.auth.job;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.Role;
import io.datarouter.web.user.role.RoleManager;
import jakarta.inject.Inject;

public class DeletedRoleCleanupJob extends BaseJob{

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterUserHistoryDao userHistoryDao;

	private record DeletedRoleCleanupDto(
			DatarouterUser updatedUser,
			DatarouterUserHistory historyEntry,
			DatarouterChangelogDto changelogEntry){}

	@Override
	public void run(TaskTracker tracker){
		Set<Role> roles = roleManager.getAllRoles();
		userDao.scan()
				.exclude(datarouterUser -> roles.containsAll(datarouterUser.getRoles()))
				.batch(100)
				.map(batch -> Scanner.of(batch)
							.map(user -> {
								Set<Role> deletedRoles = Scanner.of(user.getRoles())
												.exclude(roles::contains)
												.collect(HashSet::new);
								user.removeRoles(deletedRoles);
								List<String> removedRolePersistentStrings = Scanner.of(deletedRoles)
										.map(Role::getPersistentString)
										.list();
								String changes = "Removed deleted role types: " + removedRolePersistentStrings;
								var userHistory = new DatarouterUserHistory(
										user.getId(),
										Instant.now(),
										DatarouterUserCreationService.ADMIN_ID,
										DatarouterUserChangeType.EDIT,
										changes);
								var changelogEntry = new DatarouterChangelogDtoBuilder(
										DatarouterUserHistoryService.CHANGELOG_TYPE,
										user.getUsername(),
										DatarouterUserChangeType.EDIT.persistentString,
										adminEmail.get())
										.withComment(changes)
										.build();
								return new DeletedRoleCleanupDto(user, userHistory, changelogEntry);
							})
							.list())
				.forEach(batch -> {
					userDao.putMulti(Scanner.of(batch).map(DeletedRoleCleanupDto::updatedUser).list());
					userHistoryDao.putMulti(Scanner.of(batch).map(DeletedRoleCleanupDto::historyEntry).list());
					Scanner.of(batch)
							.map(DeletedRoleCleanupDto::changelogEntry)
							.forEach(changelogRecorder::record);
				});
	}

}
