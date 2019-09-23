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
package io.datarouter.util.web;

public class HtmlSelectOptionBean{

	public HtmlSelectOptionBean(String name, String value){
		this.name = name;
		this.value = value;
	}

	private String name;
	private String value;
	private boolean selected = false;

	@Override
	public String toString(){
		return "<option value=\"" + value + "\"" + (selected ? " selected" : "") + ">" + name + "</option>";
	}

	public String getName(){
		return name;
	}

	public String getValue(){
		return value;
	}

}
