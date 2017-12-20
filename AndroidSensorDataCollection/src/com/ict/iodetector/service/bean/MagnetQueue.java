package com.ict.iodetector.service.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ict.iodetector.service.utils.StatisticsUtil;

public class MagnetQueue {
	public static final ReadWriteLock lock = new ReentrantReadWriteLock(false);
	private Queue<Magnet> magQueue;
	public MagnetQueue() {
		magQueue  = new LinkedList<Magnet>();
	}
	public void enMagQueue(Magnet newMember) {
		lock.writeLock().lock();
		magQueue.offer(newMember);
		lock.writeLock().unlock();
	}
//	public void retainLength(long limitedTime) {
//		lock.writeLock().lock();
//		if (!magQueue.isEmpty()) {
//			while (magQueue.peek().getRecordingTime() < limitedTime) {
//				magQueue.remove();
//				if (magQueue.isEmpty()) {	
//					break;
//				}
//			}
//		}
//		lock.writeLock().unlock();
//	}
	public int getSize() {
		lock.readLock().lock();
		int size = magQueue.size();
		lock.readLock().unlock();
		return size;
		
	}
	public void deleteRecent15Data() {
		lock.writeLock().lock();
		LinkedList<Magnet> tmp = (LinkedList<Magnet>)magQueue;
		
		int i = 7;
		if (!tmp.isEmpty()) {
			while (i > 0) {
				i--;
				tmp.pollLast();
				if (tmp.isEmpty()) {	
					break;
				}
			}
		}
		magQueue = tmp;
		lock.writeLock().unlock();
	}
	public void retainLength() {
		lock.writeLock().lock();
		if (!magQueue.isEmpty()) {
//			while (magQueue.size() > 180) {
//			while (magQueue.size() > 90) {
			while (magQueue.size() > 120) {
				magQueue.remove();
				if (magQueue.isEmpty()) {	
					break;
				}
			}
		}
		lock.writeLock().unlock();
	}
	public int getQueueSize() {
		lock.readLock().lock();
		int size = magQueue.size();
		lock.readLock().unlock();
		return size;
		
	}
	public double getMagnetMedian() {
		lock.readLock().lock();
		if ( magQueue.isEmpty()) {
			lock.readLock().unlock();
			return -1;
		}
		double median = 0;
		Double[] objectArray = new Double[magQueue.size()];
		Magnet[] elements = new Magnet[magQueue.size()];
		magQueue.toArray(elements);
		for(int p = 0;p < elements.length;p ++) {
			objectArray[p] = new Double(elements[p].getMagnetModulous());
		}
		double[] fundamentalType = new double[objectArray.length];
		for(int i = 0;i < fundamentalType.length;i ++) {
			fundamentalType[i] = objectArray[i].doubleValue();
		}
		Arrays.sort(fundamentalType);
		median = fundamentalType[fundamentalType.length/2];
		lock.readLock().unlock();
		return median;
	}
//	public double getMagnetAverage() {
//		if (magQueue.isEmpty()) {
//			return -1;
//		}
//		double average = 0;
//		double sumModulous = 0;
//		Iterator<Magnet> it = magQueue.iterator();
//		while (it.hasNext()) {
//			Magnet mag = it.next();
//			sumModulous += mag.getMagnetModulous();
//		}
//		average = sumModulous / magQueue.size();
//		return average;
//	}
	public int getNumOfZeroCrossing() {
		lock.readLock().lock();
		if(magQueue.isEmpty()) {
			lock.readLock().unlock();
			return -1;
		}
		//logic1:using median
		//中位数
		double median = getMagnetMedian();
//		//logic2:using average
//		double average = getMagnetAverage();
		int num = 0;
		Iterator<Magnet> it1 = magQueue.iterator();
		double lastMagnetModulousBias = 0;
		Magnet m = null;
		while (it1.hasNext()) {
			m = it1.next();
			//logic1
			double nowMagnetModulousBias = m.getMagnetModulous() - median;
//			//logic2
//			double nowMagnetModulousBias = m.getMagnetModulous() - average;
			//eliminate the effects of fluctuations in the data
			if (!(Math.abs(nowMagnetModulousBias) < 1)) {
				if (nowMagnetModulousBias * lastMagnetModulousBias < 0) {
					num ++;
				}
				lastMagnetModulousBias = nowMagnetModulousBias;
			}
		}
		lock.readLock().unlock();
		return num;
	}
	public double getCoefficientOfVariation() {
		lock.readLock().lock();
		if(magQueue.isEmpty()) {
			lock.readLock().unlock();
			return -1;
		}
//		if()
//		ArrayList<Double> modulous = new ArrayList<Double>();
//		Iterator<Magnet> it2 = magQueue.iterator();
//		Magnet m = null;
//		while (it2.hasNext()) {
//			m = it2.next();
//			modulous.add(new Double(m.getMagnetModulous()));
//		}
		double[] fundamentalType = new double[magQueue.size()];
		Magnet[] elements = new Magnet[magQueue.size()];
		magQueue.toArray(elements);
		for(int p = 0;p < elements.length;p ++) {
			fundamentalType[p] = elements[p].getMagnetModulous();
		}
//		Arrays.sort(fundamentalType);
//		Arrays.copyOfRange(original, start, end);
		double sd = Math.sqrt(StatisticsUtil.getVariation(fundamentalType, 1000));
		double mn = StatisticsUtil.getAverage(fundamentalType, 1000);
		lock.readLock().unlock();
		return sd/mn;
	}
	public int getMinutiaNumOfZeroCrossing() {
		lock.readLock().lock();
		if(magQueue.size() < 3) {
			lock.readLock().unlock();
			return -1;
		}
		
		
		double[] fundamentalType = new double[magQueue.size()];
		Magnet[] elements = new Magnet[magQueue.size()];
		magQueue.toArray(elements);
		for(int p = 0;p < elements.length;p ++) {
			fundamentalType[p] = elements[p].getMagnetModulous();
		}
//		ArrayList<Double> extremum = new ArrayList<Double>();
//		for (int i = 1;i < fundamentalType.length - 2;i ++) {
//			double previous = fundamentalType[i] - fundamentalType[i - 1];
//			double latter = fundamentalType[i] - fundamentalType[i + 1];
//			if(previous > 0 && latter > 0) { 
//				extremum.add(new Double(fundamentalType[i]));
//			}
//		}
		int num = 0;
//		for (int i = 1;i < fundamentalType.length - 1;i ++) {
//			if(fundamentalType[i] - fundamentalType[i - 1] > 3) {
//				num ++;
//			}
//		}
		for (int i = fundamentalType.length - 1;i > 0;i --) {
			double current = fundamentalType[i];
			while ((fundamentalType[i - 1] < fundamentalType[i]) && (i - 1) > 0) {
				i --;
			}
			if (current - fundamentalType[i] > 4) {
				num ++;
			}
			
		}
		
//		int num = 0;
//		Iterator<Magnet> it4 = magQueue.iterator();
//		double a = it4.next().getMagnetModulous();
//		double b = it4.next().getMagnetModulous();
//		double last = b - a,current = 0;
//		double lastMagnetModulousBias = 0;
//		Magnet m = null;
//		while (it4.hasNext()) {
//			m = it4.next();
//			current = m.getMagnetModulous() - b;
//			if(last < 0 && current < 0) {
//				num ++;
//			}
//			b = m.getMagnetModulous();
//			last = current;
//		}
		lock.readLock().unlock();
		return num;
		
	}
}
