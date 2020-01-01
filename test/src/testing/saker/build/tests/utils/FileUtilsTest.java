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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import saker.util.io.FileUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class FileUtilsTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/file.txt"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/file.txt?some=query&strings=abc"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/file.txt#hash"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/file.txt#hash?some=query"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/file.txt?some=query&strings=abc#hash"), "file.txt");

		assertEquals(FileUtils.getLastPathNameFromURL("http://host/path/file.txt"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/path/file.txt?some=query&strings=abc"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/path/file.txt#hash"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/path/file.txt#hash?some=query"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("http://host/path/file.txt?some=query&strings=abc#hash"),
				"file.txt");

		assertEquals(FileUtils.getLastPathNameFromURL("file.txt"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("file.txt?some=query&strings=abc"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("file.txt#hash"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("file.txt#hash?some=query"), "file.txt");
		assertEquals(FileUtils.getLastPathNameFromURL("file.txt?some=query&strings=abc#hash"), "file.txt");

		assertEquals(FileUtils.getLastPathNameFromURL(""), null);
		assertEquals(FileUtils.getLastPathNameFromURL("?some=query&strings=abc"), null);
		assertEquals(FileUtils.getLastPathNameFromURL("#hash"), null);
		assertEquals(FileUtils.getLastPathNameFromURL("#hash?some=query"), null);
		assertEquals(FileUtils.getLastPathNameFromURL("?some=query&strings=abc#hash"), null);

		assertEquals(FileUtils.getLastPathNameFromURL("http://example.com"), "example.com");
		assertEquals(FileUtils.getLastPathNameFromURL("http://example.com/"), null);

		assertEquals(FileUtils.splitPath("a//b"), new String[] { "a", "b" });
		assertEquals(FileUtils.splitPath("/a//b"), new String[] { "", "a", "b" });
		assertEquals(FileUtils.splitPath("a//b/"), new String[] { "a", "b" });

		Path filebytessamefile = Paths.get("FileUtilsTest/file.txt");
//				EnvironmentTestCase.getTestingBaseWorkingDirectory()
//				.resolve(getClass().getName().replace('.', '/')).resolve("file.txt");
		assertTrue(FileUtils.isFileBytesSame(filebytessamefile, "contents".getBytes()));
		assertFalse(FileUtils.isFileBytesSame(filebytessamefile, "contentx".getBytes()));
		assertFalse(FileUtils.isFileBytesSame(filebytessamefile, "othersize".getBytes()));
	}
}
