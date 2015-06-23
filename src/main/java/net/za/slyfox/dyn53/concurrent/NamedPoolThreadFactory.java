/*
 * Copyright 2015 Philip Cronje
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.za.slyfox.dyn53.concurrent;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a {@link ThreadFactory} that prefixes each thread with a pool name. The trailing part of the name will be
 * the value of a monotonically increasing counter.
 */
public final class NamedPoolThreadFactory implements ThreadFactory {
	private final String poolName;
	private final AtomicInteger threadCounter = new AtomicInteger(1);

	/**
	 * Initializes the thread factory with the pool name.
	 *
	 * @param poolName the value to prefix each thread name with
	 * @throws IllegalArgumentException if {@code poolName} is {@linkplain String#isEmpty() empty}
	 * @throws NullPointerException if {@code poolName} is null
	 */
	public NamedPoolThreadFactory(String poolName) {
		this.poolName = Objects.requireNonNull(poolName);
		if(this.poolName.isEmpty()) throw new IllegalArgumentException("Pool name may not be empty");
	}

	/**
	 * Constructs a new thread, initializing it with a name derived from the pool name for this thread factory.
	 *
	 * @param runnable the {@code Runnable} this thread will execute
	 * @return the created thread
	 */
	@Override
	public Thread newThread(Runnable runnable) {
		return new Thread(runnable, poolName + '-' + threadCounter.getAndIncrement());
	}
}
