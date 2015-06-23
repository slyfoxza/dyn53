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
package net.za.slyfox.dyn53.extip;

import net.za.slyfox.dyn53.bean.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implements a {@link Lifecycle} object that manages a scheduled task to discover the external IP of the network the
 * application is running in.
 */
final class ExternalIpDiscoveryLifecycle implements Lifecycle {
	private final Provider<Runnable> commandProvider;
	private final long delay;
	private final ScheduledExecutorService executorService;
	private final long initialDelay;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Initializes the {@code ExternalIpDiscoveryLifecycle} with configuration variables and dependencies.
	 *
	 * @param initialDelay the delay before the initial scheduled task should begin executing
	 * @param delay the delay between scheduled tasks, measured from the end of the previous task
	 * @param commandProvider a {@code Provider} that will supply the tasks to execute
	 * @param executorService the scheduled executor service that will schedule and execute tasks
	 * @throws NullPointerException if a required dependency is {@code null}
	 */
	@Inject
	ExternalIpDiscoveryLifecycle(@Named("initialDelay") long initialDelay, @Named("delay") long delay,
			Provider<Runnable> commandProvider, ScheduledExecutorService executorService) {
		this.commandProvider = Objects.requireNonNull(commandProvider);
		this.delay = delay;
		this.executorService = Objects.requireNonNull(executorService);
		this.initialDelay = initialDelay;
	}

	/**
	 * Schedules the external IP discovery task.
	 */
	@Override
	public void start() {
		logger.info("Scheduling external IP discovery to execute every {} seconds, after initial delay of {} seconds",
				delay, initialDelay);
		executorService.scheduleWithFixedDelay(commandProvider.get(), initialDelay, delay, TimeUnit.SECONDS);
	}

	/**
	 * Gracefully shuts down the scheduled executor service. If, after one minute, the executor service has not shut
	 * down, this method will return, but may result in dangling threads/tasks.
	 */
	@Override
	public void stop() {
		logger.info("Shutting down external IP discovery scheduler");
		executorService.shutdownNow();
		logger.info("Waiting for termination of scheduled task");
		try {
			if(!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
				logger.warn("Scheduled task is still running, stopping anyway");
			} else {
				logger.info("Shutdown of external IP discovery schedule complete");
			}
		} catch(InterruptedException e) {
			logger.debug("Interrupted while waiting for termination of scheduled task");
		}
	}
}
