package com.ict.iodetector.service.utils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import com.ict.iodetector.service.bean.DetectionProfile;

/**
 * 功能相关工具类
 * 
 * @author Administrator
 *
 */
public class FuncUtil {
	private static StringBuilder sb;
	private static int count;
	public static final int MAX_COUNT = 30;

	/**
	 * 检测室内外的置信度是否接近,当前判断相差在-0.2~0.2是模糊的
	 * 
	 * @param indoor
	 * @param outdoor
	 * @return
	 */
	public static boolean isAmbigious(double indoor, double outdoor) {
		double diff = indoor - outdoor;
		if (diff <= 0.2 && diff >= -0.2) {
			return true;
		}
		return false;
	}

	/**
	 * 获取光模块的置信度占百分比
	 * 
	 * @param lightMode
	 * @return
	 */
	

	/**
	 * 获取气压模块的置信度占百分比
	 * 
	 * @param lightMode
	 * @return
	 */
	public static double getPressureModeWeight(double pressureWeiAddition) {
		return 0.15 + pressureWeiAddition;
	}

	/**
	 * 记录检测结果并存入文件
	 * 
	 * @param lightProfile
	 * @param magnetProfile
	 * @param gpgsvProfile
	 * @param pressureProfile
	 * @param directionProfile
	 * @param dutyRatioProfile
	 * @param lightWei
	 * @param magnetWei
	 * @param gpgsvWei
	 * @param pressureWei
	 * @param directionWei
	 * @param dutyRatioWei
	 * @param indoor
	 * @param outdoor
	 * @param detectionStatus
	 */
	public static void recordServiceFile(DetectionProfile[] lightProfile, DetectionProfile[] magnetProfile,
			DetectionProfile[] gpgsvProfile, DetectionProfile[] pressureProfile, DetectionProfile[] directionProfile,
			DetectionProfile[] dutyRatioProfile, double lightWei, double magnetWei, double gpgsvWei, double pressureWei,
			double directionWei, double dutyRatioWei, double indoor, double outdoor, int detectionStatus) {
		DecimalFormat df = SingleUtil.getDecimalFormat();
		if (sb == null) {
			sb = new StringBuilder();
			count = 0;
		}
		// 光
		sb.append("L: " + DetectionProfile.getDetectionResult(lightProfile) + "\t"
				+ df.format(lightProfile[1].getConfidence()) + "\t");
		// 磁
		sb.append("M: " + DetectionProfile.getDetectionResult(magnetProfile) + "\t"
				+ df.format(magnetProfile[1].getConfidence()) + "\t");
		// 气压
		sb.append("P: " + DetectionProfile.getDetectionResult(pressureProfile) + "\t"
				+ df.format(pressureProfile[1].getConfidence()) + "\t");
		// 方向
		sb.append("D: " + DetectionProfile.getDetectionResult(directionProfile) + "\t"
				+ df.format(directionProfile[1].getConfidence()) + "\t");
		// 走停
		sb.append("R: " + DetectionProfile.getDetectionResult(dutyRatioProfile) + "\t"
				+ df.format(dutyRatioProfile[1].getConfidence()) + "\t");
		// gpgsv
		sb.append("G: " + DetectionProfile.getDetectionResult(gpgsvProfile) + "\t"
				+ df.format(gpgsvProfile[1].getConfidence()) + "\t");
		// 合计
		sb.append("A: " + detectionStatus + "\t" + SingleUtil.getSimpleDateFormat().format(new Date()) + "\n");
		count++;
		if (count >= MAX_COUNT) {
			FileStorageUtil.writeAutoFile("service", "result", sb.toString());
			sb.delete(0, sb.length());
			count = 0;
		}
	}
}
