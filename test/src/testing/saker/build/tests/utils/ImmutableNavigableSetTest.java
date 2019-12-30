package testing.saker.build.tests.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableSet;

import saker.util.ImmutableUtils;
import saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ImmutableNavigableSetTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) {
		NavigableSet<Integer> actualset = ObjectUtils.newTreeSet(0, 1, 3, 4, 9, 10, 14);

		testSets(actualset, ImmutableUtils.unmodifiableNavigableSet(actualset), -5, 20);
		testSets(actualset, ImmutableUtils.makeImmutableNavigableSet(actualset), -5, 20);

		testSets(actualset, ImmutableUtils.unmodifiableNavigableSet(actualset.toArray(new Integer[0])), -5, 20);
		testSets(actualset, ImmutableUtils.makeImmutableNavigableSet(actualset.toArray(new Integer[0])), -5, 20);
	}

	protected static void testSets(NavigableSet<Integer> actualset, NavigableSet<Integer> set, int start, int end) {
		assertSetEquality(set, actualset);
		for (int i = start; i < end; i++) {
			assertEquals(set.contains(i), actualset.contains(i));
			assertEquals(set.floor(i), actualset.floor(i));
			assertEquals(set.lower(i), actualset.lower(i));
			assertEquals(set.ceiling(i), actualset.ceiling(i));
			assertEquals(set.higher(i), actualset.higher(i));

			assertSetEquality(set.headSet(i, false), actualset.headSet(i, false));
			assertSetEquality(set.headSet(i, true), actualset.headSet(i, true));
			assertSetEquality(set.tailSet(i, false), actualset.tailSet(i, false));
			assertSetEquality(set.tailSet(i, true), actualset.tailSet(i, true));
			for (int j = i; j < end; j++) {
				assertSetEquality(set.subSet(i, false, j, false), actualset.subSet(i, false, j, false));
				assertSetEquality(set.subSet(i, true, j, false), actualset.subSet(i, true, j, false));
				assertSetEquality(set.subSet(i, false, j, true), actualset.subSet(i, false, j, true));
				assertSetEquality(set.subSet(i, true, j, true), actualset.subSet(i, true, j, true));
			}
		}
	}

	private static void assertSetEquality(NavigableSet<?> left, NavigableSet<?> right) {
		assertSetEqualityImpl(left, right);
		assertSetEqualityImpl(left.descendingSet(), right.descendingSet());
	}

	private static void assertSetEqualityImpl(NavigableSet<?> left, NavigableSet<?> right) throws AssertionError {
		boolean lempty = left.isEmpty();
		assertEquals(lempty, right.isEmpty());
		if (!lempty) {
			assertEquals(left.first(), right.first());
			assertEquals(left.last(), right.last());
		}
		assertEquals(left.size(), right.size());

		assertEquals(left.comparator(), right.comparator());
		assertEquals(left, right);
		assertEquals(left.toArray(), right.toArray());
		int size = left.size();
		assertEquals(left.toArray(new Object[size]), right.toArray(new Object[size]));

		Object[] larray = new Object[size + 2];
		Object[] rarray = new Object[size + 2];
		Arrays.fill(larray, "f");
		Arrays.fill(rarray, "f");
		assertEquals(left.toArray(larray), right.toArray(rarray));

		assertTrue(ObjectUtils.collectionOrderedEquals(left, right));
	}

}
