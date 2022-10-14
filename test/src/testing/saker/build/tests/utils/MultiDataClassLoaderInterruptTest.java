package testing.saker.build.tests.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import saker.util.ArrayIterator;
import saker.util.ArrayUtils;
import saker.util.classloader.ClassLoaderDataFinder;
import saker.util.classloader.JarClassLoaderDataFinder;
import saker.util.classloader.MultiDataClassLoader;
import saker.util.classloader.PathClassLoaderDataFinder;
import saker.util.io.ByteArrayRegion;
import saker.util.io.ByteRegion;
import saker.util.io.ByteSinkOutputStream;
import saker.util.io.ByteSource;
import saker.util.io.ByteSourceInputStream;
import saker.util.io.IOUtils;
import saker.util.io.InputStreamByteSource;
import saker.util.io.JarFileUtils;
import saker.util.io.StreamUtils;
import saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class MultiDataClassLoaderInterruptTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		Path path = Paths.get("MultiDataClassLoaderInterruptTest").toAbsolutePath().normalize();
		System.out.println("Path is: " + path);
		//preload these classes for the test, so they are loaded, and the interruption doesn't prevent the loading of them
		//this list has been discovered by attempting to run the test 
		@SuppressWarnings("unused")
		Object loadthis = ByteSource.class;
		loadthis = ByteRegion.class;
		loadthis = ByteSourceInputStream.class;
		loadthis = InputStreamByteSource.class;
		loadthis = UnsyncByteArrayOutputStream.class;
		loadthis = ByteSinkOutputStream.class;
		loadthis = ByteArrayRegion.class;
		loadthis = ByteArrayRegion.EMPTY; // EmptyByteArrayRegion
		loadthis = ArrayUtils.class;
		loadthis = StreamUtils.class;
		loadthis = IOUtils.class;
		loadthis = ArrayIterator.class;
		loadthis = JarClassLoaderDataFinder.class;
		loadthis = JarFileUtils.class;
		loadthis = StreamUtils.nullOutputStream(); // NullOutputStream

		try (ClassLoaderDataFinder cldf = new PathClassLoaderDataFinder(path)) {
			//this should succeed as a baseline
			assertNonNull(attemptLoadingHelloWorldClass(cldf));

			//this should succeed even if the current thread is interrupted
			Thread.currentThread().interrupt();
			assertNonNull(attemptLoadingHelloWorldClass(cldf));
			//the current thread should stay interrupted
			assertTrue(Thread.interrupted());
		}

		try (ClassLoaderDataFinder cldf = new JarClassLoaderDataFinder(
				Paths.get("MultiDataClassLoaderInterruptTest/hellocp.jar"))) {
			//this should succeed as a baseline
			assertNonNull(attemptLoadingHelloWorldClass(cldf));

			//this should succeed even if the current thread is interrupted
			Thread.currentThread().interrupt();
			assertNonNull(attemptLoadingHelloWorldClass(cldf));
			//the current thread should stay interrupted
			assertTrue(Thread.interrupted());
		}
	}

	private static Class<?> attemptLoadingHelloWorldClass(ClassLoaderDataFinder cldf) throws ClassNotFoundException {
		MultiDataClassLoader cl = new MultiDataClassLoader(cldf);
		Class<?> c = Class.forName("test.HelloWorld", false, cl);
		System.out.println("Loaded: " + c + " from " + cldf);
		return c;
	}

}
