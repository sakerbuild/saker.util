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
package saker.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

class LockedByteSource extends InputStream implements ByteSource {
	protected final ByteSource in;
	protected final ReentrantLock lock = new ReentrantLock();

	public LockedByteSource(ByteSource in) {
		this.in = in;
	}

	@Override
	public int read(ByteRegion buffer) throws IOException {
		lock.lock();
		try {
			return in.read(buffer);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int read() throws IOException {
		lock.lock();
		try {
			return in.read();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ByteArrayRegion read(int count) throws IOException {
		lock.lock();
		try {
			return in.read(count);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long writeTo(ByteSink out) throws IOException {
		lock.lock();
		try {
			return in.writeTo(out);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		lock.lock();
		try {
			return in.read(ByteArrayRegion.wrap(b));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		lock.lock();
		try {
			return in.read(ByteArrayRegion.wrap(b, off, len));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long skip(long n) throws IOException {
		lock.lock();
		try {
			return in.skip(n);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int available() throws IOException {
		lock.lock();
		try {
			return super.available();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		lock.lock();
		try {
			in.close();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void mark(int readlimit) {
		lock.lock();
		try {
			super.mark(readlimit);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void reset() throws IOException {
		lock.lock();
		try {
			super.reset();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean markSupported() {
		lock.lock();
		try {
			return super.markSupported();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + in + "]";
	}
}
