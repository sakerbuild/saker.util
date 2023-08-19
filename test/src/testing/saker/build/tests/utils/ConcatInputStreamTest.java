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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import saker.util.io.ConcatInputStream;
import saker.util.io.StreamUtils;
import saker.util.io.UnsyncByteArrayInputStream;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ConcatInputStreamTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			//single stream
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()));
			assertEquals(StreamUtils.readStreamStringFully(is), "abc");
		}
		{
			//simple multi stream
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new UnsyncByteArrayInputStream("def".getBytes()));
			assertEquals(StreamUtils.readStreamStringFully(is), "abcdef");
		}
		{
			//skip null stream
			ConcatInputStream is = new ConcatInputStream(null, new UnsyncByteArrayInputStream("abc".getBytes()), null,
					new UnsyncByteArrayInputStream("def".getBytes()), null);
			assertEquals(StreamUtils.readStreamStringFully(is), "abcdef");
		}
		{
			//read one by one
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new UnsyncByteArrayInputStream("def".getBytes()));
			assertEquals(is.read(), (int) 'a');
			assertEquals(is.read(), (int) 'b');
			assertEquals(is.read(), (int) 'c');

			assertEquals(is.read(), (int) 'd');
			assertEquals(is.read(), (int) 'e');
			assertEquals(is.read(), (int) 'f');
		}
		{
			//read with length of each stream
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new UnsyncByteArrayInputStream("def".getBytes()));
			byte[] buf = new byte[3];
			is.read(buf);
			assertEquals(buf, "abc".getBytes());
			is.read(buf);
			assertEquals(buf, "def".getBytes());
		}
		{
			//read with shorter buffers
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new UnsyncByteArrayInputStream("def".getBytes()));
			byte[] buf = new byte[2];
			is.read(buf);
			assertEquals(buf, "ab".getBytes());

			Arrays.fill(buf, (byte) '\0');
			is.read(buf);
			assertEquals(buf, "c\0".getBytes());

			Arrays.fill(buf, (byte) '\0');
			is.read(buf);
			assertEquals(buf, "de".getBytes());

			Arrays.fill(buf, (byte) '\0');
			is.read(buf);
			assertEquals(buf, "f\0".getBytes());
		}
		{
			//read with longer buffers
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new UnsyncByteArrayInputStream("def".getBytes()));
			byte[] buf = new byte[4];
			is.read(buf);
			assertEquals(buf, "abc\0".getBytes());

			Arrays.fill(buf, (byte) '\0');
			is.read(buf);
			assertEquals(buf, "def\0".getBytes());
		}
		{
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new ThrowingInputStream());

			assertEquals(is.read(), (int) 'a');
			assertEquals(is.read(), (int) 'b');
			assertEquals(is.read(), (int) 'c');

			assertException(IOException.class, is::read);
		}
		{
			ConcatInputStream is = new ConcatInputStream(new UnsyncByteArrayInputStream("abc".getBytes()),
					new ThrowingInputStream());
			byte[] buf = new byte[4];
			int read = is.read(buf);
			assertEquals(read, 3);

			assertEquals(buf, "abc\0".getBytes());
		}
	}

	private static class ThrowingInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			throw new IOException("exception");
		}

	}

}
