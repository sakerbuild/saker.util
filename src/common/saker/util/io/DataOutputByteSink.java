package saker.util.io;

import java.io.DataOutput;
import java.io.IOException;

import saker.rmi.annot.invoke.RMIExceptionRethrow;

/**
 * Interface extending {@link DataOutput} and {@link ByteSink}.
 * <p>
 * The interface is present to have proper RMI annotation for implementations that possibly implement both of the
 * specified interfaces.
 */
public interface DataOutputByteSink extends ByteSink, DataOutput {
	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public default void write(int b) throws IOException {
		ByteSink.super.write(b);
	}

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void write(byte[] b) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void write(byte[] b, int off, int len) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeBoolean(boolean v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeByte(int v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeShort(int v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeChar(int v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeInt(int v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeLong(long v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeFloat(float v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeDouble(double v) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeBytes(String s) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeChars(String s) throws IOException;

	@Override
	@RMIExceptionRethrow(RemoteIOException.class)
	public void writeUTF(String s) throws IOException;
}