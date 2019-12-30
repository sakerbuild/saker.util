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
