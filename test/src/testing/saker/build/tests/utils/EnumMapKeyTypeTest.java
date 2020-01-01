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

import java.util.EnumMap;
import java.util.Map;

import saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class EnumMapKeyTypeTest extends SakerTestCase {
	private enum MyEnum {
		VALUE,
		ANONYMVALUE {
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		assertEquals(ObjectUtils.getEnumMapEnumType(new EnumMap<>(MyEnum.class)), MyEnum.class);
		assertEquals(ObjectUtils.getEnumMapEnumType(new EnumMap<>((Class) MyEnum.VALUE.getClass())), MyEnum.class);

		//assert that creating an enum map with the class of an anonymous inner enum results in an exception
		assertException(Exception.class, () -> new EnumMap<>((Class) MyEnum.ANONYMVALUE.getClass()));

		EnumMap<MyEnum, Object> enmapvalue = new EnumMap<>(MyEnum.class);
		enmapvalue.put(MyEnum.VALUE, 0);
		assertEquals(ObjectUtils.getEnumMapEnumType(enmapvalue), MyEnum.class);

		EnumMap<MyEnum, Object> enmapanonymvalue = new EnumMap<>(MyEnum.class);
		enmapvalue.put(MyEnum.ANONYMVALUE, 0);
		assertEquals(ObjectUtils.getEnumMapEnumType(enmapanonymvalue), MyEnum.class);
	}

}
