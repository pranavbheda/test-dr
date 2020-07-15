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
package io.datarouter.storage.file;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.scanner.RetainingGroup;

@Guice
public class PathServiceIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(PathServiceIntegrationTests.class);

	@Inject
	private PathService pathService;

	@Test
	public void testScanDescendents(){
		Path datarouterPath = Paths.get("..");
		pathService.scanDescendants(datarouterPath, true, true)
				.map(PathService::pathToString)
				.each(logger::info)
				.retain(1)
				.forEach(this::assertSorted);
	}

	private void assertSorted(RetainingGroup<String> retained){
		if(retained.previous() == null){
			return;
		}
		int diff = retained.previous().compareTo(retained.current());
		if(diff >= 0){
			String message = String.format("%s arrived after %s", retained.current(), retained.previous());
			throw new RuntimeException(message);
		}

	}

}
