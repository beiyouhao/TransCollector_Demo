package com.ict.iodetector.service.bean;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class MotionQueue {
	private Queue<Motion> mQueue;
	private Queue<Motion> sQueue;
    private Queue<Motion> totalQueue;
    private double dutyRatioOfTime;
    private double dutyRatioOfNumber;
	public MotionQueue() {
		mQueue = new LinkedList<Motion>();
		sQueue = new LinkedList<Motion>();
		totalQueue = new LinkedList<Motion>();
	}

	
	public Queue<Motion> getTotalQueue(){
		return totalQueue;
	}
	/**
	 * @param result
	 */
	public void enMQueue(Motion result) {
		mQueue.offer(result);
	}

	/**
	 * @return
	 */
	public void deMQueue() {
		mQueue.poll();
	}
	
	/**
	 * @param result
	 */
	public void enSQueue(Motion result) {
		sQueue.offer(result);
	}

	/**
	 * @return
	 */
	public void deSQueue() {
		sQueue.poll();
	}
	/**
	 * @param result
	 */
	public void enTotalQueue(Motion result) {
		totalQueue.offer(result);
	}

	/**
	 * @return
	 */
	public void deTotalQueue() {
		totalQueue.poll();
	}

	// return the start time of the one minute motion
	public long getStartSystemTimeOfmMotion() {
		Stack<Motion> allMotionStack = new Stack<Motion>();
		Iterator<Motion> it1 = mQueue.iterator();
		while(it1.hasNext()) {
			allMotionStack.push(it1.next());
		}
		if(allMotionStack.empty()) {
			return 0;
		}
		long startTime = 0;
		long totalPeriod = 0;
		while(!allMotionStack.empty()) {
			Motion topMotion = allMotionStack.pop();
			if((totalPeriod + topMotion.getPeriod()) >= 120 * 1000) {
				startTime = topMotion.getEndTime() - (120 * 1000 - totalPeriod);
				break;
			} else {
				totalPeriod += topMotion.getPeriod();
			}
		}
		while(mQueue.peek().getEndTime() < startTime) {
			deMQueue();
		}
		while(totalQueue.peek().getEndTime() < startTime) {
			deTotalQueue();
		}
		return startTime;
//		if (mQueue.size() == 0) {
//			return 0;
//		}
//
//		if (mQueue.size() == 1) {
//			if (mQueue.peek().getPeriod() > 80 * 1000)// if the size of queue is
//														// 1 and the period is
//														// more than 1 minute
//				return mQueue.peek().getEndTime() - 80 * 1000;
//			else
//				return mQueue.peek().getStartTime();
//		}
//
//		Iterator<Motion> it = mQueue.iterator();
//		long totalPeriod = 0;
//		Motion m = null;
//		while (it.hasNext()) {
//			m = it.next();
//			totalPeriod += m.getPeriod();
//		}
//
//		long diff = totalPeriod - mQueue.peek().getPeriod();
//		if (diff >= 80 * 1000) {// total period of the queue except the head is
//								// more than 1 minute
//			while(sQueue.element().getEndTime() < mQueue.element().getStartTime()){
//				sQueue.poll();
//			}
//			while(totalQueue.element().getEndTime() < mQueue.element().getStartTime()){
//				totalQueue.poll();
//			}
//			mQueue.poll();
//			return mQueue.peek().getStartTime() + diff - 80 * 1000;
//		} else
//			return mQueue.peek().getStartTime() + totalPeriod - 80 * 1000;
	}
	
	public void setDutyRatio() {
		double dutyRatioOfNumber = 0;
		double dutyRatioOfTime = 0;
		double dutyRatio = 0;
		if (totalQueue.size() == 0) {
			dutyRatioOfNumber = 404;
			dutyRatioOfTime = 404;
		} else {
			if (totalQueue.size() == 1) {
				if (totalQueue.peek().isMoveOrStop()){// if the size of queue is
															// 1 and the period is
															// more than 1 minute
					dutyRatioOfNumber = 1;
					dutyRatioOfTime = 1;
				} else{
					dutyRatioOfNumber = 0;
					dutyRatioOfTime = 0;
				}	
			} else {
				Iterator<Motion> it = totalQueue.iterator();
				long totalQueuePeriod = 0,movePeriod = 0;
				int moveNumber = 0,stopNumber = 0;
				boolean done = false;
				Motion t = null;
				while (it.hasNext() && (totalQueuePeriod < 80 * 1000)) {
//					System.out.println("����while");
					t = it.next();
					totalQueuePeriod += t.getPeriod();
//					System.out.println(t.isMoveOrStop());
					if(t.isMoveOrStop()){
//						System.out.println("�������ж� ");
						moveNumber ++;
						movePeriod += t.getPeriod();
						done = true;
					} else {
//						System.out.println("����ͣ�ж� ");
						stopNumber ++;
						done = false;
					}
				}
				
//				System.out.println("movePeriod: " + movePeriod);
				if(totalQueuePeriod < 80 * 1000) {
					dutyRatioOfNumber = 505;
					dutyRatioOfTime = 505;
				}else{
					if(done == true){
						movePeriod = movePeriod + 80 * 1000 - totalQueuePeriod; 
					}
					dutyRatioOfTime = (double)movePeriod / (80*1000.0);
					dutyRatioOfNumber = (double)moveNumber / (moveNumber + stopNumber);
				}	
			}
		}
		this.dutyRatioOfNumber = dutyRatioOfNumber;
		this.dutyRatioOfTime = dutyRatioOfTime;
	}
	

	public long getTotalMotionTime() {
		Iterator<Motion> it = mQueue.iterator();
		long totalPeriod = 0;
		Motion m = null;
		while (it.hasNext()) {
			m = it.next();
			totalPeriod += m.getPeriod();
		}

		return totalPeriod;
	}

	public int mSize() {
		return mQueue.size();
	}
	public int sSize() {
		return sQueue.size();
	}
	public int totalSize() {
		return totalQueue.size();
	}
	
	public String toString(){
		Iterator<Motion> it = mQueue.iterator();
		StringBuilder temp = new StringBuilder();
		Motion m = null;
		while (it.hasNext()) {
			m = it.next();
			temp.append("["+m.getStartTime()+"--"+m.getEndTime()+"]");
		}

		temp.append("\n");
		m=mQueue.peek();
		if(m!=null)
			temp.append("peek:"+"["+m.getStartTime()+"--"+m.getEndTime()+"]");
		
		return temp.toString();
	}

	public double getDutyRatioOfTime() {
		return dutyRatioOfTime;
	}
	public double getDutyRatioOfNumber() {
		return dutyRatioOfNumber;
	}
}
