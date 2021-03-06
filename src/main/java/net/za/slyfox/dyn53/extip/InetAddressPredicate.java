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

import java.net.InetAddress;
import java.util.function.Predicate;

/**
 * Extension of the {@link Predicate} interface for {@link InetAddress} objects. This interface adds a {@link #reset()}
 * method that allows stateful predicates to reset their internal state.
 */
interface InetAddressPredicate extends Predicate<InetAddress> {
	/**
	 * Resets the internal state of the predicate. The default implementation is a no-op.
	 */
	default void reset() { }
}
