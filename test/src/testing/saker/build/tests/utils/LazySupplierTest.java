package testing.saker.build.tests.utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import saker.util.function.LazySupplier;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class LazySupplierTest extends SakerTestCase {
	private static final AtomicInteger counter = new AtomicInteger();

	public static String getter() {
		counter.getAndIncrement();
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
	}

}
