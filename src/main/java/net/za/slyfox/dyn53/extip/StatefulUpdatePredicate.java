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

import javax.inject.Singleton;
import java.net.InetAddress;

/**
 * A stateful predicate that compares the input argument to a previous value, and only returns a positive result if the
 * two differ.
 *
 * <p>This implementation is safe to concurrently use from multiple threads, as its methods are synchronized.</p>
 */
@Singleton
final class StatefulUpdatePredicate implements InetAddressPredicate {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private InetAddress previousAddress;

	/**
	 * Compares {@code inetAddress} to the previously evaluated address, if any. The predicate will only return {@code
	 * true} if the two addresses are not {@linkplain InetAddress#equals(Object) equal}.
	 *
	 * @param address the address to compare with the previously evaluated address
	 * @return {@code true} if {@code address} is not equal to the previously evaluated address, otherwise {@code
	 *         false}.
	 * @throws NullPointerException if {@code address} is {@code null}
	 */
	@Override
	public synchronized boolean test(InetAddress address) {
		if(!address.equals(previousAddress)) {
			logger.info("Previous address {} differs from current {}, allowing update", previousAddress, address);
			previousAddress = address;
			return true;
		} else {
			logger.info("Previous address {} is still current, disallowing update", previousAddress);
			return false;
		}
	}

	/**
	 * Resets the internal state of the predicate by discarding the previously evaluated address, so that subsequent
	 * calls to {@link #test(InetAddress)} will return {@code true}.
	 */
	@Override
	public synchronized void reset() {
		logger.debug("Resetting previous address");
		previousAddress = null;
	}
}
