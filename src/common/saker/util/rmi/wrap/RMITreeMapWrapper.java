package saker.util.rmi.wrap;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import saker.rmi.io.RMIObjectInput;
import saker.rmi.io.RMIObjectOutput;
import saker.rmi.io.wrap.RMIWrapper;

/**
 * {@link RMIWrapper} implementation that writes a {@link Map} instance and reads them as {@link TreeMap} instances.
 * <p>
 * The keys and values are written using {@link RMIObjectOutput#writeObject(Object)}.
 */
public class RMITreeMapWrapper implements RMIWrapper {
	private Map<?, ?> map;

	/**
	 * Creates a new instance.
	 * <p>
	 * Users shouldn't instantiate this class manually, but leave that to the RMI runtime.
	 */
	public RMITreeMapWrapper() {
	}

	/**
	 * Creates a new instance for a map.
	 * <p>
	 * Users shouldn't instantiate this class manually, but leave that to the RMI runtime.
	 * 
	 * @param map
	 *            The map.
	 */
	public RMITreeMapWrapper(Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public Object getWrappedObject() {
		return map;
	}

	@Override
	public Object resolveWrapped() {
		return map;
	}

	@Override
	public void writeWrapped(RMIObjectOutput out) throws IOException {
		if (map == null) {
			out.writeObject(CommonSentinel.NULL_INPUT);
			return;
		}
		for (Entry<?, ?> entry : map.entrySet()) {
			out.writeObject(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.writeObject(CommonSentinel.END_OF_OBJECTS);
	}

	@Override
	public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
		Object key = in.readObject();
		if (key == CommonSentinel.NULL_INPUT) {
			map = null;
			return;
		}
		TreeMap<Object, Object> tm = new TreeMap<>();
		while (true) {
			if (key == CommonSentinel.END_OF_OBJECTS) {
				break;
			}
			tm.put(key, in.readObject());
			key = in.readObject();
		}
		this.map = tm;
	}

}