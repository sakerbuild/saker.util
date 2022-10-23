package testing.saker.build.tests.utils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import saker.util.function.ThrowingRunnable;
import saker.util.io.IOUtils;
import saker.util.thread.BooleanLatch;
import saker.util.thread.ThreadUtils;
import saker.util.thread.ThreadUtils.ThreadWorkPool;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class BooleanLatchTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		simpleCheckSignal();

		checkCurrentThreadInterrupted();

		checkSingleConcurrent();

		checkMultiSignal();
	}

	private static void checkMultiSignal() throws InterruptedException {
		try (ThreadWorkPool wp = ThreadUtils.newDynamicWorkPool("BooleanLatchTest-")) {
			BooleanLatch latch = BooleanLatch.newBooleanLatch();
			final int threadcount = 10;
			CountDownLatch cd = new CountDownLatch(threadcount);
			for (int i = 0; i < threadcount; i++) {
				wp.offer(() -> {
					cd.countDown();
					latch.await();
					assertTrue(latch.isSignalled());
				});
			}
			cd.await();
			latch.signal();
		}
	}

	private static void checkSingleConcurrent() throws Throwable {
		for (int i = 0; i < 100; i++) {
			BooleanLatch latch = BooleanLatch.newBooleanLatch();
			runOnSeparateThread(() -> {
				latch.await();
				assertTrue(latch.isSignalled());
			}, () -> {
				latch.signal();
			});
		}
	}

	private static void checkCurrentThreadInterrupted() throws Exception {
		BooleanLatch latch = BooleanLatch.newBooleanLatch();

		Thread.currentThread().interrupt();
		assertException(InterruptedException.class, () -> latch.await());

		Thread.currentThread().interrupt();
		assertException(InterruptedException.class, () -> latch.await(10, TimeUnit.SECONDS));

		Thread.currentThread().interrupt();
		latch.signal();
		latch.await();
		assertTrue(Thread.currentThread().isInterrupted());

		assertTrue(latch.await(10, TimeUnit.SECONDS));
		assertTrue(Thread.currentThread().isInterrupted());

		latch.awaitUninterruptibly();
		assertTrue(Thread.currentThread().isInterrupted());

		assertTrue(latch.awaitUninterruptibly(10, TimeUnit.SECONDS));
		assertTrue(Thread.currentThread().isInterrupted());

		//clear the flag for the caller
		Thread.interrupted();
	}

	private static void simpleCheckSignal() throws Exception {
		BooleanLatch latch = BooleanLatch.newBooleanLatch();
		assertFalse(latch.isSignalled());
		latch.signal();
		assertTrue(latch.isSignalled());
	}

	private static void runOnSeparateThread(ThrowingRunnable run) throws Throwable {
		runOnSeparateThread(run, null);
	}

	private static void runOnSeparateThread(ThrowingRunnable run, ThrowingRunnable currentthreadrun) throws Throwable {
		Throwable[] exc = { null };
		Thread t = new Thread(() -> {
			try {
				run.run();
			} catch (Throwable e) {
				exc[0] = e;
			}
		});
		t.start();
		if (currentthreadrun != null) {
			currentthreadrun.run();
		}
		t.join();
		IOUtils.throwExc(exc[0]);
	}

}
