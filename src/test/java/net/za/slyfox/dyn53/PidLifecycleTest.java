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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PidLifecycleTest {
	private static final Path PID_FILE_RELATIVE_PATH = Paths.get("var", "run", "test.pid");

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private PidFileLifecycle lifecycle;

	private Path pidFilePath;

	@Before
	public void createLifecycle() {
		pidFilePath = temporaryFolder.getRoot().toPath().resolve(PID_FILE_RELATIVE_PATH);
		lifecycle = new PidFileLifecycle(pidFilePath);
	}

	@Test
	public void startCreatesPidFile() {
		lifecycle.start();
		assertThat(Files.isRegularFile(pidFilePath), is(true));
	}

	@Test
	public void stopDeletesPidFile() throws IOException {
		Files.createDirectories(pidFilePath.getParent());
		temporaryFolder.newFile(PID_FILE_RELATIVE_PATH.toString());
		lifecycle.stop();
		assertThat(Files.exists(pidFilePath), is(false));
	}
}
