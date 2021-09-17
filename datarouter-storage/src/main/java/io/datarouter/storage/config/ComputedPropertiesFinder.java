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
package io.datarouter.storage.config;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.Require;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;

@Singleton
public class ComputedPropertiesFinder{
	private static final Logger logger = LoggerFactory.getLogger(ComputedPropertiesFinder.class);

	private static final String JVM_ARG_PREFIX = "datarouter.";
	private static final String CONFIG_DIRECTORY_PROP = "config.directory";
	public static final String SERVER_CONFIG_FILE_NAME = "server.properties";

	private final String configDirectory;
	private final String configFileLocation;

	// delete
	private final Properties allComputedServerProperties;
	private Optional<Properties> propertiesFromConfigFile = Optional.empty();

	public ComputedPropertiesFinder(){
		this(
				ConfigDirectoryConstants.getConfigDirectory(),
				true,
				false,
				SERVER_CONFIG_FILE_NAME,
				true);
	}

	private ComputedPropertiesFinder(
			String configDirectory,
			boolean directoryRequired,
			boolean directoryFromJvmArg,
			String filename,
			boolean fileRequired){
		boolean fileRequiredWithoutDirectoryRequired = fileRequired && !directoryRequired;
		Require.isTrue(!fileRequiredWithoutDirectoryRequired, "directory is required if file is required");

		this.configDirectory = validateConfigDirectory(configDirectory, directoryRequired, directoryFromJvmArg);
		this.configFileLocation = findConfigFileLocation(filename, fileRequired);

		this.allComputedServerProperties = new Properties();
	}

	// delete
	public Properties getAllComputedServerProperties(){
		return allComputedServerProperties;
	}

	/*--------------- methods to find config values -----------------*/

	public String validateConfigDirectory(String configDirectory, boolean directoryRequired,
			boolean directoryFromJvmArg){
		if(configDirectory != null){
			FileTool.createFileParents(configDirectory + "/anything");
			if(directoryFromJvmArg){
				logJvmArgSource(CONFIG_DIRECTORY_PROP, configDirectory, JVM_ARG_PREFIX + CONFIG_DIRECTORY_PROP);
			}else{
				logSource("config directory", configDirectory, ConfigDirectoryConstants.getSource());
			}
		}else{
			Require.isTrue(!directoryRequired, "config directory required but not found");
		}
		return configDirectory;
	}

	public String findConfigFileLocation(String filename, boolean fileRequired){
		String configFileLocation = null;
		if(StringTool.isEmpty(filename)){
			Require.isTrue(!fileRequired);
		}else{
			configFileLocation = configDirectory + "/" + filename;
			if(Files.notExists(Paths.get(configFileLocation))){
				logger.error("couldn't find config file {}", configFileLocation);
			}else{
				logger.warn("found config file {}", configFileLocation);
				try{
					propertiesFromConfigFile = Optional.of(PropertiesTool.parse(configFileLocation));
					logConfigFileProperties();
				}catch(Exception e){
					logger.error("couldn't parse config file {}", configFileLocation);
				}
			}
		}
		return configFileLocation;
	}

	public String findProperty(String propertyName){
		return findProperty(propertyName, List.of());
	}

	public String findProperty(String propertyName, Supplier<String> defaultValue){
		return findProperty(propertyName, List.of(new FallbackPropertyValueSupplierDto(defaultValue)));
	}

	public String findProperty(String propertyName, Supplier<String> defaultValueSupplier, String defaultSource){
		return findProperty(
				propertyName,
				List.of(new FallbackPropertyValueSupplierDto(defaultSource, defaultValueSupplier)));
	}

