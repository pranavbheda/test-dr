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
package io.datarouter.client.mysql.field.codec.list;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import io.datarouter.bytes.LongArray;
import io.datarouter.bytes.codec.array.longarray.RawLongArrayCodec;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.field.codec.base.BaseListMysqlFieldCodec;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.imp.list.RawLongListField;

public class RawLongListMysqlFieldCodec
extends BaseListMysqlFieldCodec<Long,List<Long>,RawLongListField>{

	private static final RawLongArrayCodec RAW_LONG_ARRAY_CODEC = RawLongArrayCodec.INSTANCE;

	public RawLongListMysqlFieldCodec(RawLongListField field){
		super(field);
	}

	@Override
	public SqlColumn getSqlColumnDefinition(boolean allowNullable){
		return new SqlColumn(
				field.getKey().getColumnName(),
				getMysqlColumnType(),
				Integer.MAX_VALUE,
				allowNullable && field.getKey().isNullable(),
				false);
	}

	@Override
	public void setPreparedStatementValue(PreparedStatement ps, int parameterIndex){
		byte[] bytes = null;
		if(field.getValue() != null){
			LongArray longArray = field.getValue() instanceof LongArray
					? (LongArray)field.getValue()
					: new LongArray(field.getValue());
			bytes = RAW_LONG_ARRAY_CODEC.encode(longArray.getPrimitiveArray());
		}
		try{
			ps.setBytes(parameterIndex, bytes);
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public List<Long> fromMysqlResultSetButDoNotSet(ResultSet rs){
		try{
			byte[] bytes = rs.getBytes(field.getKey().getColumnName());
			return new LongArray(RAW_LONG_ARRAY_CODEC.decode(bytes));
		}catch(SQLException e){
			throw new DataAccessException(e);
		}
	}

	@Override
	public MysqlColumnType getMysqlColumnType(){
		return MysqlColumnType.LONGBLOB;
	}

}