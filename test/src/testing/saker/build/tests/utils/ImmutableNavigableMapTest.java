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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.util.ImmutableUtils;
import saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ImmutableNavigableMapTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) {
		{
			NavigableMap<Integer, String> actualmap = new TreeMap<>();
			actualmap.put(0, "0");
			actualmap.put(1, "1");
			actualmap.put(3, "3");
			actualmap.put(4, "4");
			actualmap.put(9, "9");
			actualmap.put(10, "10");
			actualmap.put(14, "13");
			NavigableMap<Integer, String> reversemap = new TreeMap<>(Comparator.reverseOrder());
			reversemap.putAll(actualmap);

			testMapRange(actualmap);
			testMapRange(reversemap);
		}

		{
			//singleton checks
			NavigableMap<Integer, String> actualmap = new TreeMap<>();
			actualmap.put(0, "0");
			NavigableMap<Integer, String> reversemap = new TreeMap<>(Comparator.reverseOrder());
			reversemap.putAll(actualmap);

			assertMapEquality(actualmap, ImmutableUtils.singletonNavigableMap(0, "0", actualmap.comparator()));
			assertMapEquality(reversemap, ImmutableUtils.singletonNavigableMap(0, "0", reversemap.comparator()));
		}

		assertEquals(ImmutableUtils.singletonNavigableMap("a", 1, String::compareToIgnoreCase).get("A"), 1);
		assertEquals(ImmutableUtils.singletonNavigableMap("a", 1, String::compareToIgnoreCase).get("a"), 1);

		assertMapEquality(new TreeMap<>(), ImmutableUtils.emptyNavigableMap(null));
		assertMapEquality(Collections.emptyNavigableMap(), ImmutableUtils.emptyNavigableMap(null));
		assertMapEquality(new TreeMap<>(Comparator.reverseOrder()),
				ImmutableUtils.emptyNavigableMap(Comparator.reverseOrder()));
		assertMapEquality(new TreeMap<>(Comparator.reverseOrder().reversed()),
				ImmutableUtils.emptyNavigableMap(Comparator.reverseOrder().reversed()));
		assertMapEquality(new TreeMap<>(Comparator.naturalOrder()),
				ImmutableUtils.emptyNavigableMap(Comparator.naturalOrder()));
	}

	private static void testMapRange(NavigableMap<Integer, String> map) {
		int start = -5;
		int end = 20;

		System.out.println("ImmutableNavigableMapTest.testMapRange() " + map + " cmp: " + map.comparator());

		testMaps(map, ImmutableUtils.unmodifiableNavigableMap(map), start, end);
		testMaps(map, ImmutableUtils.makeImmutableNavigableMap(map), start, end);

		Integer[] keysarray = map.keySet().toArray(new Integer[0]);
		String[] valsarray = map.values().toArray(new String[0]);

		testMaps(map, ImmutableUtils.makeImmutableNavigableMap(keysarray, valsarray, map.comparator()), start, end);
		testMaps(map, ImmutableUtils.makeImmutableNavigableMap(Arrays.asList(keysarray), Arrays.asList(valsarray),
				map.comparator()), start, end);
		testMaps(map, ImmutableUtils.unmodifiableNavigableMap(keysarray, valsarray, map.comparator()), start, end);
		testMaps(map, ImmutableUtils.unmodifiableNavigableMap(Arrays.asList(keysarray), Arrays.asList(valsarray),
				map.comparator()), start, end);
	}

	protected static void testMaps(NavigableMap<Integer, String> actualmap, NavigableMap<Integer, String> utilmap,
			int start, int end) {
		assertMapEquality(actualmap, utilmap);

		for (int i = start; i < end; i++) {
			try {
				assertEquals(utilmap.floorKey(i), actualmap.floorKey(i));
				assertEquals(utilmap.lowerKey(i), actualmap.lowerKey(i));
				assertEquals(utilmap.ceilingKey(i), actualmap.ceilingKey(i));
				assertEquals(utilmap.higherKey(i), actualmap.higherKey(i));
				assertEquals(utilmap.floorEntry(i), actualmap.floorEntry(i));
				assertEquals(utilmap.lowerEntry(i), actualmap.lowerEntry(i));
				assertEquals(utilmap.ceilingEntry(i), actualmap.ceilingEntry(i));
				assertEquals(utilmap.higherEntry(i), actualmap.higherEntry(i));

				assertMapEquality(utilmap.headMap(i, false), actualmap.headMap(i, false));
				assertMapEquality(utilmap.headMap(i, true), actualmap.headMap(i, true));
				assertMapEquality(utilmap.tailMap(i, false), actualmap.tailMap(i, false));
				assertMapEquality(utilmap.tailMap(i, true), actualmap.tailMap(i, true));
			} catch (Throwable e) {
				System.out.println("With value: " + i);
				throw e;
			}
			for (int j = i; j < end; j++) {
				int l = i;
				int r = j;
				Comparator<? super Integer> comparator = ObjectUtils.getComparator(utilmap);
				if (comparator.compare(l, r) > 0) {
					//use reverse order if the values compare otherwise
					l = j;
					r = i;
				}

				assertMapEquality(utilmap.subMap(l, false, r, false), actualmap.subMap(l, false, r, false));
				assertMapEquality(utilmap.subMap(l, true, r, false), actualmap.subMap(l, true, r, false));
				assertMapEquality(utilmap.subMap(l, false, r, true), actualmap.subMap(l, false, r, true));
				assertMapEquality(utilmap.subMap(l, true, r, true), actualmap.subMap(l, true, r, true));
			}
		}
	}

	private static void assertMapEquality(NavigableMap<?, ?> left, NavigableMap<?, ?> right) {
		try {
			assertMapEqualityImpl(left, right);
		} catch (Throwable e) {
			System.out.println("Comparing map " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}

		try {
			assertMapEqualityImpl(left.descendingMap(), right.descendingMap());
		} catch (Throwable e) {
			System.out.println("Comparing descending map of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}

		try {
			assertMapEqualityImpl(left.descendingMap().descendingMap(), right.descendingMap().descendingMap());
		} catch (Throwable e) {
			System.out.println("Comparing descending 2 map of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}
	}

	private static void assertMapEqualityImpl(NavigableMap<?, ?> left, NavigableMap<?, ?> right) {
		try {
			boolean lempty = left.isEmpty();
			assertEquals(lempty, right.isEmpty());
			if (!lempty) {
				assertEquals(left.firstKey(), right.firstKey());
				assertEquals(left.lastKey(), right.lastKey());
				assertEquals(left.firstEntry(), right.firstEntry());
				assertEquals(left.lastEntry(), right.lastEntry());
			}
			int lsize = left.size();
			assertEquals(lsize, right.size());

			assertEquals(left.comparator(), right.comparator());
			assertEquals(left, right);

			assertEquals(left.keySet(), right.keySet());
			assertEquals(left.entrySet(), right.entrySet());
			ImmutableNavigableSetTest.assertSetEquality(left.navigableKeySet(), right.navigableKeySet());
			ImmutableNavigableSetTest.assertSetEquality(left.descendingKeySet(), right.descendingKeySet());

			ImmutableNavigableSetTest.compareIterators(left.entrySet().iterator(), right.entrySet().iterator());
			ImmutableNavigableSetTest.compareIterators(left.values().iterator(), right.values().iterator());
		} catch (Throwable e) {
			System.out.println("Equality checkings maps of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}
	}

}
