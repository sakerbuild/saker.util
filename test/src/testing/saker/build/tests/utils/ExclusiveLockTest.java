package testing.saker.build.tests.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import saker.util.function.ThrowingRunnable;
import saker.util.io.IOUtils;
import saker.util.thread.ThreadUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ExclusiveLockTest extends SakerTestCase {

	private int counter;

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		Lock lock = ThreadUtils.newExclusiveLock();
		//multiple times to check they can be locked-unlocked
		tryBasicLockFunctions(lock);
		tryBasicLockFunctions(lock);
		runOnSeparateThread(() -> {
			try {
				tryBasicLockFunctions(lock);
			} catch (Throwable e) {
				throw fail(e);
			}
		});
		tryBasicLockFunctions(lock);
		runOnSeparateThread(() -> {
			try {
				tryBasicLockFunctions(lock);
			} catch (Throwable e) {
				throw fail(e);
			}
		});
		tryBasicLockFunctions(lock);

		//just dummy items for mass test
		List<int[]> items = Arrays.asList(new int[64]);
		ThreadUtils.runParallelItems(items, v -> {
			Thread.sleep(50);
			lock.lock();
			try {
				int val = this.counter;
				//just some delay between the read and the write operations
				Thread.sleep(50);
				counter = val + 1;
			} finally {
				lock.unlock();
			}
		});
		assertEquals(counter, items.size());
	}

	private static void tryBasicLockFunctions(Lock lock) throws Throwable {
		Condition condition = lock.newCondition();

		lock.lock();
		//shouldn't be able to lock it on another thread
		runOnSeparateThread(() -> {
			assertFalse(lock.tryLock());
			assertFalse(lock.tryLock(100, TimeUnit.MILLISECONDS));
			//not holding the lock, cant unlock here
			assertException(IllegalMonitorStateException.class, () -> lock.unlock());
			assertException(IllegalMonitorStateException.class, () -> condition.signal());
			assertException(IllegalMonitorStateException.class, () -> condition.signalAll());
			//not holding the lock, can't wait for the condition
			assertException(IllegalMonitorStateException.class, () -> condition.await());
			assertException(IllegalMonitorStateException.class, () -> condition.awaitUninterruptibly());
		});
		//reentrancy test
		assertException(IllegalThreadStateException.class, () -> lock.tryLock());
		assertException(IllegalThreadStateException.class, () -> lock.tryLock(100, TimeUnit.DAYS));
		assertException(IllegalThreadStateException.class, () -> lock.lock());
		assertException(IllegalThreadStateException.class, () -> lock.lockInterruptibly());

		condition.signal();
		condition.signalAll();

		assertFalse(condition.await(0, TimeUnit.MILLISECONDS));

		//check that the basic condition mechanism works
		Thread conditiont = new Thread(() -> {
			lock.lock();
			try {
				condition.signalAll();
			} finally {
				lock.unlock();
			}
		});
		conditiont.start();
		condition.await();
		conditiont.join();

		//unlocking it
		lock.unlock();

		//not holding, cant unlock
		assertException(IllegalMonitorStateException.class, () -> lock.unlock());
	}

	private static void runOnSeparateThread(ThrowingRunnable run) throws Throwable {
		Throwable[] exc = { null };
		Thread t = new Thread(() -> {
			try {
				run.run();
			} catch (Throwable e) {
				exc[0] = e;
			}
		});
		t.start();
		t.join();
		IOUtils.throwExc(exc[0]);
	}

}
