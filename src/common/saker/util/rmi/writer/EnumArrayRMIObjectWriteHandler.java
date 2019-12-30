package saker.util.rmi.writer;

import saker.rmi.exception.RMIObjectTransferFailureException;
import saker.rmi.io.writer.ArrayComponentRMIObjectWriteHandler;
import saker.rmi.io.writer.RMIObjectWriteHandler;

/**
 * RMI write handler implementation for writing arrays that only have enum elements.
 * <p>
 * If an element is not an instance of {@link Enum}, {@link RMIObjectTransferFailureException} will be thrown.
 */
public class EnumArrayRMIObjectWriteHandler extends ArrayComponentRMIObjectWriteHandler {
	/**
	 * Singleton instance of {@link EnumArrayRMIObjectWriteHandler}.
	 */
	public static final EnumArrayRMIObjectWriteHandler INSTANCE = new EnumArrayRMIObjectWriteHandler();

	/**
	 * Constructs a new instance.
	 */
	public EnumArrayRMIObjectWriteHandler() {
		super(RMIObjectWriteHandler.enumWriter());
	}
}
