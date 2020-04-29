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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SortedBeans{

	public static final String S_aardvark = "aardvark";
	public static final String S_albatross = "albatross";
	public static final String S_alpaca = "alpaca";
	public static final String S_chinchilla = "chinchilla";
	public static final String S_emu = "emu";
	public static final String S_gopher = "gopher";
	public static final String S_ostrich = "ostrich";
	public static final String S_pelican = "pelican";

	public static final SortedSet<String> STRINGS = new TreeSet<>(Arrays.asList(
			S_aardvark,
			S_albatross,
			S_alpaca,
			S_chinchilla,
			S_emu,
			S_gopher,
			S_ostrich,
			S_pelican));

	public static final String PREFIX_a = "a";
	public static final int NUM_PREFIX_a = 3;

	public static final String PREFIX_ch = "ch";
	public static final int NUM_PREFIX_ch = 1;

	public static final String RANGE_al = "al";
	public static final String RANGE_alp = "alp";
	public static final String RANGE_emu = "emu";

	public static final int RANGE_LENGTH_alp = 6;
	public static final int RANGE_LENGTH_al_b = 2;
	// exclude things that begin with emu without the other 3 key fields
	public static final int RANGE_LENGTH_alp_emu_inc = 3;
	public static final int RANGE_LENGTH_emu = 4;

	public static final int NUM_ELEMENTS = STRINGS.size();
	public static final List<Integer> INTEGERS = new ArrayList<>(NUM_ELEMENTS);
	public static final int TOTAL_RECORDS = NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;

	static{
		for(int i = 0; i < NUM_ELEMENTS; ++i){
			INTEGERS.add(i);
		}
	}

	public static List<SortedBean> generatedSortedBeans(){
		List<String> as = new ArrayList<>(STRINGS);
		List<String> bs = new ArrayList<>(STRINGS);
		List<Integer> cs = new ArrayList<>(INTEGERS);
		List<String> ds = new ArrayList<>(STRINGS);
		//shuffle them for fun.  they should end up sorted in the table
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);

		List<SortedBean> beans = new ArrayList<>();//save in periodic batches
		for(int a = 0; a < NUM_ELEMENTS; ++a){
			for(int b = 0; b < NUM_ELEMENTS; ++b){
				for(int c = 0; c < NUM_ELEMENTS; ++c){
					for(int d = 0; d < NUM_ELEMENTS; ++d){
						SortedBean bean = new SortedBean(
								as.get(a), bs.get(b), cs.get(c), ds.get(d),
								"v1",//include at least one non-null value field
								null,
								null,
								null);
						beans.add(bean);
					}
				}
			}
		}
		return beans;
	}

	public static List<SortedBeanEntityKey> generateEntityKeys(){
		return generatedSortedBeans().stream()
				.map(SortedBean::getKey)
				.map(SortedBeanKey::getEntityKey)
				.distinct()
				.sorted()
				.collect(Collectors.toList());
	}

}