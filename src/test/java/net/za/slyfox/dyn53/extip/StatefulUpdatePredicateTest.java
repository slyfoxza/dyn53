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

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StatefulUpdatePredicateTest {
	private static final InetAddress IPV4_ADDRESS;
	private static final InetAddress IPV4_ADDRESS_DUP;
	private static final InetAddress IPV6_ADDRESS;

	private StatefulUpdatePredicate predicate;

	static {
		try {
			IPV4_ADDRESS = InetAddress.getByName("12.34.56.78");
			IPV4_ADDRESS_DUP = InetAddress.getByAddress(IPV4_ADDRESS.getAddress());
			IPV6_ADDRESS = InetAddress.getByName("12:34:56::78");
		} catch(UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void createPredicate() {
		predicate = new StatefulUpdatePredicate();
	}

	@Test
	public void firstTestAllowsUpdate() {
		assertThat(predicate.test(IPV4_ADDRESS), is(true));
	}

	@Test
	public void unchangedAddressDisallowsUpdate() {
		predicate.test(IPV4_ADDRESS);
		assertThat(predicate.test(IPV4_ADDRESS_DUP), is(false));
	}

	@Test
	public void changedAddressAllowsUpdate() {
		predicate.test(IPV4_ADDRESS);
		assertThat(predicate.test(IPV6_ADDRESS), is(true));
	}

	@Test
	public void unchangedAddressAfterResetAllowsUpdate() {
		predicate.test(IPV4_ADDRESS);
		predicate.reset();
		assertThat(predicate.test(IPV4_ADDRESS_DUP), is(true));
	}
}
