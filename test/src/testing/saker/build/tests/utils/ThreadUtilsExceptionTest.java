/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing.saker.build.tests.utils;

import java.util.Map;

import saker.util.function.Functionals;
import saker.util.thread.ParallelExecutionAbortedException;
import saker.util.thread.ParallelExecutionCancelledException;
import saker.util.thread.ParallelExecutionFailedException;
import saker.util.thread.ThreadUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ThreadUtilsExceptionTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		Runnable abortthrowing = (Runnable) () -> {
			throw new ParallelExecutionAbortedException();
		};
		Runnable failer = (Runnable) () -> {
			throw new UnsupportedOperationException();
		};
		Runnable nullrunnable = Functionals.nullRunnable();
		assertException(ParallelExecutionAbortedException.class, () -> ThreadUtils.runParallelRunnables(abortthrowing,
				abortthrowing, abortthrowing, abortthrowing, abortthrowing));

		Thread.currentThread().interrupt();
		assertException(ParallelExecutionCancelledException.class, () -> ThreadUtils.runParallelRunnables(nullrunnable,
				nullrunnable, nullrunnable, nullrunnable, nullrunnable, nullrunnable));
		Thread.interrupted();

		assertException(ParallelExecutionFailedException.class,
				() -> ThreadUtils.runParallelRunnables(failer, failer, failer, failer, failer, failer));

		try {
			ThreadUtils.runParallelRunnables(abortthrowing, abortthrowing, failer, abortthrowing, abortthrowing);
			fail("Failed to catch parallel execution exception.");
		} catch (ParallelExecutionFailedException | ParallelExecutionAbortedException e) {
			// good, expected any of these
		} catch (Throwable e) {
			// bad, unexpected
			fail(e);
		}
	}

}
