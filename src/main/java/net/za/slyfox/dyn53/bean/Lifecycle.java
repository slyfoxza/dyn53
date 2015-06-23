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
package net.za.slyfox.dyn53.bean;

/**
 * Interface for controlling the lifecycle of a long-lived bean that typically executes asynchronously.
 */
public interface Lifecycle {
	/**
	 * Starts the bean. This method blocks until the bean has been fully started.
	 */
	void start();

	/**
	 * Stops the bean.
	 */
	void stop();
}
