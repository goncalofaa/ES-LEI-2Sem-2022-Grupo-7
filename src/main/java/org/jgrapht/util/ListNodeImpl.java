package org.jgrapht.util;

import org.jgrapht.util.DoublyLinkedList.ListNode;

public class ListNodeImpl <V>
implements
ListNode<V>
{
	private final V value;
	private DoublyLinkedList<V> list = null;
	private ListNodeImpl<V> next = null;
	private ListNodeImpl<V> prev = null;
	
	/**
	 * Creates new list node
	 *
	 * @param value the value this list node stores
	 */
	ListNodeImpl(V value)
	{
	    this.value = value;
	}
	
	public DoublyLinkedList<V> getList() {
		return list;
	}

	public void setNext(ListNodeImpl<V> next) {
		this.next = next;
	}

	public void setPrev(ListNodeImpl<V> prev) {
		this.prev = prev;
	}

	public void setList(DoublyLinkedList<V> list) {
		this.list = list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
	    if (list == null) {
	        return " - " + value + " - "; // not in a list
	} else {
	    return prev.value + " -> " + value + " -> " + next.value;
	    }
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public V getValue()
	{
	    return value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListNodeImpl<V> getNext()
	{
	    return next;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListNodeImpl<V> getPrev()
	{
	    return prev;
	}
}
