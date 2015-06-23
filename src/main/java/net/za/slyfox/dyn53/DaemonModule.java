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
package net.za.slyfox.dyn53;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import net.za.slyfox.dyn53.bean.Lifecycle;

import java.nio.file.Path;
import java.nio.file.Paths;

final class DaemonModule extends AbstractModule {
	private final Path pidFilePath;

	public DaemonModule(String pidFile) {
		this.pidFilePath = Paths.get(pidFile);
	}

	@Override
	protected void configure() {
		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(PidFileLifecycle.class);
		bind(Path.class).toInstance(pidFilePath);
	}
}
