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
package io.datarouter.storage.setting;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.string.StringTool;

public class SettingRoot extends SettingNode{

	private final Set<SettingNode> rootNodes = Collections.synchronizedSet(new LinkedHashSet<>());
	@SuppressWarnings("unused")
	private final SettingCategory category;

	public SettingRoot(SettingFinder finder, SettingCategory category, String name){
		super(finder, name);
		this.rootNodes.add(this);
		this.category = category;
	}

	private SettingRoot(SettingFinder finder, AdditionalSettingRootsFinder additionalSettingRootsFinder,
			SettingCategory category, String name){
		super(finder, name);
		additionalSettingRootsFinder.getAdditionalSettingRoots().forEach(this::dependsOn);
		this.category = category;
	}

	private void dependsOn(SettingRoot settingNode){
		rootNodes.add(settingNode);
		settingNode.rootNodes.forEach(rootNodes::add);
	}

	public Optional<SettingNode> getNode(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getNodeByName(nodeName);
			}
		}
		return Optional.empty();
	}

	public Optional<SettingNode> getMostRecentAncestorNode(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getMostRecentAncestorNodeByName(nodeName);
			}
		}
		return Optional.empty();
	}

	public List<SettingNode> getDescendants(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getDescendanceByName(nodeName);
			}
		}
		return null;
	}

	public List<SettingNode> getRootNodesSortedByShortName(){
		return rootNodes.stream()
				.sorted(Comparator.comparing(SettingNode::getShortName))
				.collect(Collectors.toList());
	}

	public Optional<CachedSetting<?>> getSettingByName(String name){
		return getNode(name.substring(0, name.lastIndexOf(".") + 1))
				.map(SettingNode::getSettings)
				.map(settings -> settings.get(name));
	}

	public boolean isRecognized(String settingName){
		String rootName = StringTool.getStringBeforeFirstOccurrence('.', settingName);
		return isRecognizedRootName(rootName);
	}

	public boolean isRecognizedRootName(String rootNameWithoutTrailingDot){
		return rootNodes.stream()
				.map(SettingNode::getShortName)
				.anyMatch(shortName -> shortName.equals(rootNameWithoutTrailingDot));
	}

	@Singleton
	public static class SettingRootFinder extends SettingRoot{

		@Inject
		private SettingRootFinder(SettingFinder finder, AdditionalSettingRootsFinder additionalSettingRootsFinder){
			super(finder, additionalSettingRootsFinder, DatarouterSettingCategory.DATAROUTER, "datarouter.");
		}

	}

}
