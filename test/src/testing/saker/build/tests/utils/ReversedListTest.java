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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import saker.util.ObjectUtils;
import saker.util.function.Functionals;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ReversedListTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		ArrayList<Object> al = ObjectUtils.newArrayList(0, 1, 2, 3);

		List<Object> rev = ObjectUtils.reversedList(al);

		assertEquals(rev, listOf(3, 2, 1, 0));
		rev.add(-1);
		assertEquals(rev, listOf(3, 2, 1, 0, -1));
		rev.add(0, 4);
		assertEquals(rev, listOf(4, 3, 2, 1, 0, -1));

		rev.add(1, 3.5);
		assertEquals(rev, listOf(4, 3.5, 3, 2, 1, 0, -1));

		rev.add(rev.size(), -2);
		assertEquals(rev, listOf(4, 3.5, 3, 2, 1, 0, -1, -2));

		rev.set(0, 5);
		rev.set(1, 4);
		assertEquals(rev, listOf(5, 4, 3, 2, 1, 0, -1, -2));

		rev.addAll(Arrays.asList(-3, -4));
		assertEquals(rev, listOf(5, 4, 3, 2, 1, 0, -1, -2, -3, -4));

		rev.addAll(0, Arrays.asList(7, 6));
		assertEquals(rev, listOf(7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4));

		{
			Iterator<Object> it = rev.iterator();
			it.next();
			it.remove();
			assertEquals(rev, listOf(6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4));
		}
		{
			Iterator<Object> it = rev.iterator();
			it.next();
			it.next();
			it.remove();
			assertEquals(rev, listOf(6, 4, 3, 2, 1, 0, -1, -2, -3, -4));
		}
		{
			Iterator<Object> it = rev.iterator();
			it.forEachRemaining(Functionals.nullConsumer());
			it.remove();
			assertEquals(rev, listOf(6, 4, 3, 2, 1, 0, -1, -2, -3));
			assertEquals(al, listOf(-3, -2, -1, 0, 1, 2, 3, 4, 6));
		}

		assertEquals(rev.listIterator(1).next(), 4);
		assertEquals(rev.listIterator(1).previous(), 6);

		assertIdentityEquals(ObjectUtils.reversedList(rev), al);

		List<Object> sl = rev.subList(1, 4);
		assertEquals(sl, listOf(4, 3, 2));
		sl.add(0, 5);
		assertEquals(sl, listOf(5, 4, 3, 2));
		assertEquals(rev, listOf(6, 5, 4, 3, 2, 1, 0, -1, -2, -3));

		sl.clear();
		assertEquals(sl, listOf());
		assertEquals(rev, listOf(6, 1, 0, -1, -2, -3));

		rev.add(1);
		rev.add(2);
		assertEquals(rev, listOf(6, 1, 0, -1, -2, -3, 1, 2));

		assertEquals(rev.indexOf(6), 0);
		assertEquals(rev.indexOf(1), 1);
		assertEquals(rev.lastIndexOf(1), 6);

		assertEquals(rev.indexOf(123), -1);
	}

}
