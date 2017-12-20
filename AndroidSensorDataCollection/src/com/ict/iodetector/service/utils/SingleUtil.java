package com.ict.iodetector.service.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * 各种单例对象
 * 
 * @author Administrator
 *
 */
public class SingleUtil {
	/**
	 * 获取日期格式的单例对象
	 * 
	 * @return
	 */
	private static SimpleDateFormat sdf = null;

	public static synchronized SimpleDateFormat createSDF() {
		if (sdf == null) {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return sdf;
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		if (sdf == null) {
			sdf = createSDF();
		}
		return sdf;
	}

	/**
	 * 获取保留小数点单例对象
	 */
	private static DecimalFormat df;

	public static synchronized DecimalFormat createDf() {
		if (df == null)
			df = new DecimalFormat("0.000");
		return df;
	}

	public static DecimalFormat getDecimalFormat() {
		if (df == null) {
			df = createDf();
		}
		return df;
	}
}
