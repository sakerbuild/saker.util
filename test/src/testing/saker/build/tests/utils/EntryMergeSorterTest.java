package testing.saker.build.tests.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import saker.util.ConcurrentEntryMergeSorter;
import saker.util.ConcurrentEntryMergeSorter.MatchingKeyPolicy;
import saker.util.ImmutableUtils;
import saker.util.ObjectUtils;
import saker.util.function.Functionals;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class EntryMergeSorterTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		test1();
		test2();
	}

	private void test1() throws AssertionError {
		Random random = new Random(8674756321L);

		for (int i = 0; i < 10000; i++) {
			ConcurrentEntryMergeSorter<Integer, Integer> sorter = new ConcurrentEntryMergeSorter<>();
			int collcount = random.nextInt(3) + 1;
			TreeMap<Integer, Integer> latestexpected = new TreeMap<>();
			TreeMap<Integer, Integer> earliestexpected = new TreeMap<>();
			for (int j = 0; j < collcount; j++) {
				NavigableMap<Integer, Integer> map = new TreeMap<>();
				int elemcount = random.nextInt(10);
				for (int k = 0; k < elemcount; k++) {
					int key = Math.abs(random.nextInt(32));
					int value = random.nextInt();
					Integer mapprev = map.putIfAbsent(key, value);
					if (mapprev == null) {
						latestexpected.put(key, value);
						earliestexpected.putIfAbsent(key, value);
					}
				}
				sorter.add(map);
			}
			Map<Integer, Integer> latest = sorter.createImmutableNavigableMap(MatchingKeyPolicy.CHOOSE_LATEST);
			Map<Integer, Integer> earliest = sorter.createImmutableNavigableMap(MatchingKeyPolicy.CHOOSE_EARLIEST);
			assertEquals(latest, latestexpected);
			assertEquals(earliest, earliestexpected);
		}
	}

	private void test2() {
		Random r = new Random(123456L);
		for (int i = 0; i < 1000; i++) {
			ConcurrentEntryMergeSorter<Integer, Integer> sorter = new ConcurrentEntryMergeSorter<>();
			int itcount = r.nextInt(90) + 10;
			for (int j = 0; j < itcount; j++) {
				List<Entry<Integer, Integer>> l = new ArrayList<>();
				int elemc = r.nextInt(90) + 10;
				for (int k = 0; k < elemc; k++) {
					int randind = r.nextInt();
					l.add(ImmutableUtils.makeImmutableMapEntry(randind, randind));
				}
				l.sort(Functionals.entryKeyNaturalComparator());

				// remove duplicates
				Iterator<Entry<Integer, Integer>> it = l.iterator();
				Entry<Integer, Integer> first = it.next();
				while (it.hasNext()) {
					Entry<Integer, Integer> e = it.next();
					if (e.getKey().equals(first.getKey())) {
						it.remove();
						continue;
					}
					first = e;
				}
				assertTrue(ObjectUtils.isStrictlySorted(l, Functionals.entryKeyNaturalComparator()));

				sorter.add(l);
			}
			sorter.createImmutableNavigableMap();
		}
	}

}
