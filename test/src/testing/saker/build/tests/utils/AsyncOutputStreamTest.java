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

import saker.util.function.ThrowingRunnable;
import saker.util.io.AsyncOutputStream;
import saker.util.io.IOUtils;
import saker.util.io.UnsyncByteArrayOutputStream;
import saker.util.thread.ExceptionThread;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class AsyncOutputStreamTest extends SakerTestCase {
	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		ThreadGroup threadgroup = new ThreadGroup("tg");
		ExceptionThread runnerthread = new ExceptionThread(threadgroup, (ThrowingRunnable) () -> {
			try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();) {
				try (AsyncOutputStream os = new AsyncOutputStream(baos)) {
					for (int i = 0; i < 255; i++) {
						os.write(i);
						if (i % 3 == 0) {
							Thread.yield();
						}
					}
				}
				byte[] result = baos.toByteArray();
				for (int i = 0; i < 255; i++) {
					assertEquals(result[i], (byte) i);
				}
			}
		});
		IOUtils.throwExc(runnerthread.joinGetException());
		threadgroup.destroy();
	}

}
