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
package io.datarouter.util.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import io.datarouter.util.duration.DurationUnit;
import io.datarouter.util.duration.DurationWithCarriedUnits;

public class DurationTool{

	public static Duration sinceDate(Date date){
		Objects.requireNonNull(date);
		return Duration.ofMillis(System.currentTimeMillis() - date.getTime());
	}

	public static Duration sinceInstant(Instant from){
		Objects.requireNonNull(from);
		return Duration.between(from, Instant.now());
	}

	public static String toString(Duration duration){
		DurationWithCarriedUnits wud = new DurationWithCarriedUnits(duration.toMillis());
		return wud.toStringByMaxUnitsMaxPrecision(DurationUnit.MILLISECONDS, 2);
	}

}
