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
package io.datarouter.client.memcached.web;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.memcached.MemcachedClientType;
import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedOptions;
import io.datarouter.client.memcached.client.SpyMemcachedClient;
import io.datarouter.storage.client.ClientId;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class MemcachedWebInspector implements DatarouterClientWebInspector{

	@Inject
	private MemcachedOptions memcachedOptions;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private MemcachedClientManager memcachedClientManager;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ServletContextSupplier servletContext;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, MemcachedClientType.class);
		SpyMemcachedClient spyClient = memcachedClientManager.getSpyMemcachedClient(clientParams.getClientId());
		String clientName = clientParams.getClientId().getName();
		var content = div(
				h2("Datarouter " + clientName),
				DatarouterClientWebInspector.buildNav(servletContext.get().getContextPath(), clientName),
				h3("Client Summary"),
				buildOverview(clientParams.getClientId()),
				buildStats(spyClient.getStats()))
				.withClass("container my-3");

		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Client - Memcached")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	public ContainerTag buildStats(Map<SocketAddress,Map<String,String>> stats){
		var allStats = div();
		stats.entrySet().stream()
				.map(entry -> buildSingleNodeStats(entry.getKey().toString(), entry.getValue()))
				.forEach(allStats::with);
		return allStats;
	}

	private ContainerTag buildSingleNodeStats(String node, Map<String,String> stats){
		var tbody = TagCreator.tbody();
		stats.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.map(entry -> tr(th(entry.getKey()), td(entry.getValue())))
				.forEach(tbody::with);
		var table = table(tbody).withClass("table table-striped table-hover table-sm");
		return div(h4(node), table);
	}

	private ContainerTag buildOverview(ClientId clientId){
		List<ContainerTag> listElements = memcachedOptions.getServers(clientId.getName()).stream()
				.map(InetSocketAddress::toString)
				.map(TagCreator::li)
				.collect(Collectors.toList());
		var nodeList = TagCreator.ul(listElements.toArray(new ContainerTag[listElements.size()]));
		return dl(
				dt("Number of nodes:"), dd(listElements.size() + ""),
				dt("Nodes:"), dd(nodeList));
	}

}