package testing.saker.build.tests.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import saker.util.function.LazySupplier;
import saker.util.thread.ThreadUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class LazySupplierTest extends SakerTestCase {
	private static final AtomicInteger counter = new AtomicInteger();

	public static String getter() {
		counter.getAndIncrement();
		return "abc";
	}

	public static String sleepingGetter() {
		counter.getAndIncrement();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return "abc";
	}

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::getter);
			assertEquals(s.get(), "abc");
			assertEquals(s.get(), "abc");
			assertEquals(counter.getAndSet(0), 1);
		}
		{
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::getter);
			assertEquals(s.get(), "abc");
			assertEquals(s.getIfComputed(), "abc");
			assertEquals(counter.getAndSet(0), 1);
		}
		{
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::getter);
			assertEquals(s.getIfComputed(), null);
			assertEquals(s.get(), "abc");
			assertEquals(s.getIfComputed(), "abc");
			assertEquals(counter.getAndSet(0), 1);
		}
		{
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::getter);
			assertEquals(s.getIfComputedPrevent(), null);
			assertEquals(s.get(), null);
			assertEquals(s.getIfComputed(), null);
			assertEquals(counter.getAndSet(0), 0);
		}
		{
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::getter);
			assertEquals(s.get(), "abc");
			assertEquals(s.getIfComputedPrevent(), "abc");
			assertEquals(s.get(), "abc");
			assertEquals(s.getIfComputed(), "abc");
			assertEquals(counter.getAndSet(0), 1);
		}

		{
			//detect recursive call to the initializer
			LazySupplier<String> s = LazySupplier.of(ls -> ls.get());
			assertException(IllegalThreadStateException.class, () -> s.get());
		}

		{
			LazySupplier<String> s = LazySupplier.of(new Supplier<String>() {
				int c = 0;

				@Override
				public String get() {
					if (c++ == 0) {
						throw new FailException();
					}
					return "val";
				}
			});
			//fails the first time, but can calculate it the second time
			assertException(FailException.class, () -> s.get());
			assertEquals(s.get(), "val");
		}

		{
			AtomicInteger cc = new AtomicInteger();
			LazySupplier<String> s = LazySupplier.of(LazySupplierTest::sleepingGetter);
			List<Integer> dummyitems = Arrays.asList(new Integer[16]);
			ThreadUtils.runParallelItems(dummyitems, i -> {
				assertEquals(s.get(), "abc");
				cc.getAndIncrement();
			});
			assertEquals(s.get(), "abc");
			assertEquals(counter.getAndSet(0), 1);
			assertEquals(cc.get(), dummyitems.size());
		}
		{
			assertEquals(123 * 2, LazySupplier.of(123, i -> i * 2).get());
		}
	}

	private static class FailException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
