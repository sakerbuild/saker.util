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
package saker.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class SetFromMap<E> implements Set<E>, Externalizable {
	private final class AddAllForeachConsumer implements Consumer<E> {
		boolean modified = false;

		@Override
		public void accept(E t) {
			if (SetFromMap.this.add(t)) {
				modified = true;
			}
		}
	}

	private static final long serialVersionUID = 1L;

	private Map<E, ? super Boolean> map;

	/**
	 * For {@link Externalizable}.
	 */
	public SetFromMap() {
	}

	public SetFromMap(Map<E, ? super Boolean> map) {
		this.map = map;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return map.keySet().toArray(a);
	}

	@Override
	public boolean add(E e) {
		return map.put(e, Boolean.TRUE) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) != null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return map.keySet().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		AddAllForeachConsumer consumer = new AddAllForeachConsumer();
		c.forEach(consumer);
		return consumer.modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return map.keySet().retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return map.keySet().removeAll(c);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return map.keySet().removeIf(filter);
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		map.keySet().forEach(action);
	}

	@Override
	public Stream<E> stream() {
		return map.keySet().stream();
	}

	@Override
	public Stream<E> parallelStream() {
		return map.keySet().parallelStream();
	}

	@Override
	public Spliterator<E> spliterator() {
		return map.keySet().spliterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		map = (Map<E, ? super Boolean>) in.readObject();
	}

	@Override
	public int hashCode() {
		return ObjectUtils.setHash(map.keySet());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Set)) {
			return false;
		}
		return ObjectUtils.setsEqual(this, (Set<?>) obj);
	}

	@Override
	public String toString() {
		return ObjectUtils.collectionToString(this);
	}

}
