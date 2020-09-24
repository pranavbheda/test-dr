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
package io.datarouter.metric.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.metric.dto.MetricDashboardDto;
import io.datarouter.metric.dto.MetricName;
import io.datarouter.metric.dto.MiscMetricLinksDto;
import io.datarouter.metric.links.MetricDashboardRegistry;
import io.datarouter.metric.links.MetricLinkBuilder;
import io.datarouter.metric.links.MiscMetricsLinksRegistry;
import io.datarouter.metric.types.MetricNameType;
import io.datarouter.metric.types.MetricType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class ViewMetricNameService{

	@Inject
	private DatarouterClients clients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;
	@Inject
	private RouteSetRegistry routeSetRegistry;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private MetricDashboardRegistry dashboardRegistry;
	@Inject
	private MiscMetricsLinksRegistry miscMetricLinksRegistry;

	public ContainerTag makeMetricNameTable(String header, List<MetricName> rows){
		if(rows.size() == 0){
			return div();
		}
		var h2 = h2(header);
		rows.sort(Comparator.comparing(metricName -> metricName.displayName));
		var table = new J2HtmlTable<MetricName>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Metric Name").withClass("w-50"), row -> td(row.displayName))
				.withHtmlColumn(th("Type").withClass("w-25"), row -> td(row.nameType.type))
				.withHtmlColumn(th("").withClass("w-25"), this::getMetricNameLink)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	private DomContent getMetricNameLink(MetricName metricName){
		String link;
		if(metricName.nameType == MetricNameType.AVAILABLE){
			link = linkBuilder.availableMetricsLink(metricName.getNameOrPrefix());
		}else{
			link = linkBuilder.exactMetricLink(metricName.getNameOrPrefix(), metricName.metricType);
		}
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link)
				.withTarget("_blank"));
	}

	public List<MetricName> getNodeTableMetricNames(boolean showSystemInfo){
		return Scanner.of(clients.getClientIds())
				.include(ClientId::getWritable)
				.map(ClientId::getName)
				.map(datarouterNodes::getPhysicalNodesForClient)
				.concat(Scanner::of)
				.map(PhysicalNode::getFieldInfo)
				.include(fieldInfo -> {
					if(showSystemInfo){
						return fieldInfo.getIsSystemTable();
					}
					return !fieldInfo.getIsSystemTable();
				})
				.map(fieldInfo -> {
					String prefix = "Datarouter node "
							+ clients.getClientTypeInstance(fieldInfo.getClientId()).getName()
							+ " "
							+ fieldInfo.getClientId().getName()
							+ " "
							+ fieldInfo.getNodeName();
					return MetricName.availableMetric(fieldInfo.getNodeName(), prefix);
				})
				.list();
	}

	public List<MetricName> getJobMetricNames(boolean showSystemInfo){
		return Scanner.of(injector.getInstances(triggerGroupClasses.get()))
				.include(triggerGroup -> {
					if(showSystemInfo){
						return triggerGroup.isSystemTriggerGoup;
					}
					return !triggerGroup.isSystemTriggerGoup;
				})
				.map(BaseTriggerGroup::getJobPackages)
				.concat(Scanner::of)
				.map(JobPackage::toString)
				.map(name -> {
					String prefix = "Datarouter job " + name;
					return MetricName.availableMetric(name, prefix);
				})
				.list();
	}

	public ContainerTag getHandlerMetricNames(String title, boolean showSystemInfo){
		var list = Scanner.of(routeSetRegistry.get())
				.map(BaseRouteSet::getDispatchRules)
				.concat(Scanner::of)
				.include(dispatchRule -> {
					if(showSystemInfo){
						return dispatchRule.isSystemDispatchRule();
					}
					return !dispatchRule.isSystemDispatchRule();
				})
				.map(DispatchRule::getHandlerClass)
				.map(Class::getSimpleName)
				.distinct()
				.concat(Scanner::of)
				.sorted()
				.list();
		if(list.size() == 0){
			return div();
		}
		var h2 = h2(title);
		var table = new J2HtmlTable<String>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Handlers").withClass("w-50"), row -> td(row))
				.withHtmlColumn(th("Exact").withClass("w-25"), row -> td(a("Class")
						.withHref(linkBuilder.exactMetricLink("Datarouter handler class " + row, MetricType.COUNT))
						.withTarget("_blank")))
				.withHtmlColumn(th("Available").withClass("w-25"), row -> td(a("Endpoints")
						.withHref(linkBuilder.availableMetricsLink("Datarouter handler method " + row))
						.withTarget("_blank")))
				.withCaption("Total " + list.size())
				.build(list);
		return div(h2, table)
				.withClass("container my-4");
	}

	public ContainerTag getDashboardsTable(){
		var dasboards = dashboardRegistry.dashboards;
		if(dasboards.size() == 0){
			return div();
		}
		var h2 = h2("Metric Dashboards");
		dasboards.sort(Comparator.comparing(metricName -> metricName.displayName));
		var table = new J2HtmlTable<MetricDashboardDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Dashboard Name").withClass("w-50"), row -> td(row.displayName))
				.withHtmlColumn(th("").withClass("w-25"), this::getDashboardLink)
				.build(dasboards);
		return div(h2, table)
				.withClass("container my-4");
	}

	private DomContent getDashboardLink(MetricDashboardDto dashboard){
		String link = linkBuilder.dashboardLink(dashboard.id);
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link)
				.withTarget("_blank"));
	}

	public ContainerTag miscMetricLinksTable(){
		var links = miscMetricLinksRegistry.miscMetricLinks;
		if(links.size() == 0){
			return div();
		}
		var h2 = h2("Misc Metric Links");
		links.sort(Comparator.comparing(dto -> dto.display));
		var table = new J2HtmlTable<MiscMetricLinksDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Metric Name").withClass("w-50"), row -> td(row.display))
				.withHtmlColumn(th("").withClass("w-25"), row -> {
					return td(a(i().withClass("fa fa-link"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(row.link)
							.withTarget("_blank"));
				})
				.build(links);
		return div(h2, table)
				.withClass("container my-4");
	}

}
