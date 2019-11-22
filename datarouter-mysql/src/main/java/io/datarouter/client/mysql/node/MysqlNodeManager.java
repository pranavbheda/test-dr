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
package io.datarouter.client.mysql.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlLiveTableOptionsRefresher;
import io.datarouter.client.mysql.execution.MysqlOpRetryTool;
import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.read.MysqlGetKeysOp;
import io.datarouter.client.mysql.op.read.MysqlGetOp;
import io.datarouter.client.mysql.op.read.MysqlGetOpExecutor;
import io.datarouter.client.mysql.op.read.MysqlGetPrimaryKeyRangesOp;
import io.datarouter.client.mysql.op.read.MysqlGetRangesOp;
import io.datarouter.client.mysql.op.read.MysqlLookupUniqueOp;
import io.datarouter.client.mysql.op.read.index.MysqlGetByIndexOp;
import io.datarouter.client.mysql.op.read.index.MysqlGetIndexOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetDatabeanRangesOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetKeyRangesOp;
import io.datarouter.client.mysql.op.read.index.MysqlManagedIndexGetRangesOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteAllOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteByIndexOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteOp;
import io.datarouter.client.mysql.op.write.MysqlPutOp;
import io.datarouter.client.mysql.op.write.MysqlUniqueIndexDeleteOp;
import io.datarouter.client.mysql.scan.MysqlDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexDatabeanScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexKeyScanner;
import io.datarouter.client.mysql.scan.MysqlManagedIndexScanner;
import io.datarouter.client.mysql.scan.MysqlPrimaryKeyScanner;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.IndexedStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;

@Singleton
public class MysqlNodeManager{

