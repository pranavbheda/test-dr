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
package io.datarouter.scanner;

import java.util.Objects;
import java.util.function.Function;

/**
 * Removes consecutive duplicates.  Lighter weight than Stream's distinct() because all elements need not be
 * collected into memory.
 */
public class DeduplicatingScanner<T,R> extends BaseLinkedScanner<T,T>{

	private final Function<T,R> mapper;
	private boolean hasSetCurrent = false;

	public DeduplicatingScanner(Scanner<T> input, Function<T,R> mapper){
		super(input);
		this.mapper = mapper;
	}

	@Override
	public boolean advanceInternal(){
		while(input.advance()){
			if(!hasSetCurrent || !Objects.equals(mapper.apply(current), mapper.apply(input.current()))){
				current = input.current();
				hasSetCurrent = true;
				return true;
			}
		}
		return false;
	}

}
