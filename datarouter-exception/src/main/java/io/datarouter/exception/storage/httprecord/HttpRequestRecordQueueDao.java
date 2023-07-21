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
package io.datarouter.exception.storage.httprecord;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HttpRequestRecordQueueDao extends BaseDao{

	public static class HttpRequestRecordQueueDaoParams extends BaseRedundantDaoParams{

		public HttpRequestRecordQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final BlobQueueStorageNode<HttpRequestRecordBinaryDto> node;

	@Inject
	public HttpRequestRecordQueueDao(
			Datarouter datarouter,
			HttpRequestRecordQueueDaoParams params,
			QueueNodeFactory queueNodeFactory,
			EnvironmentName environmentNameSupplier){
		super(datarouter);
		String namespace = environmentNameSupplier.deprecatedIsProduction()
				? "shared"
				: environmentNameSupplier.get() + "-shared";
		node = Scanner.of(params.clientIds)
				.map(clientId -> queueNodeFactory
						.createBlobQueue(
								clientId,
								"HttpRequestBinaryDto",
								BinaryDtoIndexedCodec.of(HttpRequestRecordBinaryDto.class))
						.withNamespace(namespace)
						.withTag(Tag.DATAROUTER)
						.withAgeMonitoring(false)
						.build())
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void combineAndPut(Scanner<HttpRequestRecordBinaryDto> dtos){
		node.combineAndPut(dtos);
	}

	public BlobQueueConsumer<HttpRequestRecordBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

}
