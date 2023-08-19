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
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import saker.util.ImmutableUtils;
import saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ImmutableNavigableSetTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) {
		{
			NavigableSet<Integer> actualset = ObjectUtils.newTreeSet(0, 1, 3, 4, 9, 10, 14);
			NavigableSet<Integer> reverseset = new TreeSet<>(Comparator.reverseOrder());
			reverseset.addAll(actualset);

			testSetRange(actualset);
			testSetRange(reverseset);
		}
		{
			//singleton checks
			NavigableSet<Integer> singleactualset = ObjectUtils.newTreeSet(5);
			NavigableSet<Integer> singlereverseset = new TreeSet<>(Comparator.reverseOrder());
			singlereverseset.addAll(singleactualset);
			testSetRange(singleactualset);
			testSetRange(singlereverseset);

			assertSetEquality(singleactualset, ImmutableUtils.singletonNavigableSet(5, singleactualset.comparator()));
			assertSetEquality(singlereverseset, ImmutableUtils.singletonNavigableSet(5, singlereverseset.comparator()));
		}

		assertTrue(ImmutableUtils.singletonNavigableSet("a", String::compareToIgnoreCase).contains("A"));
		assertTrue(ImmutableUtils.singletonNavigableSet("a", String::compareToIgnoreCase).contains("a"));

		assertSetEquality(new TreeSet<>(), ImmutableUtils.emptyNavigableSet(null));
		assertSetEquality(Collections.emptyNavigableSet(), ImmutableUtils.emptyNavigableSet(null));
		assertSetEquality(new TreeSet<>(Comparator.reverseOrder()),
				ImmutableUtils.emptyNavigableSet(Comparator.reverseOrder()));
		assertSetEquality(new TreeSet<>(Comparator.naturalOrder()),
				ImmutableUtils.emptyNavigableSet(Comparator.naturalOrder()));
		assertSetEquality(new TreeSet<>(Comparator.reverseOrder().reversed()),
				ImmutableUtils.emptyNavigableSet(Comparator.reverseOrder().reversed()));
	}

	private static void testSetRange(NavigableSet<Integer> set) {
		int start = -5;
		int end = 20;

		System.out.println("ImmutableNavigableSetTest.testSetRange() " + set + " cmp: " + set.comparator());

		testSets(set, ImmutableUtils.unmodifiableNavigableSet(set), start, end);
		testSets(set, ImmutableUtils.makeImmutableNavigableSet(set), start, end);

		Integer[] elemsarray = set.toArray(new Integer[0]);
		Integer[] elemslargearray = set.toArray(new Integer[set.size() + 128]);

		testSets(set, ImmutableUtils.unmodifiableNavigableSet(elemsarray, set.comparator()), start, end);
		testSets(set, ImmutableUtils.unmodifiableNavigableSet(elemslargearray, 0, set.size(), set.comparator()), start,
				end);
		testSets(set, ImmutableUtils.unmodifiableNavigableSet(Arrays.asList(elemsarray), set.comparator()), start, end);
		testSets(set, ImmutableUtils.makeImmutableNavigableSet(elemsarray, set.comparator()), start, end);
	}

	protected static void testSets(NavigableSet<Integer> actualset, NavigableSet<Integer> utilset, int start, int end) {
		assertSetEquality(utilset, actualset);
		for (int i = start; i < end; i++) {
			try {
				assertEquals(utilset.contains(i), actualset.contains(i));
				assertEquals(utilset.floor(i), actualset.floor(i));
				assertEquals(utilset.lower(i), actualset.lower(i));
				assertEquals(utilset.ceiling(i), actualset.ceiling(i));
				assertEquals(utilset.higher(i), actualset.higher(i));

				assertSetEqualityImpl(utilset.headSet(i), actualset.headSet(i));
				assertSetEqualityImpl(utilset.tailSet(i), actualset.tailSet(i));
				assertSetEquality(utilset.headSet(i, false), actualset.headSet(i, false));
				assertSetEquality(utilset.headSet(i, true), actualset.headSet(i, true));
				assertSetEquality(utilset.tailSet(i, false), actualset.tailSet(i, false));
				assertSetEquality(utilset.tailSet(i, true), actualset.tailSet(i, true));
			} catch (Throwable e) {
				System.out.println("With value: " + i);
				throw e;
			}
			for (int j = i; j < end; j++) {
				int l = i;
				int r = j;
				Comparator<? super Integer> comparator = ObjectUtils.getComparator(utilset);
				if (comparator.compare(l, r) > 0) {
					//use reverse order if the values compare otherwise
					l = j;
					r = i;
				}

				assertSetEqualityImpl(utilset.subSet(l, r), actualset.subSet(l, r));
				assertSetEquality(utilset.subSet(l, false, r, false), actualset.subSet(l, false, r, false));
				assertSetEquality(utilset.subSet(l, true, r, false), actualset.subSet(l, true, r, false));
				assertSetEquality(utilset.subSet(l, false, r, true), actualset.subSet(l, false, r, true));
				assertSetEquality(utilset.subSet(l, true, r, true), actualset.subSet(l, true, r, true));
			}
		}
	}

	public static void assertSetEquality(NavigableSet<?> left, NavigableSet<?> right) {
		try {
			assertSetEqualityImpl(left, right);
		} catch (Throwable e) {
			System.out.println("Comparing set " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}

		try {
			assertSetEqualityImpl(left.descendingSet(), right.descendingSet());
		} catch (Throwable e) {
			System.out.println("Comparing descending set of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}

		try {
			assertSetEqualityImpl(left.descendingSet().descendingSet(), right.descendingSet().descendingSet());
		} catch (Throwable e) {
			System.out.println("Comparing descending 2 set of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}
	}

	private static void assertSetEqualityImpl(SortedSet<?> left, SortedSet<?> right) throws AssertionError {
		try {
			boolean lempty = left.isEmpty();
			assertEquals(lempty, right.isEmpty());
			if (!lempty) {
				assertEquals(left.first(), right.first());
				assertEquals(left.last(), right.last());
			}
			int lsize = left.size();
			assertEquals(lsize, right.size());

			assertEquals(left.comparator(), right.comparator());
			assertEquals(left, right);
			assertEquals(left.toArray(), right.toArray());
			assertEquals(left.toArray(new Object[lsize]), right.toArray(new Object[lsize]));

			Object[] larray = new Object[lsize + 2];
			Object[] rarray = new Object[lsize + 2];
			Arrays.fill(larray, "f");
			Arrays.fill(rarray, "f");
			assertEquals(left.toArray(larray), right.toArray(rarray));

			assertTrue(ObjectUtils.collectionOrderedEquals(left, right));

			compareIterators(left.iterator(), right.iterator());

			if (left instanceof NavigableSet && right instanceof NavigableSet) {
				NavigableSet<?> lnavset = (NavigableSet<?>) left;
				NavigableSet<?> rnavset = (NavigableSet<?>) right;
				compareIterators(lnavset.descendingIterator(), rnavset.descendingIterator());
			}
		} catch (Throwable e) {
			System.out.println("Equality checkings sets of " + left + " (" + left.getClass().getSimpleName() + " / "
					+ left.comparator() + ") - " + right + " (" + right.getClass().getSimpleName() + " / "
					+ right.comparator() + ")");
			throw e;
		}
	}

	public static void compareIterators(Iterator<?> l, Iterator<?> r) {
		while (l.hasNext()) {
			assertTrue(r.hasNext());

			Object n1 = l.next();
			Object n2 = r.next();
			assertEquals(n1, n2);
		}
		assertFalse(r.hasNext());
	}

}
