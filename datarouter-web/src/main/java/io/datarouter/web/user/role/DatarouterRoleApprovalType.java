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

import io.datarouter.enums.StringMappedEnum;

public enum DatarouterRoleApprovalType implements RoleApprovalTypeEnum<DatarouterRoleApprovalType>{
	ADMIN("admin");

	public static final StringMappedEnum<DatarouterRoleApprovalType> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.roleApprovalType.persistentString);

	private final RoleApprovalType roleApprovalType;

	DatarouterRoleApprovalType(String persistentString){
		this.roleApprovalType = new RoleApprovalType(persistentString);
	}

	@Override
	public RoleApprovalType getRoleApprovalType(){
		return roleApprovalType;
	}

	@Override
	public String getPersistentString(){
		return roleApprovalType.getPersistentString();
	}

	@Override
	public DatarouterRoleApprovalType fromPersistentString(String str){
		return BY_PERSISTENT_STRING.fromOrNull(str);
	}

}
