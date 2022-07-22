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
package io.datarouter.aws.sqs.blob;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsBlobOpFactory;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.BlobQueueMessage.BlobQueueMessageFielder;
import io.datarouter.storage.queue.BlobQueueMessageDto;
import io.datarouter.storage.queue.BlobQueueMessageKey;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.config.service.ServiceName;

public class SqsBlobNode extends BasePhysicalNode<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder>
implements PhysicalBlobQueueStorageNode{
	private static final Logger logger = LoggerFactory.getLogger(SqsBlobNode.class);

	private final NodeParams<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder> params;
	private final SqsClientManager sqsClientManager;
	private final EnvironmentName environmentName;
	private final ServiceName serviceName;
	private final ClientId clientId;
	private final boolean owned;
	private final Supplier<Twin<String>> queueUrlAndName;
	private final SqsBlobOpFactory opFactory;

	public SqsBlobNode(NodeParams<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder> params,
			ClientType<?,?> clientType,
			SqsClientManager sqsClientManager,
			EnvironmentName environmentName,
			ServiceName serviceName){
		super(params, clientType);
		this.params = params;
		this.sqsClientManager = sqsClientManager;
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.clientId = params.getClientId();
		this.owned = params.getQueueUrl() == null;
		this.queueUrlAndName = SingletonSupplier.of(this::getOrCreateQueueUrl);
		this.opFactory = new SqsBlobOpFactory(this, sqsClientManager, clientId);
	}

	private Twin<String> getOrCreateQueueUrl(){
		String queueUrl;
		String queueName;
		if(!owned){
			queueUrl = params.getQueueUrl();
			queueName = queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
			//don't issue the createQueue request because it is probably someone else's queue
		}else{
			queueName = buildQueueName(environmentName.get(), serviceName.get());
			queueUrl = createQueueAndGetUrl(queueName);
			sqsClientManager.updateAttr(clientId, queueUrl, QueueAttributeName.MessageRetentionPeriod,
					BaseSqsNode.RETENTION_S);
			logger.warn("retention updated queueName=" + queueName);
		}
		logger.warn("nodeName={}, queueUrl={}", getName(), queueUrl);
		return new Twin<>(queueUrl, queueName);
	}

	private String createQueueAndGetUrl(String queueName){
		var createQueueRequest = new CreateQueueRequest(queueName);
		try{
			return sqsClientManager.getAmazonSqs(clientId).createQueue(createQueueRequest).getQueueUrl();
		}catch(RuntimeException e){
			throw new RuntimeException("queueName=" + queueName + " queueNameLength=" + queueName.length(), e);
		}
	}

	public String buildQueueName(String environmentName, String serviceName){
		String namespace = params.getNamespace().orElseGet(() -> environmentName + "-" + serviceName);
		String tableName = getFieldInfo().getTableName();
		String queueName = StringTool.isEmpty(namespace) ? tableName : (namespace + "-" + tableName);
		if(queueName.length() > BaseSqsNode.MAX_QUEUE_NAME_LENGTH){
			// Future change to a throw.
			logger.error("queue={} overflows the max size {}", queueName, BaseSqsNode.MAX_QUEUE_NAME_LENGTH);
		}
		return queueName;
	}

	public Supplier<Twin<String>> getQueueUrlAndName(){
		return queueUrlAndName;
	}

	@Override
	public int getMaxDataSize(){
		return CommonFieldSizes.MAX_SQS_SIZE;
	}

	@Override
	public void put(byte[] data, Config config){
		opFactory.makePutOp(data, config).call();
	}

	@Override
	public Optional<BlobQueueMessageDto> peek(Config config){
		return Optional.ofNullable(opFactory.makePeekOp(config).call());
	}

	@Override
	public void ack(byte[] handle, Config config){
		opFactory.makeAckOp(handle, config).call();
	}

}
