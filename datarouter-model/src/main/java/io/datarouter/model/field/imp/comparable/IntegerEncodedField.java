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
package io.datarouter.model.field.imp.comparable;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.field.codec.IntegerFieldCodec;

public class IntegerEncodedField<T> extends BaseField<T>{

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;

	private final IntegerEncodedFieldKey<T> key;

	public IntegerEncodedField(IntegerEncodedFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}

	@Override
	public String getStringEncodedValue(){
		Integer integerValue = key.getCodec().encode(value);
		return integerValue == null ? null : Integer.toString(integerValue);
	}

	@Override
	public T parseStringEncodedValueButDoNotSet(String stringValue){
		if(stringValue == null){
			return null;
		}
		int intValue = Integer.valueOf(stringValue);
		return key.getCodec().decode(intValue);
	}

	@Override
	public byte[] getBytes(){
		Integer intValue = key.getCodec().encode(value);
		return intValue == null ? null : COMPARABLE_INT_CODEC.encode(intValue);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return COMPARABLE_INT_CODEC.length();
	}

	@Override
	public T fromBytesButDoNotSet(byte[] bytes, int offset){
		int intValue = COMPARABLE_INT_CODEC.decode(bytes, offset);
		return key.getCodec().decode(intValue);
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<T> other){
		return key.getCodec().getComparator().compare(value, other.getValue());
	}

	public IntegerFieldCodec<T> getCodec(){
		return key.getCodec();
	}

}
