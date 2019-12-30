package testing.saker.build.tests.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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
		ThreadUtils.setInheritableDefaultThreadFactor(8);

		Set<Object> res = Collections.synchronizedSet(new HashSet<>());
		ThreadUtils.runParallelItems(ImmutableUtils.asUnmodifiableArrayList(null, 1, 2, 3), s -> {
			res.add(s);
		});
		assertEquals(res, setOf(null, 1, 2, 3));

		//test that interruption doesnt cause the work pool to throw an exception on close
		try (ThreadWorkPool wp = ThreadUtils.newDynamicWorkPool()) {
			wp.offer(() -> System.out.println("ThreadUtilsTest.runTest() 1"));
			Thread.currentThread().interrupt();
		}
		//clear interrupt flag for further tests
		Thread.interrupted();
		try (ThreadWorkPool wp = ThreadUtils.newFixedWorkPool()) {
			wp.offer(() -> System.out.println("ThreadUtilsTest.runTest() 2"));
			Thread.currentThread().interrupt();
		}

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
