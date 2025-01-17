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
package io.datarouter.auth.service;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DatarouterUserEditServiceTests{

	@Test
	public void testChangeListSingleAdd(){
		var before = Set.of("account1", "account2");
		var after = Set.of("account3", "account1", "account2");

		String output = DatarouterUserEditService.changeList("account", before, after);
		Assert.assertEquals(output, "account added: [account3]");
	}

	@Test
	public void testChangeListSingleRemove(){
		var before = Set.of("account1", "account2", "account3");
		var after = Set.of("account1", "account2");

		String output = DatarouterUserEditService.changeList("account", before, after);
		Assert.assertEquals(output, "account removed: [account3]");
	}

	@Test
	public void testChangeListMultiAddRemove(){
		var before = Set.of("account3", "account4", "account1", "account2");
		var after = Set.of("account5", "account6", "account1", "account2");

		String output = DatarouterUserEditService.changeList("account", before, after);
		Assert.assertEquals(output, "accounts added: [account5, account6] accounts removed: [account3, account4]");
	}

	@Test
	public void testChangeListNoChanges(){
		var before = Set.of("account1", "account2");
		var after = Set.of("account1", "account2");

		String output = DatarouterUserEditService.changeList("account", before, after);
		Assert.assertEquals(output, "No changes");
	}
}
