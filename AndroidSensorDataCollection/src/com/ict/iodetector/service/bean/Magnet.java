package com.ict.iodetector.service.bean;

public class Magnet {
	private double magnetModulous;
	private long recordingTime;
	public Magnet(double modulous, long time) {
		this.magnetModulous = modulous;
		this.recordingTime = time;
	}
	public double getMagnetModulous() {
		return magnetModulous;
	}
	public void setMagnetModulous(double magnetModulous) {
		this.magnetModulous = magnetModulous;
	}
	public long getRecordingTime() {
		return recordingTime;
	}
	public void setRecordingTime(long recordingTime) {
		this.recordingTime = recordingTime;
	}

}
