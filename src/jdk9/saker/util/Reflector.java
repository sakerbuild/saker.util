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
package saker.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Reference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import saker.util.ObjectUtils;

class Reflector {
	private Reflector() {
		throw new UnsupportedOperationException();
	}

	static MethodHandle getDefaultMethodHandle(Method method, Class<?> declaringClass)
			throws ReflectiveOperationException {
		return MethodHandles.lookup().findSpecial(declaringClass, method.getName(),
				MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
	}

	static Object getModule(Class<?> type) {
		if (type == null) {
			return null;
		}
		return type.getModule();
	}

	public static Class<?> lookupAccessClass(MethodHandles.Lookup lookup, Class<?> type) throws IllegalAccessException {
		return lookup.accessClass(type);
	}

	public static void reachabilityFence(Object o) {
		Reference.reachabilityFence(o);
	}
}