	/*
	 * finds a property value, logs the result (and its source when available), sets it in allComputedServerProperties,
	 * and returns it. sources are used in the following order until a non-empty one is found:
	 *
	 * 1. jvmArg
	 * 2. properties file
	 * 3. caller-defined defaults
	 * 4. empty (sets value to "" in allComputedServerProperties and returns null)
	 */
	public String findProperty(
			String propertyName,
			List<FallbackPropertyValueSupplierDto> defaultValueSupplierDtos){
		Optional<Pair<String,String>> propertyValueBySource = getPropFromJvmArg(propertyName)
				.or(() -> getPropFromConfigFile(propertyName))
				.or(() -> getPropFromDefaults(propertyName, defaultValueSupplierDtos));
		if(propertyValueBySource.isPresent() && !propertyValueBySource.get().getLeft().isEmpty()){
			//successfully found property name and non-empty value
			allComputedServerProperties.setProperty(propertyName, propertyValueBySource.get().getLeft());
			return propertyValueBySource.get().getLeft();
		}

		if(propertyValueBySource.isPresent()){
			//property name is found, but the value is empty
			logger.warn("found {} with empty value from {}", propertyName, propertyValueBySource.get().getRight());
		}else{
			//both name and value are unknown
			logger.warn("couldn't find " + propertyName + ", no default provided");
		}
		allComputedServerProperties.setProperty(propertyName, "");
		return null;
	}

	private Optional<Pair<String,String>> getPropFromConfigFile(String propertyName){
		Optional<String> propertyValue = propertiesFromConfigFile
				.map(properties -> properties.getProperty(propertyName));
		if(propertyValue.isEmpty()){
			return Optional.empty();
		}
		if(!propertyValue.get().isEmpty()){
			logSource(propertyName, propertyValue.get(), configFileLocation);
		}
		return Optional.of(new Pair<>(propertyValue.get(), configFileLocation));
	}

	private Optional<Pair<String,String>> getPropFromJvmArg(String jvmArg){
		String jvmArgName = JVM_ARG_PREFIX + jvmArg;
		String jvmArgValue = System.getProperty(jvmArgName);
		if(jvmArgValue == null){
			return Optional.empty();
		}
		if(!jvmArgValue.isEmpty()){
			logJvmArgSource(jvmArg, jvmArgValue, jvmArgName);
		}
		return Optional.of(new Pair<>(jvmArgValue, jvmArgName));
	}

	private Optional<Pair<String,String>> getPropFromDefaults(String propertyName,
			List<FallbackPropertyValueSupplierDto> defaultValueSupplierDtos){
		var optionalValueAndSource = defaultValueSupplierDtos.stream()
				.map(dto -> new Pair<>(dto.fallbackSupplier.get(), dto.propertySource))
				//supplied value should only be used if it is not null
				.filter(valueAndSource -> valueAndSource.getLeft() != null)
				.findFirst();
		if(optionalValueAndSource.isPresent()){
			var valueAndSource = optionalValueAndSource.get();
			logSource(propertyName, valueAndSource.getLeft(), valueAndSource.getRight());
			return optionalValueAndSource;
		}
		return Optional.empty();
	}

	private void logConfigFileProperties(){
		Properties allProperties = propertiesFromConfigFile.orElseGet(Properties::new);
		allProperties.stringPropertyNames().stream()
				.map(name -> name + "=" + allProperties.getProperty(name))
				.sorted()
				.forEach(logger::info);
	}

	private void logSource(String name, String value, String source){
		logger.warn("found {}={} from {}", name, value, source);
	}

	private void logJvmArgSource(String name, String value, String jvmArgName){
		logger.warn("found {}={} from -D{} JVM arg", name, value, jvmArgName);
	}

	public List<String> findPropertyStringsSplitWithComma(String propertyName){
		String propertyValue = findProperty(propertyName);
		if(StringTool.isNullOrEmptyOrWhitespace(propertyValue)){
			return List.of();
		}
		return Stream.of(propertyValue.split(","))
				.filter(StringTool::notEmptyNorWhitespace)
				.map(String::trim)
				.collect(Collectors.toUnmodifiableList());
	}

	public static class FallbackPropertyValueSupplierDto{

		public final String propertySource;
		public final Supplier<String> fallbackSupplier;

		public FallbackPropertyValueSupplierDto(Supplier<String> fallbackSupplier){
			this("default", fallbackSupplier);
		}

		public FallbackPropertyValueSupplierDto(String propertySource, Supplier<String> fallbackSupplier){
			Require.notNull(fallbackSupplier);
			Require.isTrue(StringTool.notNullNorEmptyNorWhitespace(propertySource));
			this.propertySource = propertySource;
			this.fallbackSupplier = fallbackSupplier;
		}

	}

}
