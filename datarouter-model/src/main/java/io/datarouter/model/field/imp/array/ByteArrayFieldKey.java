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
package io.datarouter.model.field.imp.array;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayFieldKey extends BaseFieldKey<byte[]>{

	private final int size;

	public ByteArrayFieldKey(String name){
		super(name, byte[].class);
		this.size = CommonFieldSizes.MAX_KEY_LENGTH;
	}

	private ByteArrayFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			byte[] defaultValue, int size){
		super(name, columnName, nullable, byte[].class, fieldGeneratorType, defaultValue);
		this.size = size;
	}

	public ByteArrayFieldKey withSize(int size){
		return new ByteArrayFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

	/**************************** get/set ****************************/

	public int getSize(){
		return size;
	}

	@Override
	public ByteArrayField createValueField(final byte[] value){
		return new ByteArrayField(this, value);
	}
}