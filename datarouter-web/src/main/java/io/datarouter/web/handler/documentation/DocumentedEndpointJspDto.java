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
package io.datarouter.web.handler.documentation;

import java.util.List;

public class DocumentedEndpointJspDto{

	private final String url;
	private final List<DocumentedParameterJspDto> parameters;
	private final String description;
	private final DocumentedResponseJspDto response;

	public DocumentedEndpointJspDto(String url, List<DocumentedParameterJspDto> parameters, String description,
			DocumentedResponseJspDto response){
		this.url = url;
		this.parameters = parameters;
		this.description = description;
		this.response = response;
	}

	public String getDescription(){
		return description;
	}

	public String getUrl(){
		return url;
	}

	public List<DocumentedParameterJspDto> getParameters(){
		return parameters;
	}

	public DocumentedResponseJspDto getResponse(){
		return response;
	}

}