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

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.util.ImmutableUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ImmutableNavigableMapTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) {
		NavigableMap<Integer, String> actualmap = new TreeMap<>();
		actualmap.put(0, "0");
		actualmap.put(1, "1");
		actualmap.put(3, "3");
		actualmap.put(4, "4");
		actualmap.put(9, "9");
		actualmap.put(10, "10");
		actualmap.put(14, "13");

		test(actualmap, ImmutableUtils.unmodifiableNavigableMap(actualmap), -5, 20);
		test(actualmap, ImmutableUtils.makeImmutableNavigableMap(actualmap), -5, 20);
	}

	protected static void test(NavigableMap<Integer, String> actualmap, NavigableMap<Integer, String> map, int start,
			int end) {
		assertEquals(actualmap.keySet(), map.keySet());
		assertEquals(actualmap.entrySet(), map.entrySet());
		for (int i = start; i < end; i++) {
			assertEquals(map.floorKey(i), actualmap.floorKey(i));
			assertEquals(map.lowerKey(i), actualmap.lowerKey(i));
			assertEquals(map.ceilingKey(i), actualmap.ceilingKey(i));
			assertEquals(map.higherKey(i), actualmap.higherKey(i));
			assertEquals(map.floorEntry(i), actualmap.floorEntry(i));
			assertEquals(map.lowerEntry(i), actualmap.lowerEntry(i));
			assertEquals(map.ceilingEntry(i), actualmap.ceilingEntry(i));
			assertEquals(map.higherEntry(i), actualmap.higherEntry(i));

			assertEquals(map.headMap(i, false), actualmap.headMap(i, false));
			assertEquals(map.headMap(i, true), actualmap.headMap(i, true));
			assertEquals(map.tailMap(i, false), actualmap.tailMap(i, false));
			assertEquals(map.tailMap(i, true), actualmap.tailMap(i, true));
			for (int j = i; j < end; j++) {
				assertEquals(map.subMap(i, false, j, false), actualmap.subMap(i, false, j, false));
				assertEquals(map.subMap(i, true, j, false), actualmap.subMap(i, true, j, false));
				assertEquals(map.subMap(i, false, j, true), actualmap.subMap(i, false, j, true));
				assertEquals(map.subMap(i, true, j, true), actualmap.subMap(i, true, j, true));
			}
		}
	}

}
