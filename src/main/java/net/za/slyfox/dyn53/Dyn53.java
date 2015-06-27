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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import net.za.slyfox.dyn53.bean.Lifecycle;
import net.za.slyfox.dyn53.extip.ExternalIpModule;
import net.za.slyfox.dyn53.route53.Route53Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Application launcher class.
 */
public final class Dyn53 implements Runnable {
	private static final AtomicInteger shutdownThreadCounter = new AtomicInteger(1);

	private final Set<Lifecycle> lifecycleObjects;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Runtime runtime;

	@Inject
	public Dyn53(Set<Lifecycle> lifecycleObjects, Runtime runtime) {
		this.lifecycleObjects = Objects.requireNonNull(lifecycleObjects);
		this.runtime = Objects.requireNonNull(runtime);
	}

	/**
	 * Initialises dependency injection, obtains an instance of this class, and {@linkplain #run() runs} it.
	 *
	 * @param arguments array of command line arguments
	 */
	public static void main(String[] arguments) {
		/**
		 * Load configuration properties from a file, if specified. If a file is specified, `properties` will refer to
		 * a copy of System.getProperties(), with the configuration file properties as its default values fallback.
		 * Otherwise, `properties` will be aliased to System.getProperties().
		 */
		final String configurationFilePath = System.getProperty("net.za.slyfox.dyn53.configurationFile");
		final Properties properties;
		if(configurationFilePath != null) {
			final Properties fileProperties = new Properties();
			try(final BufferedReader reader = Files.newBufferedReader(Paths.get(configurationFilePath))) {
				fileProperties.load(reader);
			} catch(IOException e) {
				LoggerFactory.getLogger(Dyn53.class)
						.error("Could not load configuration properties from {}", configurationFilePath);
				System.exit(1);
			}

			properties = new Properties(fileProperties);
			properties.putAll(System.getProperties());
		} else {
			properties = System.getProperties();
		}

		final String logFile = properties.getProperty("net.za.slyfox.dyn53.logFile");
		if(logFile != null) {
			final LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
			context.putProperty("logFile", logFile);
			final ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
			final JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			try {
				configurator.doConfigure(Dyn53.class.getResourceAsStream("/logback-file.xml"));
			} catch(JoranException ignored) {
			}

			final Appender<ILoggingEvent> stdoutAppender = rootLogger.getAppender("stdout");
			rootLogger.detachAppender(stdoutAppender);
			stdoutAppender.stop();

			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}

		final Logger logger = LoggerFactory.getLogger(Dyn53.class);
		logger.info("Initializing Dyn53 application");

		final Set<Module> modules = new HashSet<>();
		modules.add(new ExternalIpModule());
		modules.add(new SystemModule());

		final String hostedZoneId = properties.getProperty("net.za.slyfox.dyn53.route53.hostedZoneId");
		if(hostedZoneId == null) throw new IllegalArgumentException("Hosted zone ID missing");
		final String resourceRecordSetName = properties.getProperty(
				"net.za.slyfox.dyn53.route53.resourceRecordSetName");
		if(resourceRecordSetName == null) throw new IllegalArgumentException("Resource record set name missing");
		modules.add(new Route53Module(hostedZoneId, resourceRecordSetName));

		final String pidFile = properties.getProperty("net.za.slyfox.dyn53.daemon.pidFile");
		if(pidFile != null) modules.add(new DaemonModule(pidFile));

		final Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
		try {
			injector.getInstance(Dyn53.class).run();
		} catch(RuntimeException e) {
			logger.error("Application terminated with error", e);
		}
	}

	@Override
	public void run() {
		logger.info("Starting Dyn53 application");
		if(lifecycleObjects.isEmpty()) throw new IllegalStateException("No lifecycle objects registered");
		lifecycleObjects.forEach(lifecycle -> {
			logger.debug("Starting {}", lifecycle);
			lifecycle.start();
			logger.debug("Registering shutdown hook for {}", lifecycle);
			runtime.addShutdownHook(new Thread(lifecycle::stop, getShutdownThreadName(lifecycle)));
		});
	}

	private static String getShutdownThreadName(Lifecycle lifecycle) {
		final Named nameAnnotation = lifecycle.getClass().getAnnotation(Named.class);
		return "shutdown-"
				+ ((nameAnnotation != null) ? nameAnnotation.value() : shutdownThreadCounter.getAndIncrement());
	}
}