	@Inject
	private Datarouter datarouter;
	@Inject
	private MysqlFieldCodecFactory fieldCodecFactory;
	@Inject
	private MysqlGetOpExecutor mysqlGetOpExecutor;
	@Inject
	private MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	@Inject
	private MysqlClientType mysqlClientType;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private SessionExecutor sessionExecutor;
	@Inject
	public MysqlLiveTableOptionsRefresher mysqlLiveTableOptionsRefresher;

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> boolean exists(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, PK key, Config config){
		return CollectionTool.notEmpty(getKeys(fieldInfo, Collections.singleton(key), config));
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> D get(
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, PK key, Config config){
		return CollectionTool.getFirst(getMulti(fieldInfo, ListTool.wrap(key), config));
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<PK> getKeys(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getKeys;
		MysqlGetKeysOp<PK,D,F> op = new MysqlGetKeysOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, fieldInfo,
				opName, keys, config);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> getMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		String opName = MapStorageReader.OP_getMulti;
		MysqlGetOp<PK,D,F> op = new MysqlGetOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor, fieldInfo, opName,
				keys, config);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	D lookupUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		MysqlLookupUniqueOp<PK,D,F> op = new MysqlLookupUniqueOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor,
				fieldInfo, opName, ListTool.wrap(uniqueKey), config);
		List<D> result = sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
		if(CollectionTool.size(result) > 1){
			throw new DataAccessException("found >1 databeans with unique index key=" + uniqueKey);
		}
		return CollectionTool.getFirst(result);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> lookupMultiUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(CollectionTool.isEmpty(uniqueKeys)){
			return new LinkedList<>();
		}
		MysqlLookupUniqueOp<PK,D,F> op = new MysqlLookupUniqueOp<>(datarouter, fieldCodecFactory, mysqlGetOpExecutor,
				fieldInfo, opName, uniqueKeys, config);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		BaseMysqlOp<List<IE>> op = new MysqlGetIndexOp<>(datarouter, mysqlGetOpExecutor, fieldInfo, fieldCodecFactory,
				opName, config, indexEntryFieldInfo, keys);
		return sessionExecutor.runWithoutRetries(op);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getByIndex;
		BaseMysqlOp<List<D>> op = new MysqlGetByIndexOp<>(datarouter, fieldInfo, fieldCodecFactory,
				mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher, mysqlClientType, indexEntryFieldInfo,
				keys, config);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getIndexRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<IK>> ranges, Config config,
		IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexRange;
		MysqlManagedIndexGetRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetRangesOp<>(datarouter, fieldInfo,
				fieldCodecFactory, mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher, indexEntryFieldInfo,
				ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IK> getIndexKeyRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<IK>> ranges, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		MysqlManagedIndexGetKeyRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetKeyRangesOp<>(datarouter,
				fieldInfo, fieldCodecFactory, mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher,
				indexEntryFieldInfo, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getIndexDatabeanRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<IK>> ranges,
			Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getByIndexRange;
		MysqlManagedIndexGetDatabeanRangesOp<PK,D,F,IK,IE,IF> op = new MysqlManagedIndexGetDatabeanRangesOp<>(
				datarouter, fieldInfo, fieldCodecFactory, mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher,
				indexEntryFieldInfo, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<PK> getKeysInRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getKeysInRange;
		MysqlGetPrimaryKeyRangesOp<PK,D,F> op = new MysqlGetPrimaryKeyRangesOp<>(datarouter, fieldInfo,
				fieldCodecFactory, mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher, ranges, config,
				mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<D> getRanges(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getRange;
		MysqlGetRangesOp<PK,D,F> op = new MysqlGetRangesOp<>(datarouter, fieldInfo, fieldCodecFactory,
				mysqlPreparedStatementBuilder, mysqlLiveTableOptionsRefresher, ranges, config, mysqlClientType);
		return sessionExecutor.runWithoutRetries(op, getTraceName(fieldInfo.getNodeName(), opName));
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void put(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, D databean, Config config){
		MysqlPutOp<PK,D,F> op = new MysqlPutOp<>(datarouter, fieldInfo, this, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, ListTool.wrap(databean), config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void putMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<D> databeans, Config config){
		if(CollectionTool.isEmpty(databeans)){
			return;// avoid starting txn
		}
		MysqlPutOp<PK,D,F> op = new MysqlPutOp<>(datarouter, fieldInfo, this, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, databeans, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void deleteAll(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Config config){
		MysqlDeleteAllOp<PK,D,F> op = new MysqlDeleteAllOp<>(datarouter, fieldInfo, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void delete(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, PK key, Config config){
		MysqlDeleteOp<PK,D,F> op = new MysqlDeleteOp<>(datarouter, fieldInfo, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, ListTool.wrap(key), config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void deleteMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<PK> keys, Config config){
		if(CollectionTool.isEmpty(keys)){
			return;// avoid starting txn
		}
		MysqlDeleteOp<PK,D,F> op = new MysqlDeleteOp<>(datarouter, fieldInfo, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, keys, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, null), config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void deleteUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		MysqlUniqueIndexDeleteOp<PK,D,F> op = new MysqlUniqueIndexDeleteOp<>(datarouter, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, fieldInfo, ListTool.wrap(uniqueKey), config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, getTraceName(fieldInfo.getNodeName(), opName)),
				config);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void deleteMultiUnique(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<? extends UniqueKey<PK>> uniqueKeys,
			Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		if(CollectionTool.isEmpty(uniqueKeys)){
			return;// avoid starting txn
		}
		MysqlUniqueIndexDeleteOp<PK,D,F> op = new MysqlUniqueIndexDeleteOp<>(datarouter, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, fieldInfo, uniqueKeys, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, getTraceName(fieldInfo.getNodeName(), opName)),
				config);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>>
	void deleteByIndex(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<IK> keys, Config config){
		BaseMysqlOp<Long> op = new MysqlDeleteByIndexOp<>(datarouter, fieldInfo, mysqlPreparedStatementBuilder,
				mysqlLiveTableOptionsRefresher, keys, config);
		MysqlOpRetryTool.tryNTimes(sessionExecutor.makeCallable(op, IndexedStorageWriter.OP_deleteMultiUnique), config);
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges, Config config){
		return new MysqlManagedIndexScanner<>(this, indexEntryFieldInfo, fieldInfo, ranges, config, MysqlCollation
				.isCaseInsensitive(fieldInfo))
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges, Config config){
		return new MysqlManagedIndexDatabeanScanner<>(this, fieldInfo, indexEntryFieldInfo, ranges, config,
				MysqlCollation.isCaseInsensitive(fieldInfo))
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<Range<IK>> ranges, Config config){
		return new MysqlManagedIndexKeyScanner<>(this, fieldInfo, indexEntryFieldInfo,
				ranges, config, MysqlCollation.isCaseInsensitive(fieldInfo))
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	Scanner<PK> scanKeysMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges,
			Config config){
		return new MysqlPrimaryKeyScanner<>(this, fieldInfo, ranges, config, MysqlCollation.isCaseInsensitive(
				fieldInfo))
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	@SuppressWarnings("resource")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	Scanner<D> scanMulti(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, Collection<Range<PK>> ranges, Config config){
		return new MysqlDatabeanScanner<>(this, fieldInfo, ranges, config, MysqlCollation
				.isCaseInsensitive(fieldInfo))
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>>
	N registerManaged(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, N managedNode){
		return managedNodesHolder.registerManagedNode(fieldInfo, managedNode);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	List<ManagedNode<PK,D,?,?,?>> getManagedNodes(PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo){
		return managedNodesHolder.getManagedNodes(fieldInfo);
	}

	private static String getTraceName(String nodeName, String opName){
		return nodeName + " " + opName;
	}

}
