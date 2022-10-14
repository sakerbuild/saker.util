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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import saker.util.DateUtils;
import saker.util.ImmutableUtils;
import saker.util.thread.ThreadUtils;
import saker.util.thread.ThreadUtils.ThreadWorkPool;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;
import testing.saker.build.tests.ExecutionOrderer;

@SakerTest
public class ThreadUtilsTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		// make sure the null value is handled

		{
			Set<Object> res = Collections.synchronizedSet(new HashSet<>());
			ThreadUtils.runParallelItems(ImmutableUtils.asUnmodifiableArrayList(null, 1, 2, 3), s -> {
				res.add(s);
			});
			assertEquals(res, setOf(null, 1, 2, 3));
		}

		{
			//test that interruption doesnt cause the work pool to throw an exception on close
			Set<Object> res = Collections.synchronizedSet(new HashSet<>());
			try (ThreadWorkPool wp = ThreadUtils.newDynamicWorkPool()) {
				wp.offer(() -> res.add(123));
				Thread.currentThread().interrupt();

			}
			assertEquals(res, setOf(123));
		}
		testInterruptBeforeClose();

		//clear interrupt flag for further tests
		Thread.interrupted();

		testThreadPoolAPI(() -> ThreadUtils.newFixedWorkPool(4, "fixed-"), false);
		testThreadPoolAPI(ThreadUtils::newDirectWorkPool, true);
		testThreadPoolAPI(ThreadUtils::newDynamicWorkPool, false);

		//assert that threads don't exit in a fixed work pool when the pool exites, but doesn close
		System.out.println("ThreadUtilsTest.runTest() fixed same thread test");
		try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool(4)) {
			Thread[] runthread = { null };
			wp.offer(() -> {
				runthread[0] = Thread.currentThread();
			});
			wp.exit();
			wp.reset();
			assertNonNull(runthread[0]);
			wp.offer(() -> {
				assertIdentityEquals(Thread.currentThread(), runthread[0]);
			});
		}
		assertFalse(Thread.currentThread().isInterrupted());

		runParallelMultiInterruptTest();
	}

	private static void testInterruptBeforeClose() throws InterruptedException, AssertionError {
		//clear interrupt flag for further tests
		Thread.interrupted();
		{
			Set<Object> res = Collections.synchronizedSet(new HashSet<>());
			Semaphore sem = new Semaphore(0);
			try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool()) {
				wp.offer(() -> {
					System.out.println("ThreadUtilsTest.testInterruptBeforeClose() task enter");
					try {
						sem.release();
						res.add(456);
						assertException(InterruptedException.class, () -> Thread.sleep(10 * DateUtils.MS_PER_SECOND));
					} catch (Throwable e) {
						e.printStackTrace();
						throw e;
					} finally {
						System.out.println("ThreadUtilsTest.testInterruptBeforeClose() task exit");
					}
				});

				//interrupt after the runnable is accepted, otherwise we might interrupt the pool
				//after/before the runnable, and get a ParallelExecutionCancelledException 
				sem.acquire();
				Thread.currentThread().interrupt();
			}
			assertEquals(res, setOf(456));
		}
	}

	/**
	 * Checks that all interrupts are forwarded to the runners
	 */
	private static void runParallelMultiInterruptTest() throws Exception {
		final int INTERRUPT_COUNT = 10;
		Thread currentthread = Thread.currentThread();
		for (int threadcount = 1; threadcount <= 10; threadcount++) {
			AtomicInteger interruptcounter = new AtomicInteger();
			System.out.println("Run with " + threadcount + " threads");
			Semaphore[] semaphores = new Semaphore[threadcount];
			for (int i = 0; i < semaphores.length; i++) {
				semaphores[i] = new Semaphore(0);
			}
			Thread.interrupted(); // clear flag of the current thread

			Runnable interruptorrunnable = () -> {
				for (int intidx = 0; intidx < INTERRUPT_COUNT; intidx++) {
					try {
						//acquire all semaphores before interrupting the current thread
						for (int semidx = 0; semidx < semaphores.length; semidx++) {
							semaphores[semidx].acquire();
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					currentthread.interrupt();
				}
			};

			Runnable[] runnables = new Runnable[threadcount];
			for (int i = 0; i < runnables.length; i++) {
				Semaphore sem = semaphores[i];
				int runidx = i;
				runnables[i] = () -> {
					for (int intidx = 0; intidx < INTERRUPT_COUNT; intidx++) {
						System.out.println("Start[" + intidx + "] of runnable[" + runidx + "]");
						sem.release();
						try {
							//timeout so test doesn't halt forever
							Thread.sleep(10 * DateUtils.MS_PER_SECOND);
							fail("should've gotten interrupted");
						} catch (InterruptedException e) {
							//good
							System.out.println("Interrupted[" + intidx + "]: " + e);
							interruptcounter.incrementAndGet();
						} catch (Throwable e) {
							//fail, release semaphores so test doesnt halt
							sem.release(Integer.MAX_VALUE / 2);
							throw e;
						}
					}
				};
			}

			//set the thread count, so all runnables run concurrently
			Thread interruptor = ThreadUtils.startDaemonThread(interruptorrunnable);
			ThreadUtils.parallelRunner().setThreadCount(threadcount).runRunnables(runnables);
			Thread.interrupted(); // clear flag for join
			interruptor.join();

			assertEquals(interruptcounter.get(), threadcount * INTERRUPT_COUNT);

			//check that the dynamic work pool works the same way
			for (int i = 0; i < runnables.length; i++) {
				semaphores[i].drainPermits();
			}
			interruptcounter.set(0);
			try (ThreadWorkPool wp = ThreadUtils.newDynamicWorkPool("DYN-INT-TEST-")) {
				for (Runnable run : runnables) {
					wp.offer(() -> run.run());
				}
				interruptor = ThreadUtils.startDaemonThread(interruptorrunnable);
			}
			Thread.interrupted(); // clear flag for join
			interruptor.join();

			assertEquals(interruptcounter.get(), threadcount * INTERRUPT_COUNT);

			//check that the fixed work pool works the same way
			for (int i = 0; i < runnables.length; i++) {
				semaphores[i].drainPermits();
			}
			interruptcounter.set(0);
			try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool(threadcount, "FIX-INT-TEST-")) {
				for (Runnable run : runnables) {
					wp.offer(() -> run.run());
				}
				interruptor = ThreadUtils.startDaemonThread(interruptorrunnable);
			}
			Thread.interrupted(); // clear flag for join
			interruptor.join();

			assertEquals(interruptcounter.get(), threadcount * INTERRUPT_COUNT);
		}
	}

	private static void testThreadPoolAPI(Supplier<? extends ThreadWorkPool> poolcreator, boolean direct)
			throws InterruptedException {
		assertFalse(Thread.currentThread().isInterrupted());

		//simple create and close
		try (ThreadWorkPool pool = poolcreator.get()) {
			System.out.println("ThreadUtilsTest.testThreadPoolAPI() test " + pool.getClass());
		}
		assertFalse(Thread.currentThread().isInterrupted());

		{
			boolean[] b = { false };
			try (ThreadWorkPool pool = poolcreator.get()) {
				pool.offer(() -> {
					b[0] = true;
				});
			}
			assertTrue(b[0]);
			assertFalse(Thread.currentThread().isInterrupted());
		}
		{
			//test that all tasks run
			AtomicInteger counter = new AtomicInteger();
			try (ThreadWorkPool pool = poolcreator.get()) {
				for (int i = 0; i < 50; i++) {
					pool.offer(() -> {
						//some yield or minor sleep to encourage different scheduling
						Thread.yield();
						counter.incrementAndGet();
					});
				}
			}
			assertEquals(counter.get(), 50);
			assertFalse(Thread.currentThread().isInterrupted());
		}
		{
			//test resetting
			AtomicInteger counter = new AtomicInteger();
			try (ThreadWorkPool pool = poolcreator.get()) {
				for (int i = 0; i < 50; i++) {
					pool.offer(() -> {
						//some yield or minor sleep to encourage different scheduling
						Thread.yield();
						counter.incrementAndGet();
					});
				}
				pool.reset();
				assertEquals(counter.get(), 50);
				for (int i = 0; i < 50; i++) {
					pool.offer(() -> {
						//some yield or minor sleep to encourage different scheduling
						Thread.yield();
						counter.incrementAndGet();
					});
				}
			}
			assertEquals(counter.get(), 100);
		}

		if (!direct) {
			//test exiting
			ExecutionOrderer orderer = new ExecutionOrderer();
			orderer.addSection("started");
			orderer.addSection("exitcalled");
			orderer.addSection("taskexiting");
			try (ThreadWorkPool pool = poolcreator.get()) {
				pool.offer(() -> {
					orderer.enter("started");
					orderer.enter("taskexiting");
				});
				pool.exit();
				orderer.enter("exitcalled");
			}
		}

	}

}
