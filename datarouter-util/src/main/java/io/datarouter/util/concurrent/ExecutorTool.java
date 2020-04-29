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
package io.datarouter.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorTool{

	public static ThreadPoolExecutor createThreadPool(
			String name,
			int minThreadCount,
			int maxThreadCount,
			int queueSize,
			RejectedExecutionHandler rejectPolicy){
		ThreadFactory threadFactory = new NamedThreadFactory(name, true);
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
		return new ThreadPoolExecutor(minThreadCount, maxThreadCount, 1, TimeUnit.MINUTES, queue, threadFactory,
				rejectPolicy);
	}

	public static ThreadPoolExecutor createFixedPool(String name, int numThreads){
		ThreadFactory threadFactory = new NamedThreadFactory(name, true);
		return (ThreadPoolExecutor)Executors.newFixedThreadPool(numThreads, threadFactory);
	}

	public static ThreadPoolExecutor newCachedThreadPool(String name){
		ThreadFactory threadFactory = new NamedThreadFactory(name, true);
		return (ThreadPoolExecutor)Executors.newCachedThreadPool(threadFactory);
	}

	public static ThreadPoolExecutor createCached(String name){
		ThreadFactory threadFactory = new NamedThreadFactory(name, true);
		return (ThreadPoolExecutor)Executors.newCachedThreadPool(threadFactory);
	}

}