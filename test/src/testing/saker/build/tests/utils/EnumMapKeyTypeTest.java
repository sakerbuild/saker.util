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
