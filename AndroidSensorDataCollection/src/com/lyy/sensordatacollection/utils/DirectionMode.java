/*
 * File:DirectionMode.java
 * 
 */
package com.lyy.sensordatacollection.utils;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ict.iodetector.service.bean.DetectionProfile;
import com.ict.iodetector.service.bean.LinkedQueue;
import com.ict.iodetector.service.bean.Motion;
import com.ict.iodetector.service.bean.MotionQueue;
import com.ict.iodetector.service.utils.StatisticsUtil;
import com.lyy.sensordatacollection.MainActivity;

public class DirectionMode {

	private boolean enable = false;

	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor gyroscopeSensor;

	public static double outdoorConfiD = 0.5;

	/**
	 * cycle of pedestrian' walking
	 */
	private double walkingCycle;// default:3 seconds
	/**
	 * sampling rate
	 */
	private double samplingRate;
	/**
	 * data count for one cycle
	 */
	private int dataCountForAStepCycle;
	/**
	 * accelerometer represent three axes
	 */
	private double xAccelerometer;
	private double yAccelerometer;
	private double zAccelerometer;

	private double[] xAc;
	private double[] yAc;
	private double[] zAc;
	private int accelerationCount;
	private double[] xAverage;
	private double[] yAverage;
	private double[] zAverage;
	private int averageLength;
	/**
	 * direction cosine represents three axes be used to calculate the angle
	 * directly
	 */
	private double xDirectionCosine;
	private double yDirectionCosine;
	private double zDirectionCosine;

	// gyroscope
	/**
	 * angular speed represent three axes
	 */
	private double xPitchAngularRate;
	private double yRollAngularRate;
	private double zYawAngularRate;
	private double[] xPit;
	private double[] yRol;
	private double[] zYaw;
	private int angularVelocityCount;

	// result of angle calculate
	private double turnWithVolatility;
	private double[] angles;
	private int angleCount;
	/**
	 * threshold of change be used to judge whether the angle is a turn or not
	 */
	private double turnThreshold = 30.0;

	// for record movement
	private LinkedQueue<Long> turnQueue;
	private MotionQueue motionQueue;
	private boolean motionExisted;
	private boolean stopExisted;
	private boolean moveState;
	private boolean stopState;
	private Motion currentMoveMotion;
	private Motion currentStopMotion;
	private double noTurnCount;

	private DetectionProfile[] listProfile = new DetectionProfile[3];
	private DetectionProfile indoor, semi, outdoor;

	/**
	 * @param sManager
	 * @param context
	 * @param enable
	 */
	public DirectionMode(SensorManager sManager, Context context, boolean enable) {
		this.enable = enable;
		if (enable) {
			sensorManager = sManager;
			accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

			walkingCycle = 1.2;// two steps: right and left
			samplingRate = 0.2;
			dataCountForAStepCycle = (int) (walkingCycle / samplingRate);

			aLastTime = System.currentTimeMillis();
			xAccelerometer = 0;
			yAccelerometer = 0;
			zAccelerometer = 0;
			// 9 second.one step:0.4s~1s,about 9 step,3 turn length
			accelerationCount = 0;
			int accDataLength = 5 * dataCountForAStepCycle;
			xAc = new double[accDataLength];
			yAc = new double[accDataLength];
			zAc = new double[accDataLength];
			for (int i = 0; i < accDataLength; i++) {
				xAc[i] = 1000;
				yAc[i] = 1000;
				zAc[i] = 1000;
			}

			averageLength = 5;
			xAverage = new double[averageLength];
			yAverage = new double[averageLength];
			zAverage = new double[averageLength];

			xDirectionCosine = 0;
			yDirectionCosine = 0;
			zDirectionCosine = 0;

			gLastTime = System.currentTimeMillis();
			xPitchAngularRate = 0;
			yRollAngularRate = 0;
			zYawAngularRate = 0;
			// assume 3 seconds per turn:60~4
			angularVelocityCount = 0;
			int gyroDataLength = 4 * dataCountForAStepCycle;
			xPit = new double[gyroDataLength];
			yRol = new double[gyroDataLength];
			zYaw = new double[gyroDataLength];
			for (int i = 0; i < gyroDataLength; i++) {
				xPit[i] = 1000;
				yRol[i] = 1000;
				zYaw[i] = 1000;
			}

			turnWithVolatility = 0.0;
			angles = new double[9];
			for (int i = 0; i < 9; i++) {
				angles[i] = 1000;
			}
			angleCount = 0;

			turnQueue = new LinkedQueue<Long>(8);
			motionQueue = new MotionQueue();
			motionExisted = false;
			stopExisted = false;
			moveState = true;
			stopState = false;
			noTurnCount = 0;

			preTurnRecord = System.currentTimeMillis();
		}
		indoor = new DetectionProfile("indoor");
		semi = new DetectionProfile("semi-outdoor");
		outdoor = new DetectionProfile("outdoor");
		listProfile[0] = indoor;
		listProfile[1] = semi;
		listProfile[2] = outdoor;
	}

