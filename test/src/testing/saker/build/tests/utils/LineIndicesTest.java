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

import saker.util.StringUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class LineIndicesTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		assertEquals(StringUtils.getLineIndexMap("singleline"), new int[] { 0 });
		assertEquals(StringUtils.getLineIndexMap("\n"), new int[] { 0, 1 });
		assertEquals(StringUtils.getLineIndexMap("data\n"), new int[] { 0, 5 });
		assertEquals(StringUtils.getLineIndexMap("data\ndata"), new int[] { 0, 5 });
		assertEquals(StringUtils.getLineIndexMap("data\ndata\n"), new int[] { 0, 5, 10 });

		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 0), 0);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 4), 0);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 5), 1);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 7), 1);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 10), 2);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 11), 2);
		assertEquals(StringUtils.getLineIndex(new int[] { 0, 5, 10 }, 99), 2);

		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 0), 0);
		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 4), 4);
		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 5), 0);
		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 7), 2);
		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 10), 0);
		assertEquals(StringUtils.getLinePositionIndex(new int[] { 0, 5, 10 }, 20), 10);
	}

}
