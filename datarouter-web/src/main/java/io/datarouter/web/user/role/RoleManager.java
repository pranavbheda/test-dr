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
package io.datarouter.web.user.role;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.RoleApprovalType.RoleApprovalTypePriorityComparator;

public interface RoleManager{

	RoleEnum<? extends RoleEnum<?>> getRoleEnum();

	default Optional<Role> findRoleFromPersistentString(String persistentString){
		return Optional.ofNullable(getRoleEnum().fromPersistentString(persistentString))
				.map(RoleEnum::getRole);
	}

	RoleApprovalTypeEnum<? extends RoleApprovalTypeEnum<?>> getRoleApprovalTypeEnum();

	default Optional<RoleApprovalType> findRoleApprovalTypeFromPersistentString(String persistentString){
		return Optional.ofNullable(getRoleApprovalTypeEnum().fromPersistentString(persistentString))
				.map(RoleApprovalTypeEnum::getRoleApprovalType);
	}

	Set<Role> getAllRoles();
	Set<Role> getRolesForGroup(String groupId);
	Set<Role> getSuperAdminRoles();
	Set<Role> getDefaultRoles();

	default Map<RoleApprovalType, Integer> getRoleApprovalRequirements(Role role){
		return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
	}

	default Map<Role, Map<RoleApprovalType, Integer>> getAllRoleApprovalRequirements(){
		return Scanner.of(getAllRoles()).toMap(Function.identity(), role -> {
			Map<RoleApprovalType, Integer> roleApprovalRequirements = getRoleApprovalRequirements(role);
			// Each role should at a minimum require a single standard approval
			if(roleApprovalRequirements == null || roleApprovalRequirements.isEmpty()){
				return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
			}
			return roleApprovalRequirements;
		});
	}

	Map<RoleApprovalType,BiFunction<DatarouterUser,DatarouterUser,Boolean>> getApprovalTypeAuthorityValidators();

	default List<RoleApprovalType> getPrioritizedRoleApprovalTypes(
			DatarouterUser editor,
			DatarouterUser user,
			Set<RoleApprovalType> relevantApprovalTypes){
		var approvalTypeAuthorityValidators = getApprovalTypeAuthorityValidators();
		return Scanner.of(relevantApprovalTypes)
				.include(approvalTypeAuthorityValidators::containsKey)
				.include(roleApprovalType -> {
					var validatorFunction = approvalTypeAuthorityValidators.get(roleApprovalType);
					return validatorFunction.apply(editor, user);
				})
				// For RoleManagers extending others and overriding the approval type's priority
				.map(RoleApprovalType::persistentString)
				.map(this::findRoleApprovalTypeFromPersistentString)
				.concat(OptionalScanner::of)
				.sort(new RoleApprovalTypePriorityComparator())
				.list();
	}

	//these are roles that do not present a security risk, although they may be more than just the default roles
	default Set<Role> getUnimportantRoles(){
		return Set.of();
	}

	default Set<String> getAdditionalPermissionRequestEmailRecipients(
			DatarouterUser requestor,
			Set<Role> requestedRoles){
		return Set.of();
	}

}
