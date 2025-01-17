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
package io.datarouter.web.user;

import java.util.Collection;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionKey;

public interface BaseDatarouterSessionDao{

	DatarouterSession get(DatarouterSessionKey key);
	void put(DatarouterSession databean);
	void delete(DatarouterSessionKey key);
	void deleteMulti(Collection<DatarouterSessionKey> keys);
	Scanner<DatarouterSession> scan();

	static class NoOpDatarouterSessionDao implements BaseDatarouterSessionDao{

		@Override
		public DatarouterSession get(DatarouterSessionKey key){
			return null;
		}

		@Override
		public void put(DatarouterSession databean){
		}

		@Override
		public void delete(DatarouterSessionKey key){
		}

		@Override
		public void deleteMulti(Collection<DatarouterSessionKey> keys){
		}

		@Override
		public Scanner<DatarouterSession> scan(){
			return Scanner.empty();
		}

	}

}
