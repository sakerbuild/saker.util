package testing.saker.build.tests.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import saker.util.ObjectUtils;
import saker.util.StringUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class CharSplitIteratorTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) {
		assertEquals(
				ObjectUtils.addAll(new ArrayList<>(), ObjectUtils.transformIterator(
						StringUtils.splitCharSequenceIterator("hello,there", ','), Objects::toString)),
				listOf("hello", "there"));
		assertEquals(
				ObjectUtils
						.addAll(new ArrayList<>(),
								ObjectUtils.transformIterator(
										StringUtils.splitCharSequenceIterator("hello,there,", ','), Objects::toString)),
				listOf("hello", "there", ""));
		assertEquals(
				ObjectUtils
						.addAll(new ArrayList<>(),
								ObjectUtils.transformIterator(
										StringUtils.splitCharSequenceIterator("hello,,there", ','), Objects::toString)),
				listOf("hello", "", "there"));
		assertEquals(
				ObjectUtils
						.addAll(new ArrayList<>(),
								ObjectUtils.transformIterator(
										StringUtils.splitCharSequenceIterator(",hello,there", ','), Objects::toString)),
				listOf("", "hello", "there"));

	}

}
