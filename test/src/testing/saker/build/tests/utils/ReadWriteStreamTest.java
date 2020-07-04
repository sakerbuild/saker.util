package testing.saker.build.tests.utils;

import java.io.IOException;
import java.util.Map;

import saker.util.io.ByteArrayRegion;
import saker.util.io.ByteRegion;
import saker.util.io.ReadWriteBufferOutputStream;
import saker.util.thread.ThreadUtils;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ReadWriteStreamTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		try (ReadWriteBufferOutputStream os = new ReadWriteBufferOutputStream()) {
			os.write("abc".getBytes());
			assertEquals(os.read(3).toString(), "abc");
		}

		try (ReadWriteBufferOutputStream os = new ReadWriteBufferOutputStream()) {
			os.write("abc".getBytes());
			ByteArrayRegion bar = ByteArrayRegion.allocate(3);
			os.read(bar);
			assertEquals(bar.toString(), "abc");
		}
		try (ReadWriteBufferOutputStream os = new ReadWriteBufferOutputStream()) {
			ByteArrayRegion bar = ByteArrayRegion.allocate(3);
			ThreadUtils.startDaemonThread(() -> {
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
				}
				try {
					os.write("abc".getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			os.read(new ByteRegion() {
				@Override
				public void put(int index, ByteArrayRegion bytes)
						throws UnsupportedOperationException, IndexOutOfBoundsException, NullPointerException {
					bar.put(index, bytes);
				}

				@Override
				public int getLength() {
					return bar.getLength();
				}

				@Override
				public byte get(int index) throws IndexOutOfBoundsException {
					return bar.get(index);
				}

				@Override
				public byte[] copyArrayRegion(int offset, int length)
						throws IndexOutOfBoundsException, IllegalArgumentException {
					return bar.copyArrayRegion(offset, length);
				}
			});
			assertEquals(bar.toString(), "abc");
		}
	}

}
