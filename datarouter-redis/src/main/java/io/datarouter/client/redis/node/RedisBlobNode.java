package io.datarouter.client.redis.node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import io.datarouter.client.redis.RedisBlobCodec;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.client.RedisOps;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.InputStreamTool;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.tuple.Twin;
import io.lettuce.core.KeyValue;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public class RedisBlobNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalBlobStorageNode<PK,D,F>{

	private final ClientId clientId;
	private final String bucket;
	private final Subpath rootPath;
	private final Integer schemaVersion;
	private final RedisBlobCodec<PK,D,F> codec;
	private final RedisClientManager redisClientManager;
	private final RedisOps ops;

	public RedisBlobNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			RedisClientManager redisClientManager){
		super(params, clientType);
		this.clientId = params.getClientId();
		this.bucket = params.getPhysicalName();
		this.rootPath = params.getPath();
		this.schemaVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new RedisBlobCodec<>(getFieldInfo(), schemaVersion);
		this.redisClientManager = redisClientManager;
		this.ops = new RedisOps(client());
	}

	private RedisClusterAsyncCommands<byte[],byte[]> client(){
		return redisClientManager.getClient(clientId);
	}

	/*------------- BlobStorageReader --------------*/

	@Override
	public String getBucket(){
		return bucket;
	}

	@Override
	public Subpath getRootPath(){
		return rootPath;
	}

	@Override
	public boolean exists(PathbeanKey key){
		return ops.exists(codec.encodeKey(key));
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		byte[] byteKey = codec.encodeKey(key);
		return ops.find(byteKey)
				.map(value -> value.length)
				.map(Integer::longValue);
	}

	@Override
	public byte[] read(PathbeanKey key){
		byte[] byteKey = codec.encodeKey(key);
		return ops.find(byteKey).orElse(null);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		byte[] byteKey = codec.encodeKey(key);
		return ops.mget(List.of(byteKey))
				.findFirst()
				.map(KeyValue::getValue)
				.map(bytes -> ByteTool.copyOfRange(bytes, (int)offset, length))
				.orElse(null);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(PathbeanKey key, byte[] value){
		ops.set(Twin.of(codec.encodeKey(key), value));
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		byte[] bytes = chunks.listTo(ByteTool::concatenate);
		ops.set(Twin.of(codec.encodeKey(key), bytes));
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		var baos = new ByteArrayOutputStream();
		InputStreamTool.transfer(inputStream, baos);
		write(key, baos.toByteArray());
	}

	@Override
	public void delete(PathbeanKey key){
		ops.del(codec.encodeKey(key));
	}

	@Override
	public void deleteAll(Subpath subpath){
		throw new UnsupportedOperationException();
	}

}
