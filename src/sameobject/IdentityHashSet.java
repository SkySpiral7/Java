package src.sameobject;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * A very simple set wrapper for IdentityHashMap
 * @see IdentityHashMap
 * @param <E> the data type to be stored
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Set<E> {
    private transient final IdentityHashMap<E, Boolean> dataMap;
    
	/**
	 * The no-arg construct creates an empty set.
	 */
	public IdentityHashSet(){super(); dataMap = new IdentityHashMap<E, Boolean>();}
	public IdentityHashSet(int expectedMaxSize){super(); dataMap = new IdentityHashMap<E, Boolean>(expectedMaxSize);}
	public IdentityHashSet(Collection<? extends E> initialElements){this(initialElements.size()); this.addAll(initialElements);}
	public IdentityHashSet(E[] initialElements){this(Arrays.asList(initialElements));}

	@Override
	public int size() {
		return dataMap.size();
	}

	@Override
	public boolean contains(Object elementToFind) {
		return dataMap.containsKey(elementToFind);
	}

	@Override
	public Iterator<E> iterator() {
		return dataMap.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return dataMap.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] destinationArray) {
		return dataMap.keySet().toArray(destinationArray);
	}

	@Override
	public boolean add(E newElement) {
		return dataMap.put(newElement, Boolean.TRUE);
	}

	@Override
	public boolean remove(Object elementToRemove) {
		return dataMap.remove(elementToRemove);
	}

	@Override
	public void clear() {
		dataMap.clear();
	}

}
