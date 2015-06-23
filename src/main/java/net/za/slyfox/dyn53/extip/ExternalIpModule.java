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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import net.za.slyfox.dyn53.bean.Lifecycle;
import net.za.slyfox.dyn53.concurrent.NamedPoolThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ExternalIpModule extends AbstractModule {
	@Override
	protected void configure() {
		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(ExternalIpDiscoveryLifecycle.class);

		bind(Runnable.class).to(ExternalIpDiscoveryCommand.class);

		bind(ScheduledExecutorService.class)
				.toInstance(Executors.newSingleThreadScheduledExecutor(
						new NamedPoolThreadFactory("externalIpDiscovery")));
		bind(Long.class).annotatedWith(Names.named("initialDelay")).toInstance(0L);
		bind(Long.class).annotatedWith(Names.named("delay")).toInstance(300L);
	}
}
