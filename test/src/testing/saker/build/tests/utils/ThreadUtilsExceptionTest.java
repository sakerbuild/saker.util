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

		assertException(ParallelExecutionFailedException.class, () -> ThreadUtils.runParallelRunnables(abortthrowing,
				abortthrowing, failer, abortthrowing, abortthrowing));
	}

}
