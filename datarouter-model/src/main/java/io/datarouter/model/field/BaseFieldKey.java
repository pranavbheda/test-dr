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
package io.datarouter.model.field;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public abstract class BaseFieldKey<T,K extends BaseFieldKey<T,K>>
implements FieldKey<T>{

	protected final String name;// the name of the java field
	protected final String columnName;// defaults to name if not specified
	protected final boolean nullable;
	protected final FieldGeneratorType fieldGeneratorType;
	protected final T defaultValue;
	protected final TypeToken<T> valueTypeToken;
	protected final Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes;

	protected BaseFieldKey(String name, TypeToken<T> valueType){
		this(name, name, true, valueType, FieldGeneratorType.NONE, null, new HashMap<>());
	}

	protected BaseFieldKey(
			String name,
			String columnName,
			boolean nullable,
			TypeToken<T> valueType,
			FieldGeneratorType fieldGeneratorType,
			T defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		this.name = name;
		this.columnName = columnName;
		this.nullable = nullable;
		this.attributes = attributes;
		this.valueTypeToken = valueType;
		this.fieldGeneratorType = fieldGeneratorType;
		this.defaultValue = defaultValue;
	}

	/*---------------------------- methods ----------------------------------*/

	@Override
	public String toString(){
		return "[" + getClass().getSimpleName() + ":" + name + "]";
	}

	@Override
	public boolean isFixedLength(){
		return true;
	}

	@Override
	public boolean isCollection(){
		return false;
	}

	@Override
	public boolean isPossiblyCaseInsensitive(){
		return false;
	}

	//don't cache this until we are using keys where it would be allocated on every equals/hashCode/compareTo
	@Override
	public byte[] getColumnNameBytes(){
		return StringCodec.UTF_8.encode(columnName);
	}

	@Override
	public T getDefaultValue(){
		return defaultValue;
	}

	@Override
	public Type getValueType(){
		return valueTypeToken.getType();
	}

	@Override
	public Optional<String> findDocString(){
		return Optional.empty();
	}

	/*---------------------------- get/set ----------------------------------*/

	@Override
	public String getName(){
		return name;
	}

	@Override
	public String getColumnName(){
		return columnName;
	}

	@Override
	public boolean isNullable(){
		return nullable;
	}

	@Override
	public Optional<Integer> findSize(){
		return Optional.empty();
	}

	@Override
	public FieldGeneratorType getAutoGeneratedType(){
		return fieldGeneratorType;
	}

	@Override
	public T generateRandomValue(){
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public final <V> K with(FieldKeyAttribute<V> attribute){
		attributes.put(attribute.getKey(), attribute);
		return (K)this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <U extends FieldKeyAttribute<U>> Optional<U> findAttribute(FieldKeyAttributeKey<U> key){
		return Optional.ofNullable((U)attributes.get(key));
	}

	@Override
	public Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> getAttributes(){
		return attributes;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof BaseFieldKey)){
			return false;
		}
		BaseFieldKey<?,?> otherKey = (BaseFieldKey<?,?>)obj;
		return Objects.equals(name, otherKey.name)
				&& Objects.equals(columnName, otherKey.columnName)
				&& Objects.equals(nullable, otherKey.nullable)
				&& Objects.equals(fieldGeneratorType, otherKey.fieldGeneratorType)
				&& Objects.equals(defaultValue, otherKey.defaultValue);
	}

	/*---------------------------- avro generic ----------------------------------*/

	@Override
	public Type getGenericType(){
		return getValueType();
	}

}
