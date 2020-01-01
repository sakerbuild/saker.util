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

import java.security.SecureRandom;
import java.util.Map;

import saker.util.StringUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class StringUtilTest extends SakerTestCase {

	private SecureRandom random = new SecureRandom();

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		testNextNaturalOrder("a", "aa");
		testNextNaturalOrder("a", "b");

		for (int i = 0; i < 1000; i++) {
			byte[] buffer = new byte[(random.nextInt(16) + 1)];
			String hex = StringUtils.toHexString(buffer);
			byte[] rehex = StringUtils.parseHexString(hex);
			assertEquals(buffer, rehex);
		}

		for (int i = 0; i < 1000; i++) {
			int len = random.nextInt(10) + 1;
			String s = randomStringWithLength(len);
			String next = StringUtils.nextInNaturalOrder(s);
			assertTrue(s.compareTo(next) < 0);
			for (int l = -1; l <= 1; l++) {
				for (int j = 0; j < 100; j++) {
					String t = randomStringWithLength(len + l);
					if (t.equals(next) || t.equals(s)) {
						continue;
					}
					int ts = t.compareTo(s);
					if (ts < 0) {
						//the randomed is before s
						assertTrue(t.compareTo(next) < 0);
					} else {
						//the randomed is after s
						assertTrue(t.compareTo(next) > 0);
					}
				}
			}
		}

		assertTrue(StringUtils.isIntegralString("1"));
		assertTrue(StringUtils.isIntegralString("1123"));
		assertTrue(StringUtils.isIntegralString("+1123"));
		assertTrue(StringUtils.isIntegralString("-1123"));
		assertFalse(StringUtils.isIntegralString(""));
		assertFalse(StringUtils.isIntegralString(null));
		assertFalse(StringUtils.isIntegralString("a"));
		assertFalse(StringUtils.isIntegralString("+a"));
		assertFalse(StringUtils.isIntegralString("-a"));
		assertFalse(StringUtils.isIntegralString("-"));
		assertFalse(StringUtils.isIntegralString("+"));
		assertFalse(StringUtils.isIntegralString("1-"));
		assertFalse(StringUtils.isIntegralString("1+"));
		assertFalse(StringUtils.isIntegralString("--"));
		assertFalse(StringUtils.isIntegralString("-+"));
		assertFalse(StringUtils.isIntegralString("+-"));
		assertFalse(StringUtils.isIntegralString("++"));
	}

	private String randomStringWithLength(int len) {
		char[] chars = new char[len];
		for (int j = 0; j < chars.length; j++) {
			chars[j] = (char) random.nextInt((Character.MAX_VALUE) + 1);
		}
		String s = new String(chars);
		return s;
	}

	private static void testNextNaturalOrder(String s, String smallerthan) {
		assertTrue(s.compareTo(smallerthan) < 0, s + " - " + smallerthan);
		String next = StringUtils.nextInNaturalOrder(s);
		assertTrue(next.compareTo(smallerthan) < 0, s + " - " + next + " - " + smallerthan);
	}

}
