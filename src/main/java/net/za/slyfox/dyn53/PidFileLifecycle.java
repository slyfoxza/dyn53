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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import net.za.slyfox.dyn53.bean.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Implements a {@link Lifecycle} object that manages a PID file for the JVM process.
 */
final class PidFileLifecycle implements Lifecycle {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Path pidFilePath;

	/**
	 * Initializes the {@code PidFileLifecycle} with the path to the PID file.
	 *
	 * @param pidFilePath the path to the PID file
	 */
	@Inject
	PidFileLifecycle(Path pidFilePath) {
		this.pidFilePath = Objects.requireNonNull(pidFilePath);
	}

	/**
	 * Obtains the PID of the JVM process, and writes the PID to the file.
	 *
	 * @throws UncheckedIOException if writing to the PID file fails
	 */
	@Override
	public final void start() {
		try {
			if(!Files.exists(pidFilePath.getParent())) Files.createDirectories(pidFilePath.getParent());
			Files.write(pidFilePath, getPid().getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Returns the PID of the JVM process.
	 *
	 * @return a string containing the PID of the JVM process
	 */
	private String getPid() {
		if(Platform.isWindows()) {
			return String.valueOf(WindowsKernelLibrary.INSTANCE.GetCurrentProcessId());
		} else {
			return String.valueOf(UnixCLibrary.INSTANCE.getpid());
		}
	}

	/**
	 * Deletes the PID file, if it exists.
	 */
	@Override
	public final void stop() {
		try {
			Files.deleteIfExists(pidFilePath);
		} catch(IOException e) {
			logger.warn("Failed to delete PID file \"{}\"", pidFilePath, e);
		}
	}

	private interface UnixCLibrary extends Library {
		UnixCLibrary INSTANCE = (UnixCLibrary)Native.loadLibrary("c", UnixCLibrary.class);
		int getpid();
	}

	private interface WindowsKernelLibrary extends Library {
		WindowsKernelLibrary INSTANCE
				= (WindowsKernelLibrary)Native.loadLibrary("kernel32", WindowsKernelLibrary.class);
		int GetCurrentProcessId();
	}
}
