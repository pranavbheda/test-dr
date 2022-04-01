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
package io.datarouter.util.serialization;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.datarouter.gson.serialization.OptionalLegacyTypeAdapterFactory;

public class OptionalLegacyTypeAdapterFactoryTests{

	@Test(dataProvider = "dataProvider")
	public void testInteroperability(Optional<Instant> instant){
		Type type = new TypeToken<Optional<Instant>>(){}.getType();
		Gson legacyAdapterGson = new GsonBuilder()
				.registerTypeAdapterFactory(new OptionalLegacyTypeAdapterFactory())
				.create();
		String legacyAdapterJson = legacyAdapterGson.toJson(instant);
		Assert.assertEquals(legacyAdapterGson.fromJson(legacyAdapterJson, type), instant);

		// Remove to get to Java 16
		Gson legacyReflectionGson = new Gson();
		String legacyReflectionJson = legacyReflectionGson.toJson(instant);
		Assert.assertEquals(legacyReflectionJson, legacyAdapterJson);
		Assert.assertEquals(legacyReflectionGson.fromJson(legacyAdapterJson, type), instant);
		Assert.assertEquals(legacyReflectionGson.fromJson(legacyReflectionJson, type), instant);
		Assert.assertEquals(legacyAdapterGson.fromJson(legacyReflectionJson, type), instant);
	}

	@DataProvider
	public Object[][] dataProvider(){
		return new Object[][]{{Optional.of(Instant.ofEpochMilli(1647548719105L))}, {Optional.empty()}, {null}};
	}

}
