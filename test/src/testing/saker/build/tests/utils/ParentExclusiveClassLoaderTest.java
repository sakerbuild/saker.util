package testing.saker.build.tests.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import saker.util.ReflectUtils;
import saker.util.classloader.ClassLoaderDataFinder;
import saker.util.classloader.MultiClassLoader;
import saker.util.classloader.MultiDataClassLoader;
import saker.util.classloader.ParentExclusiveClassLoader;
import saker.util.io.ByteArrayRegion;
import saker.util.io.ByteSource;
import saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ParentExclusiveClassLoaderTest extends SakerTestCase {

	public interface ParentItf {

	}

	public interface SubItf extends ParentItf {

	}

	public static class SubClass implements SubItf {
	}

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		ClassLoader parentcl = createClassLoaderForClasses((ClassLoader) null, ParentItf.class);
		ClassLoader subcl = createClassLoaderForClasses(parentcl, SubItf.class);
		ParentExclusiveClassLoader subclexclusib = new ParentExclusiveClassLoader(subcl);
		ClassLoader subclasscl = createClassLoaderForClasses(subclexclusib, SubClass.class);
		Class<?> subc = Class.forName(SubClass.class.getName(), true, subclasscl);

		//make sure its instantiateable
		ReflectUtils.newInstance(subc).toString();
		//direct parent interface is accessible
		Class.forName(SubItf.class.getName(), true, subclasscl);
		//the above parent is not accessible
		assertException(ClassNotFoundException.class, () -> Class.forName(ParentItf.class.getName(), true, subclasscl));
		//but it is again from the sub cl
		Class.forName(ParentItf.class.getName(), true, subcl);

		//make sure the reduction selects it appropriately
		assertEquals(MultiClassLoader.reduceClassLoaders(Arrays.asList(subclasscl, subclexclusib, subcl, parentcl)),
				setOf(subcl, subclasscl));
	}

	private static ClassLoader createClassLoaderForClasses(ClassLoader parent, Class<?>... classes) {
		Map<String, ByteArrayRegion> clresources = new TreeMap<>();
		for (Class<?> c : classes) {
			clresources.put(c.getName().replace('.', '/') + ".class", ReflectUtils.getClassBytesUsingClassLoader(c));
		}
		return new MultiDataClassLoader(parent, new MemoryClassLoaderDataFinder(clresources));
	}

	private static class MemoryClassLoaderDataFinder implements ClassLoaderDataFinder {
		private Map<String, ByteArrayRegion> resourceBytes;

		public MemoryClassLoaderDataFinder(Map<String, ByteArrayRegion> resourceBytes) {
			this.resourceBytes = resourceBytes;
		}

		@Override
		public Supplier<ByteSource> getResource(String name) {
			ByteArrayRegion got = resourceBytes.get(name);
			if (got == null) {
				return null;
			}
			return () -> new UnsyncByteArrayInputStream(got);
		}

	}
}
