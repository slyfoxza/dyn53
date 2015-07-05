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

/**
 * A predicate that always returns true to allow unconditional updates.
 */
final class UnconditionalUpdatePredicate implements InetAddressPredicate {
	/**
	 * Always returns {@code true}.
	 *
	 * @param inetAddress the address to evaluate. This argument is ignored by this implementation.
	 * @return {@code true}
	 */
	@Override
	public boolean test(InetAddress inetAddress) {
		return true;
	}
}
