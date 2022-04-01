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
package io.datarouter.model.field.codec;

import java.util.Date;

import com.google.gson.reflect.TypeToken;

public class DateToLongFieldCodec extends LongFieldCodec<Date>{

	public DateToLongFieldCodec(){
		super(TypeToken.get(Date.class),
				() -> null,
				Date::getTime,
				() -> null,
				Date::new,
				Date::compareTo,
				new Date(0));
	}

}
