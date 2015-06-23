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
package net.za.slyfox.dyn53.route53;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Route53UpdaterTest {
	private static final String HOSTED_ZONE_ID = "HOSTEDZONE";
	private static final String RESOURCE_RECORD_SET_NAME = "test.example.com.";

	@Rule public MockitoRule mockObjects = MockitoJUnit.rule();

	private Route53Updater updater;

	@Mock private AmazonRoute53 route53;

	@Before
	public void createUpdater() {
		updater = new Route53Updater(HOSTED_ZONE_ID, RESOURCE_RECORD_SET_NAME, route53);
	}

	@Test
	public void ipv4AddressUpdatesARecord() throws UnknownHostException {
		ChangeResourceRecordSetsResult requestResult = mock(ChangeResourceRecordSetsResult.class);
		ChangeInfo changeInfo = mock(ChangeInfo.class);
		InetAddress address = InetAddress.getByName("127.0.0.1");

		when(requestResult.getChangeInfo()).thenReturn(changeInfo);
		when(route53.changeResourceRecordSets(any())).thenReturn(requestResult);

		updater.accept(address);

		verify(route53).changeResourceRecordSets(argThat(updatesRecord(RRType.A, RESOURCE_RECORD_SET_NAME)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownInetAddressTypeThrowsException() {
		InetAddress address = mock(InetAddress.class);
		updater.accept(address);
	}

	private static Matcher<ChangeResourceRecordSetsRequest> updatesRecord(RRType type, String resourceRecordSetName) {
		return new UpdatesRecordMatcher(type, resourceRecordSetName);
	}

	private static class UpdatesRecordMatcher extends TypeSafeMatcher<ChangeResourceRecordSetsRequest> {
		private final String resourceRecordSetName;
		private final RRType type;

		UpdatesRecordMatcher(RRType type, String resourceRecordSetName) {
			this.resourceRecordSetName = resourceRecordSetName;
			this.type = type;
		}

		@Override
		protected boolean matchesSafely(ChangeResourceRecordSetsRequest item) {
			return item.getChangeBatch().getChanges().stream()
					.map(Change::getResourceRecordSet)
					.anyMatch(rrs -> rrs.getName().equals(resourceRecordSetName)
							&& rrs.getType().equals(type.toString()));
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("change resource record sets request for ").appendValue(resourceRecordSetName)
					.appendText(" with record type ").appendValue(type);
		}
	}
}
