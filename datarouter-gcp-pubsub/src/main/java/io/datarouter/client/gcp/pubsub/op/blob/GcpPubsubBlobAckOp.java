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
package io.datarouter.client.gcp.pubsub.op.blob;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.AcknowledgeRequest;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.storage.client.ClientId;

public class GcpPubsubBlobAckOp extends GcpPubsubBlobOp<Void>{

	private final byte[] handle;

	public GcpPubsubBlobAckOp(
			byte[] handle,
			GcpPubsubBlobNode<?> node,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(node, clientManager, clientId);
		this.handle = handle;
	}

	@Override
	protected Void run(){
		SubscriberStub subscriberStub = clientManager.getSubscriber(clientId);
		AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.newBuilder()
				.setSubscription(subscriberId)
				.addAckIds(StringCodec.UTF_8.decode(handle))
				.build();
		subscriberStub.acknowledgeCallable().call(acknowledgeRequest);
		return null;
	}

}
