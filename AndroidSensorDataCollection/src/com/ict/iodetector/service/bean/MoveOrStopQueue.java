package com.ict.iodetector.service.bean;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MoveOrStopQueue {
	private Queue<Long> moveOrStopQueue;
	private int stopCount;
	private int moveCount;
	private long stopPeriod;
	public MoveOrStopQueue(){
		moveOrStopQueue = new LinkedList<Long>();
	}
	/**
	 * @return moveOrStopQueue
	 */
	public Queue<Long> getMoveOrStopQueue(){
		return moveOrStopQueue;
	}
	/**
	 * @param uiTime
	 */
	public void enMoveOrStopQueue(Long uiTime) {
		moveOrStopQueue.offer(uiTime);
	}
	/**
	 * @return
	 */
	public Long deMoveOrStopQueue() {
		return moveOrStopQueue.poll();
	}
	/**
	 * @return
	 */
	public int getSize() {
		return moveOrStopQueue.size();
	}
	/**
	 * @return countsOfStop
	 */
//	public int getStopCount() {
//		Iterator<Long> iteratorMoveOrStop = moveOrStopQueue.iterator();
//		int countsOfStop = 0;
//		long lastTime = 0;
//		long beforeLast = 0;
//		if(!iteratorMoveOrStop.hasNext()){
//			return 0;
//		}
//		while(getTotalTime() > 240000){
//			deMoveOrStopQueue();
//		}
//		iteratorMoveOrStop = moveOrStopQueue.iterator();
//		lastTime = iteratorMoveOrStop.next().longValue();
//		while (iteratorMoveOrStop.hasNext()) {
//			beforeLast = iteratorMoveOrStop.next().longValue();
////			System.out.println("beforeLast" + beforeLast);
//			if((beforeLast - lastTime) > 7000){
//				countsOfStop ++;
////				System.out.println("counts of stop" + countsOfStop);
//			}
//			lastTime = beforeLast;
//		}
//		return countsOfStop;
//	}
	public int getStopCount() {
		return stopCount;
	}
	public void calculateNumbers() {
		Iterator<Long> iteratorMoveOrStop = moveOrStopQueue.iterator();
		long lastTime = 0;
		long beforeLast = 0;
		stopCount = 0;
		moveCount = 0;
		stopPeriod = 0;
		if(!iteratorMoveOrStop.hasNext()){
			stopCount = 0;
			moveCount = 0;
		}
//		while(getTotalTime() > 240000){
//			deMoveOrStopQueue();
//		}
		while(getTotalTime() > 360000){
			deMoveOrStopQueue();
		}
		iteratorMoveOrStop = moveOrStopQueue.iterator();
		if(!iteratorMoveOrStop.hasNext()) {
			return;
		}
		lastTime = iteratorMoveOrStop.next().longValue();
		while (iteratorMoveOrStop.hasNext()) {
			beforeLast = iteratorMoveOrStop.next().longValue();
//			System.out.println("beforeLast" + beforeLast);
			if(Math.abs(beforeLast - lastTime) < 5000){
				moveCount ++;
			} else {
				stopCount ++;
				stopPeriod += (Math.abs(beforeLast - lastTime));
			}
			lastTime = beforeLast;
		}
	}
	public long getTotalTime() {
		Iterator<Long> it = moveOrStopQueue.iterator();
		long totalPeriod = 0;
		long begin = 0,after = 0;
		if(it.hasNext()) {
			begin = it.next().longValue();
		}
		while (it.hasNext()) {
			after = it.next().longValue();
			if(after - begin < 5000){
				totalPeriod += (after - begin);
			}
			begin = after;
		}
		return totalPeriod;
	}
	public long getTotalTimeWithStop() {
		Iterator<Long> it = moveOrStopQueue.iterator();
		long totalPeriod = 0;
		long begin = 0,after = 0;
		if(it.hasNext()) {
			begin = it.next().longValue();
		}
		while (it.hasNext()) {
			after = it.next().longValue();
			totalPeriod += (after - begin);
			begin = after;
		}
		return totalPeriod;
	}
	public int getMoveCount() {
		return moveCount;
	}
	public long getStopPeriod() {
		return stopPeriod;
	}
//	public int getMoveCount() {
//		Iterator<Long> it = moveOrStopQueue.iterator();
//		int countOfMove = 0;
//		
//		long totalPeriod = 0;
//		long begin = 0,after = 0;
//		if(it.hasNext()) {
//			begin = it.next().longValue();
//		}
//		while (it.hasNext() && totalPeriod < 240000) {
//			after = it.next().longValue();
//			totalPeriod += (after - begin);
//			begin = after;
//			countOfMove ++;
//		}
//		
//		return countOfMove;
//	}

}
