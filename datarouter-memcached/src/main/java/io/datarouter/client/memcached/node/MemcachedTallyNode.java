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
package io.datarouter.client.memcached.node;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.client.memcached.client.DatarouterMemcachedClient;
import io.datarouter.client.memcached.codec.MemcachedTallyCodec;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.client.memcached.util.MemcachedResult;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class MemcachedTallyNode
extends BasePhysicalNode<TallyKey,Tally,TallyFielder>
implements PhysicalTallyStorageNode{

	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);

	private final Supplier<DatarouterMemcachedClient> lazyClient;
	private final MemcachedTallyCodec tallyCodec;

	public MemcachedTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> params,
			ClientType<?,?> clientType,
			MemcachedTallyCodec tallyCodec,
			Supplier<DatarouterMemcachedClient> lazyClient){
		super(params, clientType);
		this.tallyCodec = tallyCodec;
		this.lazyClient = lazyClient;
	}

	/*------------- TallyStorage -------------*/

	@Override
	public Long incrementAndGetCount(String tallyStringKey, int delta, Config config){
		return tallyCodec.encodeKeyIfValid(tallyStringKey)
				.map(encodedKey -> lazyClient.get().increment(
						encodedKey,
						delta,
						MemcachedExpirationTool.getExpirationSeconds(config),
						config.findIgnoreException().orElse(DEFAULT_IGNORE_EXCEPTION)))
				.orElse(0L);
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		return Optional.ofNullable(getMultiTallyCount(List.of(key), config).get(key));
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> tallyStringKeys, Config config){
		return Scanner.of(tallyStringKeys)
				.map(tallyCodec::encodeKeyIfValid)
				.concat(OptionalScanner::of)
				.listTo(memcachedStringKeys -> lazyClient.get().scanMultiStrings(
						getName(),
						memcachedStringKeys,
						config.findTimeout().orElse(DEFAULT_TIMEOUT).toMillis(),
						config.findIgnoreException().orElse(DEFAULT_IGNORE_EXCEPTION)))
				.map(tallyCodec::decodeResult)
				.toMap(MemcachedResult::key, MemcachedResult::value);
	}

	@Override
	public void deleteTally(String tallyStringKey, Config config){
		tallyCodec.encodeKeyIfValid(tallyStringKey)
				.ifPresent(memcachedStringKey -> lazyClient.get().deleteTally(
						getName(),
						memcachedStringKey,
						config.findTimeout().orElse(DEFAULT_TIMEOUT),
						config.findIgnoreException().orElse(DEFAULT_IGNORE_EXCEPTION)));
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
