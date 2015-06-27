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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements a {@link Consumer} that updates an Amazon Route 53 resource record set with the value of an
 * {@link InetAddress}.
 */
final class Route53Updater implements Consumer<InetAddress> {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String hostedZoneId;
	private final String resourceRecordSetName;
	private final Long resourceRecordSetTtl;
	private final AmazonRoute53 route53;

	/**
	 * Initializes this {@code Route53Updater} with configuration values, and injects dependencies.
	 *
	 * @param hostedZoneId the identifier of the hosted zone to update, as given by Route 53
	 * @param resourceRecordSetName the name of the resource record set in the hosted zone to update
	 * @param route53 the Amazon Route 53 client interface to use when making requests against the service
	 * @throws NullPointerException if a required parameter is {@code null}
	 */
	@Inject
	Route53Updater(@Named("hostedZoneId") String hostedZoneId,
			@Named("resourceRecordSetName") String resourceRecordSetName,
			@Named("resourceRecordSetTtl") Long resourceRecordSetTtl, AmazonRoute53 route53) {
		this.hostedZoneId = Objects.requireNonNull(hostedZoneId);
		this.resourceRecordSetName = Objects.requireNonNull(resourceRecordSetName);
		this.resourceRecordSetTtl = Objects.requireNonNull(resourceRecordSetTtl);
		this.route53 = Objects.requireNonNull(route53);
	}

	/**
	 * Updates the configured resource record set with the value of an IP address.
	 *
	 * @param inetAddress the address to update the resource record set with
	 */
	@Override
	public void accept(InetAddress inetAddress) {
		final String address = inetAddress.getHostAddress();
		logger.info("Updating resource record set {} in hosted zone {} to {}", resourceRecordSetName, hostedZoneId,
				address);

		final ResourceRecordSet resourceRecordSet = new ResourceRecordSet(resourceRecordSetName,
				getResourceRecordType(inetAddress))
				.withResourceRecords(new ResourceRecord(address))
				.withTTL(resourceRecordSetTtl);
		final Change change = new Change(ChangeAction.UPSERT, resourceRecordSet);
		final ChangeBatch changeBatch = new ChangeBatch().withChanges(change).withComment("Dyn53 update");

		final ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest()
				.withHostedZoneId(hostedZoneId)
				.withChangeBatch(changeBatch);

		if(logger.isDebugEnabled()) {
			logger.debug("Requesting change: {}", change);
		}

		final ChangeResourceRecordSetsResult result = route53.changeResourceRecordSets(request);
		if(logger.isInfoEnabled()) {
			logger.info("Result of change request {}: {}", result.getChangeInfo().getId(),
					result.getChangeInfo().getStatus());
		}
	}

	/**
	 * Maps the given {@link InetAddress} to a {@link RRType} enumeration value.
	 *
	 * @param address the address to map
	 * @return the {@code RRType} enumeration value corresponding to the type of address given
	 * @throws IllegalArgumentException if {@code address} cannot be mapped to a value in the {@code RRType} enumeration
	 */
	private static RRType getResourceRecordType(InetAddress address) {
		if(address instanceof Inet4Address) {
			return RRType.A;
		} else if(address instanceof Inet6Address) {
			return RRType.AAAA;
		} else {
			throw new IllegalArgumentException("Unsupported address type " + address.getClass());
		}
	}
}
