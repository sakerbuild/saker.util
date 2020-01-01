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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import saker.util.ReflectUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ReflectUtilsTest extends SakerTestCase {

	public enum MyEnum {
		FIRST_ENUM,
		SECOND_ENUM {
		};
	}

	private class InnerClass {

	}

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			Set<Class<?>> s = setOf(Thread.class, Runnable.class);
			ReflectUtils.reduceAssignableTypes(s);
			assertEquals(s, setOf(Thread.class));
		}
		{
			Set<Class<?>> s = setOf(List.class, Collection.class);
			ReflectUtils.reduceAssignableTypes(s);
			assertEquals(s, setOf(List.class));
		}
		{
			Set<Class<?>> s = setOf(Number.class, Float.class, Double.class);
			ReflectUtils.reduceAssignableTypes(s);
			assertEquals(s, setOf(Float.class, Double.class));
		}

		assertEquals(ReflectUtils.getInterfaces(List.class), setOf(List.class));

		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(List.class, List.class.getName()), List.class);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(List.class, Object.class.getName()), null);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(ArrayList.class, List.class.getName()), List.class);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(ArrayList.class, Collection.class.getName()),
				Collection.class);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(ArrayList.class, Iterable.class.getName()),
				Iterable.class);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(ArrayList.class, RandomAccess.class.getName()),
				RandomAccess.class);
		assertEquals(ReflectUtils.findInterfaceWithNameInHierarchy(ArrayList.class, ArrayList.class.getName()), null);

		assertEquals(ReflectUtils.findTypeWithNameInHierarchy(ArrayList.class, ArrayList.class.getName()),
				ArrayList.class);
		assertEquals(ReflectUtils.findTypeWithNameInHierarchy(ArrayList.class, List.class.getName()), List.class);
		assertEquals(ReflectUtils.findTypeWithNameInHierarchy(ArrayList.class, Object.class.getName()), Object.class);

		assertEquals(ReflectUtils.findClassWithNameInHierarchy(ArrayList.class, Object.class.getName()), Object.class);
		assertEquals(ReflectUtils.findClassWithNameInHierarchy(ArrayList.class, ArrayList.class.getName()),
				ArrayList.class);
		assertEquals(ReflectUtils.findClassWithNameInHierarchy(ArrayList.class, List.class.getName()), null);

		assertEquals(MyEnum.FIRST_ENUM.getClass(), MyEnum.class);
		assertNotEquals(MyEnum.SECOND_ENUM.getClass(), MyEnum.class);
		assertTrue(ReflectUtils.isEnumOrEnumAnonymous(MyEnum.FIRST_ENUM.getClass()));
		assertTrue(ReflectUtils.isEnumOrEnumAnonymous(MyEnum.SECOND_ENUM.getClass()));
		assertFalse(ReflectUtils.isEnumOrEnumAnonymous(Enum.class));

		defaultMethodTest();

		Map<Class<?>, Integer> intinheritancedistances = ReflectUtils.getAllInheritedTypesWithDistance(Integer.class);
		assertEquals(intinheritancedistances.get(Integer.class), 0);
		assertEquals(intinheritancedistances.get(Number.class), 1);
		assertEquals(intinheritancedistances.get(Comparable.class), 1);
		assertEquals(intinheritancedistances.get(Serializable.class), 2);
		assertEquals(intinheritancedistances.get(Object.class), 2);

		assertEquals(ReflectUtils.getPackageNameOf(ReflectUtilsTest.class), "testing.saker.build.tests.utils");
		assertEquals(ReflectUtils.getPackageNameOf(InnerClass.class), "testing.saker.build.tests.utils");
		assertEquals(ReflectUtils.getEnclosingCanonicalNameOf(ReflectUtilsTest.class),
				"testing.saker.build.tests.utils");
		assertEquals(ReflectUtils.getEnclosingCanonicalNameOf(InnerClass.class),
				ReflectUtilsTest.class.getCanonicalName());
	}

	public interface DefMethodItf {
		public default String defMethod() {
			return "default";
		}
	}

	public static class DefMethodImpl implements DefMethodItf {
		@Override
		public String defMethod() {
			return "impl";
		}

		public String callDefault(Method m) throws Throwable {
			return (String) ReflectUtils.invokeDefaultMethodOn(m, this);
		}
	}

	private static void defaultMethodTest() throws NullPointerException, AssertionError, Throwable {
		Method thedefaultmethod = ReflectUtils.getMethodAssert(DefMethodItf.class, "defMethod");

		assertEquals(new DefMethodImpl().defMethod(), "impl");

		//test that we can call the default method on an implementation externally
		assertEquals(ReflectUtils.invokeDefaultMethodOn(thedefaultmethod, new DefMethodImpl()), "default");

		//test that calling the defualt method from the scope of an implementation is possible.
		assertEquals(new DefMethodImpl().callDefault(thedefaultmethod), "default");

		//test that the default method is callable from the proxy
		//    create a new classloader so the proxy is defined in it instead of in the interface classloader 
		ClassLoader proxycl = new ClassLoader(DefMethodItf.class.getClassLoader()) {
		};
		DefMethodItf theproxy = (DefMethodItf) Proxy.newProxyInstance(proxycl, new Class<?>[] { DefMethodItf.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return ReflectUtils.invokeDefaultMethodOn(method, proxy);
					}
				});
		assertIdentityEquals(proxycl, theproxy.getClass().getClassLoader());
		assertEquals(theproxy.defMethod(), "default");
	}
}
