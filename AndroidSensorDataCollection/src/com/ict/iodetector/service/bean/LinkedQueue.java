package com.ict.iodetector.service.bean;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class LinkedQueue<E> {
	private static final int QUEUE_LENGTH = 5;
	private Queue<E> queue;
	private int length = QUEUE_LENGTH;

	public LinkedQueue(int length) {
		queue = new LinkedList<E>();
		this.length = length;
	}

	/**
	 * @param result
	 */
	public void enQueue(E result) {
		if (queue.size() >= length) {
			queue.poll();
		}
		queue.offer(result);
	}

	public E deQueue() {
		return queue.poll();
	}

	public E peek(){
		return queue.peek();
	}
	
	/**
	 * @return
	 */
	public E getLast() {
		//System.out.println("size:" + queue.size());
		return get(queue.size() - 1);
	}

	/**
	 * @param index
	 * @return
	 */
	public E get(int index) {
		Iterator<E> it = queue.iterator();
		int i = 0;

		while (it.hasNext() && i < index) {
			it.next();
			i++;
		}
		return it.next();
	}
	
	public void remove(int index) {
		Queue<E> newQueue = new LinkedList<E>();
		Iterator<E> it = queue.iterator();
		int i = 0;
		while(it.hasNext()) {
			E element = it.next();
			if(i != index) {
				newQueue.add(element);
			}
			i ++;
		}
		this.queue = newQueue;
		
	}

	/**
	 * @return
	 */
	public int size() {
		return queue.size();
	}
}
