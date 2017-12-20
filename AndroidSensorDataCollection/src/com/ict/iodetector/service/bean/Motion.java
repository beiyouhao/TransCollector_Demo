package com.ict.iodetector.service.bean;

public class Motion {
	private long startTime;
	private long endTime;
	private boolean moveOrStop;

	public Motion(long startTime, long endTime,boolean moveOrStop) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.moveOrStop = moveOrStop;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getPeriod() {
		return endTime - startTime;
	}

	public boolean isMoveOrStop() {
		return moveOrStop;
	}

	public void setMoveOrStop(boolean moveOrStop) {
		this.moveOrStop = moveOrStop;
	}
}
