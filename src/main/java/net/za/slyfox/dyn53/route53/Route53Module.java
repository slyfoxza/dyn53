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

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.net.InetAddress;
import java.util.Objects;
import java.util.function.Consumer;

public final class Route53Module extends AbstractModule {
	private final String hostedZoneId;
	private final String resourceRecordSetName;
	private final Long resourceRecordSetTtl;

	public Route53Module(String hostedZoneId, String resourceRecordSetName, Long resourceRecordSetTtl) {
		this.hostedZoneId = Objects.requireNonNull(hostedZoneId);
		this.resourceRecordSetName = Objects.requireNonNull(resourceRecordSetName);
		this.resourceRecordSetTtl = Objects.requireNonNull(resourceRecordSetTtl);
	}

	@Override
	protected void configure() {
		bind(new TypeLiteral<Consumer<InetAddress>>(){}).to(Route53Updater.class);

		bind(String.class).annotatedWith(Names.named("hostedZoneId")).toInstance(hostedZoneId);
		bind(String.class).annotatedWith(Names.named("resourceRecordSetName")).toInstance(resourceRecordSetName);
		bind(Long.class).annotatedWith(Names.named("resourceRecordSetTtl")).toInstance(resourceRecordSetTtl);
	}

	@Provides
	protected AmazonRoute53 amazonRoute53() {
		return new AmazonRoute53Client(new ProfileCredentialsProvider("dyn53"));
	}
}
