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
package io.datarouter.web.html.j2html;

import static j2html.TagCreator.head;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.title;

import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

public class DatarouterPageHead{

	private final EmptyTag[] datarouterWebCssImports;
	private final ContainerTag datarouterWebRequireJsImport;
	private final ContainerTag datarouterWebRequireJsConfig;
	private final ContainerTag webappRequireJsConfig;
	private final ContainerTag requireScript;
	private final EmptyTag[] datarouterNavbarCssImports;
	private final ContainerTag datarouterNavbarRequestTimingJsImport;
	private final ContainerTag datarouterNavbarRequestTimingScript;
	private final String title;

	public DatarouterPageHead(
			EmptyTag[] datarouterWebCssImports,
			ContainerTag datarouterWebRequireJsImport,
			ContainerTag datarouterWebRequireJsConfig,
			ContainerTag webappRequireJsConfig,
			ContainerTag requireScript,
			EmptyTag[] datarouterNavbarCssImports,
			ContainerTag datarouterNavbarRequestTimingJsImport,
			ContainerTag datarouterNavbarRequestTimingScript,
			String title){
		this.datarouterWebCssImports = datarouterWebCssImports;
		this.datarouterWebRequireJsImport = datarouterWebRequireJsImport;
		this.datarouterWebRequireJsConfig = datarouterWebRequireJsConfig;
		this.webappRequireJsConfig = webappRequireJsConfig;
		this.requireScript = requireScript;
		this.datarouterNavbarCssImports = datarouterNavbarCssImports;
		this.datarouterNavbarRequestTimingJsImport = datarouterNavbarRequestTimingJsImport;
		this.datarouterNavbarRequestTimingScript = datarouterNavbarRequestTimingScript;
		this.title = title;
	}

	public ContainerTag build(){
		var head = head();
		var meta = meta()
				.withName("viewport")
				.withContent("width=device-width, initial-scale=1");
		var titleTag = title(title);
		return head
				.with(meta)
				.with(datarouterWebCssImports)
				.with(datarouterWebRequireJsImport)
				.with(datarouterWebRequireJsConfig)
				.with(webappRequireJsConfig)
				.with(requireScript)
				.with(datarouterNavbarCssImports)
				.with(datarouterNavbarRequestTimingJsImport)
				.with(datarouterNavbarRequestTimingScript)
				.with(titleTag);
	}

}
