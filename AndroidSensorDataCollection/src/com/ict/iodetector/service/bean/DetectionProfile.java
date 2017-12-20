/*
 * This class is for the profile of each sensor. 
 * It will save the environment(indoor,semi,outdoor) and the confidence value;
 */

package com.ict.iodetector.service.bean;

public class DetectionProfile {
	public final static int INDOOR = 0;
	public final static int OUTDOOR = 2;
	public final static int TRANSITION_ZONE = 1;
	public final static int NO_INPUT = -1;
	private String environment;
	private double confidence;

	public static String getDetectionResultAsString(int result) {
		switch (result) {
		case INDOOR:
			return "indoor";
		case OUTDOOR:
			return "outdoor";
		case TRANSITION_ZONE:
			return "transition";
		case NO_INPUT:
			return "no_input";
		}

		return "no_input";
	}

	/**
	 * @param indoor
	 * @param outdoor
	 * @param semi
	 * @return detection result
	 */
	public static int getDetectionResult(double indoor, double outdoor, double semi) {
		if (indoor > outdoor && indoor >= semi) {// Indoor
			return INDOOR;
		} else if (outdoor > indoor && outdoor >= semi) {
			return OUTDOOR;
		} else {// Outdoor
			return NO_INPUT;
		}
	}

	/**
	 * 获取检测结果
	 * 
	 * @param profile
	 * @return
	 */
	public static int getDetectionResult(DetectionProfile[] profile) {
		return getDetectionResult(profile[0].getConfidence(), profile[2].getConfidence(), 0);
	}

	/**
	 * @param indoor
	 * @param outdoor
	 * @param semi
	 * @return detection result as string
	 */
	public static String getDetectionResultAsString(double indoor, double outdoor, double semi) {
		semi = 0;
		return getDetectionResultAsString(getDetectionResult(indoor, outdoor, semi));
	}

	/**
	 * @param indoor
	 * @param outdoor
	 * @param semi
	 * @return detection result as string
	 */
	public static String getDetectionResultAsString(DetectionProfile[] profile) {
		double indoor = profile[0].getConfidence();
		double transi = 0;
		double outdoor = profile[2].getConfidence();
		return getDetectionResultAsString(getDetectionResult(indoor, outdoor, transi));
	}

	/**
	 * @param env
	 */
	public DetectionProfile(String env) {
		this.environment = env;
		this.confidence = 0;
	}

	/**
	 * @return environment
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * @return confidence
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * @param environment
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * @param confidence
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

}
