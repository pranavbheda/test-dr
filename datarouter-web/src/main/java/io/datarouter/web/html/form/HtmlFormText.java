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
package io.datarouter.web.html.form;

import java.util.Optional;
import java.util.function.Function;

import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;

public class HtmlFormText extends BaseHtmlFormField{

	private String name;
	private String display;
	private String placeholder;
	private String value;
	private boolean required;
	private boolean readOnly;
	private boolean submitOnChange;

	public HtmlFormText withName(String name){
		this.name = name;
		return this;
	}

	public HtmlFormText withDisplay(String display){
		this.display = display;
		return this;
	}

	public HtmlFormText withPlaceholder(String placeholder){
		this.placeholder = placeholder;
		return this;
	}

	public HtmlFormText withPlaceholder(Number placeholder){
		this.placeholder = placeholder.toString();
		return this;
	}

	public HtmlFormText withValue(String value){
		this.value = value;
		return this;
	}

	public HtmlFormText withValue(
			String value,
			boolean shouldValidate,
			Function<String,Optional<String>> errorFinder){
		this.value = value;
		if(shouldValidate){
			errorFinder.apply(value).ifPresent(this::withError);
		}
		return this;
	}

	public HtmlFormText readOnly(){
		this.readOnly = true;
		return this;
	}

	public HtmlFormText required(){
		this.required = true;
		return this;
	}

	public HtmlFormText withSubmitOnChange(){
		this.submitOnChange = true;
		return this;
	}

	public HtmlFormText withError(String error){
		this.error = error;
		return this;
	}

	public String getName(){
		return name;
	}

	public String getDisplay(){
		return display;
	}

	public String getPlaceholder(){
		return placeholder;
	}

	public String getValue(){
		return value;
	}

	public boolean isRequired(){
		return required;
	}

	public boolean isReadOnly(){
		return readOnly;
	}

	public boolean isSubmitOnChange(){
		return submitOnChange;
	}

}
