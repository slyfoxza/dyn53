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
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class UnconditionalUpdatePredicateTest {
	private UnconditionalUpdatePredicate predicate;

	@DataPoint public static InetAddress nullAddress = null;
	@DataPoint public static InetAddress ipv4Address() throws Exception { return InetAddress.getByName("127.0.0.1"); }
	@DataPoint public static InetAddress ipv6Address() throws Exception { return InetAddress.getByName("::1"); }

	@Before
	public void createPredicate() {
		predicate = new UnconditionalUpdatePredicate();
	}

	@Theory
	public void testAlwaysAllowsUpdate(InetAddress address) {
		assertThat(predicate.test(address), is(true));
	}

	@Theory
	public void testAfterResetAllowsUpdate(InetAddress address) {
		predicate.reset();
		assertThat(predicate.test(address), is(true));
	}
}
