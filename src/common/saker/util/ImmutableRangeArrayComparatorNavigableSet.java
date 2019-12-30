package saker.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

class ImmutableRangeArrayComparatorNavigableSet<E> extends ImmutableRangeArrayNavigableSet<E> {
	private static final long serialVersionUID = 1L;

	private Comparator<? super E> comparator;

	/**
	 * For {@link Externalizable}.
	 */
	public ImmutableRangeArrayComparatorNavigableSet() {
	}

	protected ImmutableRangeArrayComparatorNavigableSet(Object[] items, int start, int end,
			Comparator<? super E> comparator) {
		super(items, start, end);
		this.comparator = comparator;
	}

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		comparator = (Comparator<? super E>) in.readObject();
	}

}