	/**
	 * register the listener
	 */
	private long lastCalculatingTime;// for calculating rate:1Hz
	private long lastTurnTime;// for turning rate
	private long aLastTime, gLastTime;// for sampling rate
	private SensorEventListener aListener;
	private SensorEventListener gListener;
	/**
	 * if the user changes the holding position of phone, angle changes caused
	 * by this action does not count
	 */
	private long positionChange;

	public void start() {
		if (enable) {
			positionChange = System.currentTimeMillis();
			lastCalculatingTime = System.currentTimeMillis();
			lastTurnTime = System.currentTimeMillis();
			aListener = new AccListener();
			// sensorManager.registerListener(aListener, accelerometerSensor,
			// SensorManager.SENSOR_DELAY_UI);
			// sensorManager.registerListener(aListener, accelerometerSensor,
			// 200000);
			sensorManager.registerListener(aListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			// int i = 0;
			// while (i < 1000) {
			// i ++;
			// }
			gListener = new AngListener();
			// sensorManager.registerListener(gListener, gyroscopeSensor,
			// SensorManager.SENSOR_DELAY_UI);
			// sensorManager.registerListener(gListener, gyroscopeSensor,
			// 200000);
			sensorManager.registerListener(gListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * unregister the listener
	 */
	public void stop() {
		if (enable) {
			sensorManager.unregisterListener(aListener, accelerometerSensor);
			sensorManager.unregisterListener(gListener, gyroscopeSensor);
		}
	}

	public void startIfBeginWalking() {
		if (enable) {
			sensorManager.registerListener(aListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(gListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * unregister the listener when the user stops
	 */
	public void stopIfBecomingStandstill() {
		if (enable) {
			sensorManager.unregisterListener(aListener, accelerometerSensor);
			sensorManager.unregisterListener(gListener, gyroscopeSensor);
		}
	}

	/**
	 * @return listProfile
	 */
	public DetectionProfile[] getProfile() {
		if (enable) {
			// StringBuilder temp = new StringBuilder();
			// for(int i=0;i<turnQueue.size();i++) {
			// temp.append("["+turnQueue.get(i)+"]");
			// }
			// temp.append("\n");
			// System.out.println("turnQueue:"+temp.toString());

			// when the total motion time is more than 1 minute, update the
			// motion queue and turn queue
			// System.out.println("motionQueue.getTotalMotionTime()" +
			// motionQueue.getTotalMotionTime());
			if (motionQueue.getTotalMotionTime() >= 120 * 1000) {
				double conf = 0.5;
//				conf = 0.5 + turnQueue.size() / 10.0 - noTurnCount / 1200.0;
				conf = 0.5 + turnQueue.size() / 16.0 - noTurnCount / 1200.0;
				if (conf < 0)
					conf = 0;
				outdoorConfiD = (1 - conf);
				indoor.setConfidence(conf);
				semi.setConfidence(turnQueue.size());
				outdoor.setConfidence(1 - conf);
				// SimpleDateFormat sdf=new SimpleDateFormat("HH:MM:SS");
				// get the start time of one minute motion
				long timeFlag = motionQueue.getStartSystemTimeOfmMotion();
				if (turnQueue.peek() != null && turnQueue.peek() < timeFlag) {
					// if the time of the oldest turn is earlier than the start
					// time of one minute motion, delete the turning
					turnQueue.deQueue();
				}
			}
		}
		return listProfile;
	}

	public double sum(double theQueue[], int start, int end) {
		double sum = 0;
		for (int i = start; i < end; i++) {
			sum += theQueue[i];
		}
		return sum;
	}

	/**
	 * @param angleCount
	 * @param angleThreshold
	 * @return the result of detection
	 */
	private long preTurnRecord;

	private boolean turnDetection(double currentCalculation, double angleThreshold) {
		Arrays.sort(angles);
		double[] volatility = new double[angles.length / 3];
		for (int i = 0; i < volatility.length; i++) {
			volatility[i] = angles[i + volatility.length];
		}
		double rawTurning = Math.abs(currentCalculation) - StatisticsUtil.getAverage(volatility, 1000);
		// record truthful data
		// File logFile = new File(Environment
		// .getExternalStorageDirectory().getAbsolutePath()
		// + "/IODetector/" + "truthturning.txt");
		// try {
		// if (!logFile.exists())
		// logFile.createNewFile();
		// BufferedWriter output = new BufferedWriter(new FileWriter(logFile,
		// true));
		// output.append(Double.toString(rawTurning) +
		// Long.toString(System.currentTimeMillis() - preTurnRecord) +"\n");
		// preTurnRecord = System.currentTimeMillis();
		// //output.append("\n");
		// output.flush();
		// output.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("TruthTurning: " + rawTurning);
		if (Math.abs(rawTurning) >= angleThreshold) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return number of turns
	 */
	public int numOfTurn() {
		return turnQueue.size();
	}

	public double getDutyRatio() {
		motionQueue.setDutyRatio();
		return motionQueue.getDutyRatioOfTime();
	}

	public double getNoTurnCount() {
		return noTurnCount;
	}

	/**
	 * record every movement
	 */
	private void recordMovement() {
		long currentTime = System.currentTimeMillis();
		if (DutyRatioMode.isWalking) {
			if (noTurnCount <= 600) {
				noTurnCount++;
			}
			if (motionExisted) {// if walking and motion exists,
								// update the end time of this
								// motion
				currentMoveMotion.setEndTime(currentTime);
				if (currentMoveMotion.getPeriod() >= 60 * 1000) {
					motionQueue.enMQueue(currentMoveMotion);
					motionQueue.enTotalQueue(currentMoveMotion);
					motionExisted = false;
				}

			} else {// if walking and motion doesn't exist, create a
					// new motion and add to the queue
				currentMoveMotion = new Motion(currentTime, currentTime + 1, moveState);
				// motionQueue.enQueue(currentMoveMotion);
				motionExisted = true;
				// motionSaved = false;
			}
			if (stopExisted) {
				if (currentTime - currentStopMotion.getStartTime() >= 2000) {
					currentStopMotion.setEndTime(currentTime);
					motionQueue.enSQueue(currentStopMotion);
					motionQueue.enTotalQueue(currentStopMotion);
					// System.out.println("StopStart:" +
					// currentStopMotion.getStartTime());
					// System.out.println("StopEnd:" +
					// currentStopMotion.getEndTime());
					stopExisted = false;
				} else {
					motionExisted = false;
				}
			}
		} else {
			if (stopExisted) {
				currentStopMotion.setEndTime(currentTime);
			} else {
				currentStopMotion = new Motion(currentTime, currentTime + 1, stopState);
				stopExisted = true;
			}
			if (motionExisted) {// if not walking and motion exists,
								// update the end time of this
								// motion
				if (currentTime - currentMoveMotion.getStartTime() >= 2000) {
					currentMoveMotion.setEndTime(currentTime);
					motionQueue.enMQueue(currentMoveMotion);
					motionQueue.enTotalQueue(currentMoveMotion);
					// System.out.println("MoveStart:" +
					// currentMoveMotion.getStartTime());
					// System.out.println("MoveEnd:" +
					// currentMoveMotion.getEndTime());
					motionExisted = false;
				} else {
					stopExisted = false;
				}
			}
		}
	}

	/**
	 * @param array
	 * @return medium
	 */
	private double getMediumValue(double[] array) {
		// TODO Auto-generated method stub
		double median;
		Arrays.sort(array);
		median = array[array.length / 2];
		return median;
	}

	public double getTurnThreshold() {
		return turnThreshold;
	}

	public void setTurnThreshold(double turnThreshold) {
		this.turnThreshold = turnThreshold;
	}

	/**
	 * @return indoors confidence of directionMode
	 */
	public double getIndoorsConfidenceOfDirectionMode() {
		return 1 - outdoorConfiD;
	}

	public class AccListener implements SensorEventListener {
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			// System.out.println("2.detect the sampling rate");
			// set the sampling rate manually
			long aTime = System.currentTimeMillis() - aLastTime;
			if (aTime <= samplingRate * 1000) {
				return;
			}
			aLastTime = System.currentTimeMillis();

			// monitor the movement every 0.2 second
			recordMovement();

			// if the user stops, this mode stops producing data
			if (!DutyRatioMode.isWalking) {
				for (int i = 0; i < xAc.length; i++) {
					xAc[i] = 1000;
					yAc[i] = 1000;
					zAc[i] = 1000;
				}
				accelerationCount = 0;
				return;
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// System.out.println("4.it is accelerometer");
				// TODO Auto-generated method stub
				xAccelerometer = event.values[0];
				yAccelerometer = event.values[1];
				zAccelerometer = event.values[2];
				// System.out.println("positionChange" + (xAccelerometer -
				// StatisticsUtil.getAverage(xAc, 1000)));
				// System.out.println("positionChange" + (yAccelerometer -
				// StatisticsUtil.getAverage(yAc, 1000)));
				// System.out.println("positionChange" + (zAccelerometer -
				// StatisticsUtil.getAverage(zAc, 1000)));
				boolean a = Math.abs(xAccelerometer - StatisticsUtil.getAverage(xAc, 1000)) > 8.5;
				boolean b = Math.abs(yAccelerometer - StatisticsUtil.getAverage(yAc, 1000)) > 8.5;
				boolean c = Math.abs(zAccelerometer - StatisticsUtil.getAverage(zAc, 1000)) > 8.5;
				// File logFile = new File(Environment
				// .getExternalStorageDirectory().getAbsolutePath()
				// + "/IODetector/" + "positionChange.txt");
				// try {
				// if (!logFile.exists())
				// logFile.createNewFile();
				// BufferedWriter output = new BufferedWriter(new
				// FileWriter(logFile, true));
				// output.append("a:"+Boolean.toString(a)+"b:"+Boolean.toString(b)+"c:"+Boolean.toString(c)+"\n");
				// //output.append("\n");
				// output.flush();
				// output.close();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				if ((a && b) || (b && c) || (a && c)) {
					positionChange = System.currentTimeMillis();
					for (int i = 0; i < xAc.length; i++) {
						xAc[i] = 1000;
						yAc[i] = 1000;
						zAc[i] = 1000;
					}
					accelerationCount = 0;
				}
				xAc[accelerationCount] = xAccelerometer;
				yAc[accelerationCount] = yAccelerometer;
				zAc[accelerationCount] = zAccelerometer;
				accelerationCount = (accelerationCount + 1) % xAc.length;
				// if((Math.abs(System.currentTimeMillis() -
				// IODetectorService.ScreenOnTime) < 6000) &&
				if ((Math.abs(System.currentTimeMillis() - MainActivity.ScreenOffTime) < 3000)) {
					return;
				}
				if (xAc[xAc.length - 1] == 1000) {
					return;
				}
				// if(Math.abs(System.currentTimeMillis() - positionChange) <
				// 3000) {
				// return;
				// }
				for (int i = 0; i < averageLength; i++) {
					xAverage[i] = sum(xAc, i * xAc.length / 5, (i + 1) * xAc.length / 5);
					yAverage[i] = sum(yAc, i * yAc.length / 5, (i + 1) * yAc.length / 5);
					zAverage[i] = sum(zAc, i * zAc.length / 5, (i + 1) * zAc.length / 5);
				}
				// accelerometer data in a straight line
				double xStableAverage = getMediumValue(xAverage);
				double yStableAverage = getMediumValue(yAverage);
				double zStableAverage = getMediumValue(zAverage);
				double modulus = Math
						.sqrt(Math.pow(xStableAverage, 2) + Math.pow(yStableAverage, 2) + Math.pow(zStableAverage, 2));
				// System.out.println("xStableAverage: " + xStableAverage);
				// System.out.println("yStableAverage: " + yStableAverage);
				// System.out.println("zStableAverage: " + zStableAverage);
				if (modulus != 0) {
					xDirectionCosine = xStableAverage / modulus;
					yDirectionCosine = yStableAverage / modulus;
					zDirectionCosine = zStableAverage / modulus;
				} else {
					xDirectionCosine = 0;
					yDirectionCosine = 0;
					zDirectionCosine = 0;
				}
			} // end of accelerometer data processing
		}// end of onSensorChanged
	}

	public class AngListener implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			// set the sampling rate manually
			long gTime = System.currentTimeMillis() - gLastTime;
			if (gTime <= samplingRate * 1000) {
				// System.out.println("delay 1");
				return;
			}
			gLastTime = System.currentTimeMillis();

			// if the user stops, this mode stops producing data
			if (!DutyRatioMode.isWalking) {
				for (int i = 0; i < xPit.length; i++) {
					xPit[i] = 1000;
					yRol[i] = 1000;
					zYaw[i] = 1000;
				}
				angularVelocityCount = 0;
				return;
			}

			// whole length :4 * dataCountForAStepCycle
			double[] xtemplet = new double[3 * dataCountForAStepCycle];
			double[] xCurrentSampling = new double[dataCountForAStepCycle];
			int xhead = 0;
			double[] ytemplet = new double[3 * dataCountForAStepCycle];
			double[] yCurrentSampling = new double[dataCountForAStepCycle];
			int yhead = 0;
			double[] ztemplet = new double[3 * dataCountForAStepCycle];
			double[] zCurrentSampling = new double[dataCountForAStepCycle];
			int zhead = 0;
			int leftdata = 0, rightdata = 0;
			// System.out.println("3.detect which sensor is");
			// System.out.println("getType: " + event.sensor.getType());

			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				// System.out.println("5.it is gyroscope");
				double xAngle = 0, yAngle = 0, zAngle = 0;
				xPitchAngularRate = event.values[0];
				yRollAngularRate = event.values[1];
				zYawAngularRate = event.values[2];
				// do not calculate during positionChanging
				if (Math.abs(System.currentTimeMillis() - positionChange) < 3000) {
					return;
				}
				if (xAc[xAc.length - 1] == 1000) {
					return;
				}
				// System.out.println("1.detect the screen state" +
				// IODetectorService.ScreenOffTime);
				// if((Math.abs(System.currentTimeMillis() -
				// IODetectorService.ScreenOnTime) < 3000) &&
				// if(Math.abs(System.currentTimeMillis() -
				// IODetectorService.ScreenOffTime) < 3000){
				// System.out.println("delay 3");
				// return;
				// }
				xPit[angularVelocityCount] = xPitchAngularRate;
				yRol[angularVelocityCount] = yRollAngularRate;
				zYaw[angularVelocityCount] = zYawAngularRate;
				// do calculating every 1 seconds
				long calculatingPeriod = System.currentTimeMillis() - lastCalculatingTime;
				if (calculatingPeriod <= 1000) {
					// System.out.println("delay 4");
					angularVelocityCount = (angularVelocityCount + 1) % (xPit.length);
					return;
				}
				lastCalculatingTime = System.currentTimeMillis();
				if (xPit[xPit.length - 1] != 1000) {// indicates initial
													// gyroscope array
													// filled
					if (angularVelocityCount < dataCountForAStepCycle) {
						xtemplet = Arrays.copyOfRange(xPit, angularVelocityCount,
								angularVelocityCount + 3 * dataCountForAStepCycle);
						ytemplet = Arrays.copyOfRange(yRol, angularVelocityCount,
								angularVelocityCount + 3 * dataCountForAStepCycle);
						ztemplet = Arrays.copyOfRange(zYaw, angularVelocityCount,
								angularVelocityCount + 3 * dataCountForAStepCycle);
						for (int head = angularVelocityCount + 3 * dataCountForAStepCycle; head < xPit.length; head++) {
							xCurrentSampling[xhead] = xPit[head];
							yCurrentSampling[yhead] = yRol[head];
							zCurrentSampling[zhead] = zYaw[head];
							xhead++;
							yhead++;
							zhead++;
						}
						leftdata = 0;
						for (int tail = xhead; tail < dataCountForAStepCycle; tail++) {
							xCurrentSampling[tail] = xPit[leftdata];
							yCurrentSampling[tail] = yRol[leftdata];
							zCurrentSampling[tail] = zYaw[leftdata];
							leftdata++;
						}
						xhead = 0;
						yhead = 0;
						zhead = 0;
					} else {
						xCurrentSampling = Arrays.copyOfRange(xPit, angularVelocityCount - (dataCountForAStepCycle - 1),
								angularVelocityCount + 1);
						yCurrentSampling = Arrays.copyOfRange(yRol, angularVelocityCount - (dataCountForAStepCycle - 1),
								angularVelocityCount + 1);
						zCurrentSampling = Arrays.copyOfRange(zYaw, angularVelocityCount - (dataCountForAStepCycle - 1),
								angularVelocityCount + 1);
						for (int head = angularVelocityCount + 1; head < xPit.length; head++) {
							xtemplet[xhead] = xPit[head];
							ytemplet[yhead] = yRol[head];
							ztemplet[zhead] = zYaw[head];
							zhead++;
							xhead++;
							yhead++;
						}
						rightdata = 0;
						for (int tail = xhead; tail < 3 * dataCountForAStepCycle; tail++) {
							xtemplet[tail] = xPit[rightdata];
							ytemplet[tail] = yRol[rightdata];
							ztemplet[tail] = zYaw[rightdata];
							rightdata++;
						}
						xhead = 0;
						yhead = 0;
						zhead = 0;
					}
					// contain natural change of wave,need other
					// optimization
					xAngle = StatisticsUtil.dynamicTimeWarping(xtemplet, xCurrentSampling);
					yAngle = StatisticsUtil.dynamicTimeWarping(ytemplet, yCurrentSampling);
					zAngle = StatisticsUtil.dynamicTimeWarping(ztemplet, zCurrentSampling);
					xAngle *= samplingRate;
					yAngle *= samplingRate;
					zAngle *= samplingRate;
				}
				angularVelocityCount = (angularVelocityCount + 1) % (xPit.length);
				// calculate an incoming turn
				turnWithVolatility = Math
						.toDegrees(xAngle * xDirectionCosine + yAngle * yDirectionCosine + zAngle * zDirectionCosine);

				// System.out.println("noTruthturnWithVolatility:" +
				// turnWithVolatility);
				if (turnWithVolatility != Double.NaN) {
					if (angles[angles.length - 1] == 1000) {
						// turn counting negative
					} else {
						if (turnDetection(turnWithVolatility, turnThreshold)) {
							// least timeSpan between two turn action is
							// 2s
							if (DutyRatioMode.isWalking && (System.currentTimeMillis() - lastTurnTime) > 3000) {
								// there is a new turn, add the time of
								// this
								// turn to the queue
								turnQueue.enQueue(System.currentTimeMillis());

								for (int i = 0; i < turnQueue.size(); i++) {
									boolean turnQueueChanged = false;
									if (Math.abs(
											turnQueue.get(i).longValue() - MainActivity.ScreenOffTime) < 3000) {
										turnQueue.remove(i);
										turnQueueChanged = true;
									}
									if (!turnQueueChanged) {
										noTurnCount /= turnQueue.size();
									}
								}

							}
							lastTurnTime = System.currentTimeMillis();
						}
					}
					angles[angleCount] = Math.abs(turnWithVolatility);
					angleCount = (angleCount + 1) % angles.length;
				} // end of turn processing
			} // end of gyroscope data processing
		}// end of onSensorChanged

	}
}