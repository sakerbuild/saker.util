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
package saker.util.rmi.wrap;

import java.io.IOException;
import java.util.Objects;

import saker.apiextract.api.PublicApi;
import saker.rmi.io.RMIObjectInput;
import saker.rmi.io.RMIObjectOutput;
import saker.rmi.io.wrap.RMIWrapper;
import saker.util.io.ByteArrayRegion;
import saker.util.io.ByteRegion;

/**
 * {@link RMIWrapper} implementation for caching the length of the remotely transferred {@link ByteRegion}.
 * <p>
 * This wrapper will query the length of the transferred {@link ByteRegion}, and write that alongside the remote region
 * object. When it is deserialized, an instance of this wrapper will take place of the transferred object.
 * <p>
 * The purpose of this wrapper is to spare the remote method call cost of {@link #getLength()}. As that is a very
 * commonly called method, using this wrapper can increase performance.
 * <p>
 * The target type for this wrapper should be {@link ByteRegion}.
 */
@PublicApi
public class RMIByteRegionWrapper implements ByteRegion, RMIWrapper {
	private ByteRegion region;
	private int length;

	/**
	 * Creates a new instance.
	 * <p>
	 * Users shouldn't instantiate this class manually, but leave that to the RMI runtime.
	 */
	public RMIByteRegionWrapper() {
	}

	/**
	 * Creates a new instance for the given region.
	 * <p>
	 * Users shouldn't instantiate this class manually, but leave that to the RMI runtime.
	 * 
	 * @param region
	 *            The byte region.
	 * @throws NullPointerException
	 *             If the region is <code>null</code>.
	 */
	public RMIByteRegionWrapper(ByteRegion region) throws NullPointerException {
		Objects.requireNonNull(region, "byte region");
		this.region = region;
		this.length = region.getLength();
	}

	@Override
	public Object getWrappedObject() {
		return region;
	}

	@Override
	public Object resolveWrapped() {
		return this;
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		out.writeRemoteObject(region);
		out.writeInt(length);
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		region = (ByteRegion) in.readObject();
		length = in.readInt();
	}

	@Override
	public byte[] copy() {
		return region.copy();
	}

	@Override
	public byte[] copyArrayRegion(int offset, int length) {
		return region.copyArrayRegion(offset, length);
	}

	@Override
	public byte get(int index) {
		return region.get(index);
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public void put(int index, ByteArrayRegion bytes) {
		region.put(index, bytes);
	}

	@Override
	public void put(int index, byte b) {
		region.put(index, b);
	}

	@Override
	public String toString() {
		return region.toString();
	}
}
