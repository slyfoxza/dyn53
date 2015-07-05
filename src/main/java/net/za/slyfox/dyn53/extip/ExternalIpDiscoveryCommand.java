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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLConnection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements the task logic to obtain the external IP address of the network the application is running in.
 */
final class ExternalIpDiscoveryCommand implements Runnable {
	private final Provider<Consumer<InetAddress>> consumerProvider;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final InetAddressPredicate updatePredicate;

	/**
	 * Injects dependencies into the instance.
	 *
	 * @param consumerProvider a provider used to obtain a {@link Consumer} that will process the discovered IP address
	 * @param updatePredicate a predicate used to evaluate whether the command will invoke the consumer or not
	 * @throws NullPointerException if {@code consumerProvider} is {@code null}
	 */
	@Inject
	ExternalIpDiscoveryCommand(Provider<Consumer<InetAddress>> consumerProvider, InetAddressPredicate updatePredicate) {
		this.consumerProvider = Objects.requireNonNull(consumerProvider);
		this.updatePredicate = Objects.requireNonNull(updatePredicate);
	}

	/**
	 * Requests the external IP from the ipify web service, and passes it on to the {@link Consumer} obtained from the
	 * {@link Provider} this object was initialized with.
	 */
	@Override
	public void run() {
		try {
			logger.info("Requesting external IP from https://api.ipify.org/");
			final InetAddress address;
			try {
				URLConnection connection = URI.create("https://api.ipify.org").toURL().openConnection();

				final String ip;
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					ip = reader.readLine();
				}

				address = InetAddress.getByName(ip);
			} catch(IOException e) {
				logger.warn("Failed to retrieve external IP from remote service", e);
				return;
			} catch(RuntimeException e) {
				logger.error("Failed to retrieve external IP from remote service", e);
				return;
			}

			try {
				if(updatePredicate.test(address)) consumerProvider.get().accept(address);
			} catch(RuntimeException e) {
				logger.error("Failed to process external IP ({}) received from remote service", address, e);
			}
		} catch(Error e) {
			logger.error("JVM encountered error while executing command, aborting application execution", e);
			System.exit(1);
		}
	}
}
