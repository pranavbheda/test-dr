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
package io.datarouter.aws.sqs;

import io.datarouter.instrumentation.cost.CloudPriceType;
import io.datarouter.instrumentation.cost.CostCounters;

public class SqsCostCounters{

	public static void request(){
		inc(CloudPriceType.MESSAGE_REQUEST_AWS);
	}

	private static void inc(CloudPriceType price){
		CostCounters.incInput(price.id, 1);
		CostCounters.incNanos(price.id, price.nanoDollars);
	}

}
