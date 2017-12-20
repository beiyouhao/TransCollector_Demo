package com.ict.iodetector.service.utils;

public class TimeCompareUtil {
	/**
	 * 当前时间大于后面的时间
	 * 
	 * @param currentHour
	 * @param currentMinute
	 * @param hour
	 * @param minute
	 * @return
	 */
	public static boolean isLaterThan(int currentHour, int currentMinute, int hour, int minute) {

		if (currentHour > hour)
			return true;
		else if (currentHour < hour)
			return false;
		else {
			if (currentMinute > minute)
				return true;
			else
				return false;
		}
	}

	/**
	 * 获取时间的小时
	 * 
	 * @param time
	 * @return
	 */
	public static int getTimeHour(String time) {
		String[] timeStr = time.split(":");
		return Integer.parseInt(timeStr[0]);
	}

	/**
	 * 获取时间的分钟
	 * 
	 * @param time
	 * @return
	 */
	public static int getTimeMin(String time) {
		String[] timeStr = time.split(":");
		return Integer.parseInt(timeStr[1]);
	}
}
