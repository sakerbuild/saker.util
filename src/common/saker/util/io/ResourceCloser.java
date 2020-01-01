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

import java.io.Closeable;
import java.io.IOException;

import saker.util.ConcurrentPrependAccumulator;

/**
 * Utility class for holding resources to be closed later.
 * <p>
 * This class collects {@link AutoCloseable} instances to be closed later. This class is useful when users need to keep
 * track of unclosed resources and close them all later.
 * <p>
 * The closeables added to a closer will be closed in reverse order. I.e. the last one added will be closed first.
 * <p>
 * This class is thread-safe. Closeable can be added concurrently, and the class can be closed concurrently, multiple
 * times.
 * <p>
 * For each closeable added to this class they will be closed exactly once when {@link #close()} is called.
 * <p>
 * It is recommended that instances are used with try-with-resources statement.
 */
public class ResourceCloser implements Closeable {
	private ConcurrentPrependAccumulator<AutoCloseable> accumulator;

	/**
	 * Creates a new instance without any enclosed closeables.
	 */
	public ResourceCloser() {
		accumulator = new ConcurrentPrependAccumulator<>();
	}

	/**
	 * Creates a new instance initialized with the given closeables.
	 * 
	 * @param closeables
	 *            The closeables.
	 */
	public ResourceCloser(AutoCloseable... closeables) {
		accumulator = new ConcurrentPrependAccumulator<>(closeables);
	}

	/**
	 * Creates a new instance initialized with the given closeables.
	 * 
	 * @param closeables
	 *            The closeables.
	 */
	public ResourceCloser(Iterable<? extends AutoCloseable> closeables) {
		accumulator = new ConcurrentPrependAccumulator<>(closeables);
	}

	/**
	 * Adds the closeables to this resource closer.
	 * 
	 * @param closeables
	 *            The closeables.
	 */
	public void add(AutoCloseable... closeables) {
		for (AutoCloseable ac : closeables) {
			accumulator.add(ac);
		}
	}

	/**
	 * Adds the closeables to this resource closer.
	 * 
	 * @param closeables
	 *            The closeables.
	 */
	public void add(Iterable<? extends AutoCloseable> closeables) {
		for (AutoCloseable ac : closeables) {
			accumulator.add(ac);
		}
	}

	/**
	 * Clears any closeables in this resource closer without actually closing the contained closeables.
	 */
	public void clearWithoutClosing() {
		accumulator.clear();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Any exceptions thrown by the added closeables will be rethrown as {@link IOException} instances.
	 */
	@Override
	public void close() throws IOException {
		IOUtils.close(accumulator.clearAndIterator());
	}

}
