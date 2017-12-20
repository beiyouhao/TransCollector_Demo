package com.lyy.sensordatacollection.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ict.iodetector.service.bean.DetectionProfile;
import com.ict.iodetector.service.bean.MoveOrStopQueue;
import com.ict.iodetector.service.utils.StatisticsUtil;
//import com.ict.iodetector.service.IODetectorService.AggregatedIODetector;

public class DutyRatioMode{
	/**
	 * isWalking retains the result of the last judgment
	 */
	public static boolean isWalking;
	public static boolean oneNewStepHappen;
	public boolean beforeWalking;
	private boolean enable;
	private SensorManager sensorManager;
	private Sensor proximitySensor;
	private Sensor accelerometerSensor;
	
	public static double outdoorConfiDR = 0.5;
//	for walking detection
	private double mThreshold;
	private double xAccelerometer;
	private double yAccelerometer;
	private double zAccelerometer;
	private double[] gravity;
	private double[] magnitudeArray;
	private int magnitudeCount;
//  for velocity evaluation
    public final static int LOW_SPEED = 1;
	public final static int NORMAL_SPEED = 2;
	public final static int HIGH_SPEED = 3;
	public static int speedState = 0;
	private double[] magnitudeForAcrossingZero;
	private int magnitudeForAcrossingZeroCount;
//	walking results or stopping results extend a two-seconds
	private int walkingExtend;
	private long walkingEnableTime;
	
//  record every count since this module was started	
	private Long currentTimeRecord,preTimeRecord;
	private MoveOrStopQueue moveOrStopQueue;
	private int stopCount;
	
	private long timerTime = System.currentTimeMillis();
	
//	for count since this module was started
	private Timer processTimer;

	private DetectionProfile[] listProfile = new DetectionProfile[3];
	private DetectionProfile indoor, semi, outdoor;
	private int kongzhi = 1,zhen = 0, s = 0, sum = 0, e = 0, num = 0, x = 0, y = 0, z = 0,
			w = 0, b = 0, c = 0, d = 0, newsum = 0, time_s2, biaoji = 0,
			newsum2 = 0, o = 0;
	private boolean zhenjia = false;
	private double A = 0.0, B = 0.0;
	private int oldNewSum = 0;
	private double lowX = 0, lowY = 0, lowZ = 0;
	private final double FILTERING_VALAUE = 0.84f;
	private long startingTime;
	
//	private ArrayList<Long> stopMoment = new ArrayList();
	/**
	 * @param sManager
	 * @param context
	 * @param enable
	 */
	public DutyRatioMode(SensorManager sensorManager0,Context context,boolean enable) {
		this.enable = enable;
		if (enable) {
			sensorManager = sensorManager0;
			proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			startingTime = System.currentTimeMillis();
			
			mThreshold = 0.5;
			currentTimeRecord = System.currentTimeMillis();
			preTimeRecord = System.currentTimeMillis();
			processTimer = new Timer();
			moveOrStopQueue = new MoveOrStopQueue();
			stopCount = 0;
			
			aLastTime = System.currentTimeMillis();
			pLastTime = System.currentTimeMillis();
			gravity = new double[3];
			magnitudeArray = new double[20];
			for(int i = 0;i < magnitudeArray.length;i ++) {
		    	magnitudeArray[i] = 1000;
		    }
			magnitudeCount = 0;
			magnitudeForAcrossingZero = new double[30];
			for(int j = 0;j < magnitudeForAcrossingZero.length;j ++) {
				magnitudeForAcrossingZero[j] = 1000;
			}
			magnitudeForAcrossingZeroCount = 0;
			walkingExtend = 12;
			walkingEnableTime = System.currentTimeMillis();
		}
		indoor = new DetectionProfile("indoor");
		semi = new DetectionProfile("semi-outdoor");
		outdoor = new DetectionProfile("outdoor");
		listProfile[0] = indoor;
		listProfile[1] = semi;
		listProfile[2] = outdoor;
	}
	private boolean isRun;
//	private Thread processCountThread = new Thread() {
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			super.run();
//			isRun = true;
//			while (isRun) {
//				try {
//					sleep(4000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				File logFile = new File(Environment
//						 .getExternalStorageDirectory().getAbsolutePath()
//						 + "/IODetector/" + "walkingRecord.txt");
//				try {
//					if (!logFile.exists())
//						logFile.createNewFile();
//					BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//					output.append("timer momemt" + Long.toString(System.currentTimeMillis() - timerTime) + "\n");
//					//output.append("\n");
//					output.flush();
//					output.close();
//					timerTime = System.currentTimeMillis();
//				} catch (IOException e) {
//				// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				if(isWalking) {
//					currentTimeRecord = new Long(System.currentTimeMillis());
//					if(beforeWalking){
//						if(currentTimeRecord.longValue() - preTimeRecord.longValue() > 5000) {
//
//							for(int i = 0;i < (int)(currentTimeRecord.longValue() - preTimeRecord.longValue())/4000 - 1;i ++) {
//								moveOrStopQueue.enMoveOrStopQueue(new Long(preTimeRecord.longValue() + 4000 * (i + 1)));
//							}
//						}
//					}
//					beforeWalking = isWalking;	
//				}
//				if(currentTimeRecord.longValue() != preTimeRecord.longValue()) {
//					moveOrStopQueue.enMoveOrStopQueue(currentTimeRecord);
//				}
////				if(currentTimeRecord.longValue() - preTimeRecord.longValue() > 5000) {
////					stopMoment.add(currentTimeRecord);
////				}
//				preTimeRecord = currentTimeRecord;
//			}
//		}
//	};
//	
	
	/**
	 * @author Li
	 * update the time record,enqueue when appropriate
	 */
	class processCountTimerTask extends TimerTask {

		@Override
		public void run() {
			
			// TODO Auto-generated method stub
//			System.out.println("rere: iswalking: " + isWalking);
//			System.out.println("rere: walkingExtend: " + walkingExtend);
//			System.out.println("rere: magnitude: " + StatisticsUtil.getAverage(magnitudeArray, 0.0f));
			
//			File logFile = new File(Environment
//					 .getExternalStorageDirectory().getAbsolutePath()
//					 + "/IODetector/" + "walkingRecord.txt");
//			try {
//				if (!logFile.exists())
//					logFile.createNewFile();
//				BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//				output.append("timer momemt" + Long.toString(System.currentTimeMillis() - timerTime) + "\n");
//				//output.append("\n");
//				output.flush();
//				output.close();
//				timerTime = System.currentTimeMillis();
//			} catch (IOException e) {
//			// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			if(isWalking) {
				currentTimeRecord = new Long(System.currentTimeMillis());
				if(beforeWalking){
					if(currentTimeRecord.longValue() - preTimeRecord.longValue() > 5000) {
					
						for(int i = 0;i < (int)(currentTimeRecord.longValue() - preTimeRecord.longValue())/4000;i ++) {
							
							moveOrStopQueue.enMoveOrStopQueue(new Long(preTimeRecord.longValue() + 4000 * (i + 1)));
							
//							File logFile1 = new File(Environment
//									 .getExternalStorageDirectory().getAbsolutePath()
//									 + "/IODetector/" + "recordback.txt");
//							try {
//								if (!logFile1.exists())
//									logFile1.createNewFile();
//								BufferedWriter output = new BufferedWriter(new FileWriter(logFile1, true));
//								output.append(Long.toString(preTimeRecord.longValue() + 4000 * (i + 1)) + "\n");
//								//output.append("\n");
//								output.flush();
//								output.close();
//							} catch (IOException e) {
//							// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
						
						}
					}
				}	
			}
			if(currentTimeRecord.longValue() != preTimeRecord.longValue()) {
				
				moveOrStopQueue.enMoveOrStopQueue(currentTimeRecord);
			}
//			if(currentTimeRecord.longValue() - preTimeRecord.longValue() > 5000) {
//				stopMoment.add(currentTimeRecord);
//			}
			beforeWalking = isWalking;	
			preTimeRecord = currentTimeRecord;
		}
	}
	/**
	 * @return frequency number of pauses
	 */
	public int getStopCount(){
//		if (0 == moveOrStopQueue.getSize()) {
//			return 0;
//		}
		return moveOrStopQueue.getStopCount();
	}
	/**
	 * @return listProfile 
	 */
	public DetectionProfile[] getProfile() {
		if (enable) {
			//stop count
//			long currentDetectTime = System.currentTimeMillis() - 24000;
//			for(int i = 0;i < stopMoment.size();i ++) {
//				if(stopMoment.get(i).longValue() < currentDetectTime) {
//					stopMoment.remove(i);
//				}
//			}
			if (System.currentTimeMillis() - startingTime < 2*60*1000) {
				
				outdoorConfiDR=0.5;
				indoor.setConfidence(0);
				semi.setConfidence(0);
				outdoor.setConfidence(0);
				return listProfile;
			}
			double timesCount = 0;
			long stopPeriod = 0;
		    moveOrStopQueue.calculateNumbers();
		    stopCount = moveOrStopQueue.getStopCount();
		    if(stopCount != 0) {
		    	timesCount = (double)moveOrStopQueue.getMoveCount() / stopCount;
		    	stopPeriod = moveOrStopQueue.getStopPeriod();
		    } else {
		    	timesCount = (double)moveOrStopQueue.getMoveCount();
		    }   
		    //90 counts in 360 seconds
		    if(timesCount > 90.0) {
		    	timesCount = 90.0;
		    }
		    //user stops 5 times in 240 seconds
		    if(stopCount > 5) {
		    	stopCount = 5;
		    }
//		    System.out.println("stopCount:" + stopCount);
//		    System.out.println("timesCount:" + timesCount);
		    double confidence = 0.5 + stopCount / 10.0 - timesCount / 180.0 + (double)(stopPeriod / 1000)/3600;
//		    System.out.println("confidence��" + confidence);
		    if (confidence < 0) {
		    	confidence = 0;
		    } else {
		    	if(confidence > 1) {
		    		confidence = 1;
		    	}
		    }
		   
		    outdoorConfiDR=(1-confidence);
			indoor.setConfidence(confidence);
			semi.setConfidence(stopCount);
//			semi.setConfidence(stopMoment.size());
			outdoor.setConfidence(1 - confidence);
		
		}
		return listProfile;
	}
	private SensorEventListener aListener;
	private SensorEventListener pListener;
	private long aLastTime,pLastTime;
	/**
	 * this mode starts
	 */
	public void start() {
		if(enable) {
//			processTimer.scheduleAtFixedRate(new processCountTimerTask(), 0, 4000);
			processTimer.schedule(new processCountTimerTask(), 0, 4000);
//			processCountThread.start();
			aListener = new SensorEventListener() {
				@Override
				public void onAccuracyChanged(Sensor arg0, int arg1) {
					// TODO Auto-generated method stub
				}
				@Override
				public void onSensorChanged(SensorEvent event) {
					// TODO Auto-generated method stub
					long aTime = System.currentTimeMillis() - aLastTime;
//					if (aTime <= 200) {
					if (aTime <= 166) {
						return;
					}
					aLastTime = System.currentTimeMillis();
					if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
						xAccelerometer = event.values[0];
						yAccelerometer = event.values[1];
						zAccelerometer = event.values[2];
						//measure the device's real accelerometer
						float kFilteringFactor = 0.6f;
						//Low-pass filter can be used to separate out gravity
						gravity[0] = (xAccelerometer * kFilteringFactor)
								+ (gravity[0] * (1.0f - kFilteringFactor));
						gravity[1] = (yAccelerometer * kFilteringFactor)
								+ (gravity[1] * (1.0f - kFilteringFactor));
						gravity[2] = (zAccelerometer * kFilteringFactor)
								+ (gravity[2] * (1.0f - kFilteringFactor));
						double magnitude = 0.0;
						//High-pass filter to exclude the interference of gravity
						magnitude = Math.sqrt(Math.pow((xAccelerometer - gravity[0]), 2)
								            + Math.pow((yAccelerometer - gravity[1]), 2)
								            + Math.pow((zAccelerometer - gravity[2]), 2));

						magnitudeForAcrossingZero[magnitudeForAcrossingZeroCount] =
								Math.sqrt(Math.pow((xAccelerometer), 2)
						                + Math.pow((yAccelerometer), 2)
						                + Math.pow((zAccelerometer), 2)) - 9.8;
						magnitudeForAcrossingZeroCount = (magnitudeForAcrossingZeroCount + 1) % magnitudeForAcrossingZero.length;
						int velocity = getVelocity(magnitudeForAcrossingZero);
						
						if (velocity > 20) {
							speedState = HIGH_SPEED;
						} else {
							if (velocity < 12) {
								speedState = LOW_SPEED;
							} else {
								speedState = NORMAL_SPEED;
							}
						}
//						File logFile = new File(Environment
//								 .getExternalStorageDirectory().getAbsolutePath()
//								 + "/IODetector/" + "velocity.txt");
//						try {
//							if (!logFile.exists())
//								logFile.createNewFile();
//							BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//							output.append(Double.toString(velocity)+"\n");
//							//output.append("\n");
//							output.flush();
//							output.close();
//						} catch (IOException e) {
//						// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						magnitude = Math.abs(magnitude);
						magnitudeArray[magnitudeCount] = magnitude;
						magnitudeCount = (magnitudeCount + 1) % magnitudeArray.length;
//						get the average of all data acquired
						magnitude = StatisticsUtil.getAverage(magnitudeArray, 1000);
						
						
						
						
//						double magnitudeVariance = StatisticsUtil.getVariation(magnitudeArray, 1000);
//						File logFile = new File(Environment
//								 .getExternalStorageDirectory().getAbsolutePath()
//								 + "/IODetector/" + "magnitudeVariance.txt");
//						try {
//							if (!logFile.exists())
//								logFile.createNewFile();
//							BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//							output.append(Double.toString(magnitudeVariance)+"\n");
//							//output.append("\n");
//							output.flush();
//							output.close();
//						} catch (IOException e) {
//						// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						
						
						
						
						//walking detection
//						System.out.println("magnitude: " + magnitude);
						
//						//record truthful data
//						File logFile = new File(Environment
//								 .getExternalStorageDirectory().getAbsolutePath()
//								 + "/IODetector/" + "magnitude.txt");
//						try {
//							if (!logFile.exists())
//								logFile.createNewFile();
//							BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//							output.append(Double.toString(magnitude)+"\n");
//							//output.append("\n");
//							output.flush();
//							output.close();
//						} catch (IOException e) {
//						// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
//						
//						File logFile = new File(Environment
//								 .getExternalStorageDirectory().getAbsolutePath()
//								 + "/IODetector/" + "pocket2.txt");
//						Date d1 = new Date();
//						SimpleDateFormat sdf1 = new SimpleDateFormat(
//								"yyyy-MM-dd HH:mm:ss");
//						try {
//							if (!logFile.exists())
//								logFile.createNewFile();
//							BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//							output.append("R" + "stopCount" + Integer.toString(stopCount) + "magnitude" + Double.toString(magnitude) + "time" + sdf1.format(d1) + "isWalking" + Boolean.toString(isWalking) + "\n");
//							//output.append("\n");
//							output.flush();
//							output.close();
//						} catch (IOException e) {
//						// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						
						if(Math.abs(magnitude) > mThreshold) {
							walkingEnableTime = System.currentTimeMillis();
							isWalking = true;
						} else {
							if(System.currentTimeMillis() - walkingEnableTime < 4000) {
								isWalking = true;
							} else {
								isWalking = false;
							}
						}
						
//						
//						
//						
//						if (kongzhi == 1) {
//							// System.out.println("���ٶȴ���������~~~~~~~~~~");
//							int flag = 0;
//							if (b == 0) {
//								x++;
//							} else if (c == 0) {
//								y++;
//							} else if (d == 0) {
//								z++;
//							} else {
//								w++;
//							}
//							String message = new String();
//							// Low-Pass Filter �õ�ͨ�˲����޳���Ƶ�ĸ���
//							lowX = lowX * FILTERING_VALAUE + xAccelerometer * (1.0 - FILTERING_VALAUE);
//							lowY = lowY * FILTERING_VALAUE + yAccelerometer * (1.0 - FILTERING_VALAUE);
//							lowZ = lowZ * FILTERING_VALAUE + zAccelerometer * (1.0 - FILTERING_VALAUE);
//							
//							A = Math.sqrt(lowX * lowX + lowY * lowY + lowZ * lowZ);
//							B = A - 9.8;
//							File logFile = new File(Environment
//									 .getExternalStorageDirectory().getAbsolutePath()
//									 + "/IODetector/" + "B.txt");
//							try {
//								if (!logFile.exists())
//									logFile.createNewFile();
//								BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
//								output.append(Double.toString(B)+"\n");
//								//output.append("\n");
//								output.flush();
//								output.close();
//							} catch (IOException e) {
//							// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							System.out.println("���ٶȴ���������~~~~~~~~~~" + B+"   "+s);
//							num++;
//							if (B > 4 || B < -3.0) {
//								s = 0;
//								num = 0;
//							} else {
//								if (s == 0 && flag == 0) {
//									flag = 1;
//									biaoji = 0;
//									num = 0;
//									if (B < 0.6) {
//										s = 0;
//									} else {
//										s = 1;
//									}
//								}
//								if (s == 1 && flag == 0) {
//									time_s2 = 0;
//									flag = 1;
//									if (B < 1.0 && B >= 0.6) {
//										s = 1;
//									}
//									if (B >= 1.0) {
//										s = 2;
//									}
//									if (B < 0.6) {
//										s = 4;
//									}
//								}
//								if (s == 4 && flag == 0) {
//									flag = 1;
//									if (biaoji >= 10) {
//										s = 0;
//									} else {
//										if (B >= 0.6) {
//											s = 1;
//										} else {
//											biaoji++;
//										}
//									}
//								}
//								if (s == 2 && flag == 0) {
//									flag = 1;
//									time_s2++;
//									if (time_s2 > 100) {
//										s = 0;
//									} else {
//										if (B >= 1.0) {
//											s = 2;
//										}
//										if (B < -0.4) {
//											s = 3;
//										}
//									}
//								}
//								if (s == 3 && flag == 0) {
//									flag = 1;
//									s=6;
//									System.out.println("@@@@@@@@@    ");
//									if (s == 6 && flag == 0) {
//										flag = 1;
//										s = 0;
//										System.out.println("######  ");
//										sum++;
//										
//										if (sum < 4) {
//											newsum++;
//											zhenjia = false;
//											if (b == 0) {
//												b++;
//												y = x;
//											} else if (c == 0) {
//												c++;
//												z = y;
//											} else if (d == 0) {
//												d++;
//												w = z;
//											} else {
//												e++;
//											}
//										} else {
//											if ((w - x) < 300 && (w - x) > 20) {
//												newsum++;
//												zhenjia = true;
//												b = 0;
//												x = w;
//											} else if ((x - y) < 300 && (x - y) > 20) {
//												newsum++;
//												zhenjia = true;
//												c = 0;
//												b = 1;
//												y = x;
//											} else if ((y - z) < 300 && (y - z) > 20) {
//												newsum++;
//												zhenjia = true;
//												d = 0;
//												c = 1;
//												z = y;
//											} else if ((z - w) < 300 && (z - w) > 20) {
//												newsum++;
//												zhenjia = true;
//												w = z;
//												d = 1;
//											} else {
//												newsum++;
//												zhenjia = false;
//												sum = 1;
//												b = 1;
//												c = 0;
//												d = 0;
//												e = 0;
//												x = 40;
//												y = 40;
//												z = 0;
//												w = 0;
//											}
//										}
//										if (zhenjia == false) {
//											o = 0;
//										} else {
//											if (o == 0) {
//												newsum2 = newsum2 + 3;
//											}
//											newsum2++;
//											o = 1;
//										}
//									}
//								}
//							}
//							if(sum != oldNewSum){
////								if(zhenjia == false){					
//									//newsum++;
//								System.out.println("new step!");
//									oneNewStepHappen = true;
////								}
//								oldNewSum = sum;
////								System.out.println("summmmmmmmm" + sum);
//							}
//							System.out.println("summmmmmmmm" + sum);
//						}
					}
				}
			};
//			sensorManager.registerListener(aListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
//			sensorManager.registerListener(aListener, accelerometerSensor, 1000000);
			sensorManager.registerListener(aListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			pListener = new SensorEventListener() {
				@Override
				public void onAccuracyChanged(Sensor arg0, int arg1) {
					// TODO Auto-generated method stub	
				}
				@Override
				public void onSensorChanged(SensorEvent event) {
					// TODO Auto-generated method stub	
					long pTime = System.currentTimeMillis() - pLastTime;
					if (pTime <= 2000) {
						return;
					}
					//android debug
//					android.os.Debug.waitForDebugger();
					pLastTime = System.currentTimeMillis();
					if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
						if (event.values[0] == 0) {
							//device maybe in a trouser pocket
							mThreshold = 0.4;
						} else {
							mThreshold = 0.8;
						}
					} 
				}
			};
			sensorManager.registerListener(pListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);	
//			sensorManager.registerListener(pListener, proximitySensor, 2000000);
		}
	}
	/**
	 * @param arrays
	 * @return the user's speed level
	 */
	private int getVelocity(double[] arrays) {
		int num = 0;
		boolean lastContrast = false,currentContrast = false;
//		double average = StatisticsUtil.getAverage(arrays, 1000);
		for (int i = 0;i < arrays.length;i ++) {
			currentContrast = (arrays[i] > 0);
			if (lastContrast ^ currentContrast) {
				num ++;
			}
			lastContrast = currentContrast;
		}
		return num;
	}
	public void start1() {
		if(enable) {
//			sensorManager.unregisterListener(aListener,accelerometerSensor);
			sensorManager.registerListener(aListener,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
//			sensorManager.registerListener(aListener, accelerometerSensor, 1000000);
			synchronized(processTimer) {
				processTimer.notify();
			}
		}
		
	}
	public void stop1() {
		if(enable) {
			sensorManager.unregisterListener(aListener, accelerometerSensor);
			synchronized(processTimer) {
				try {
					processTimer.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	/**
	 * unregister the listener
	 */
	public void stop() {
		if(enable) {
			processTimer.cancel();
			sensorManager.unregisterListener(pListener, proximitySensor);
			sensorManager.unregisterListener(aListener, accelerometerSensor);
			kongzhi = 0;
			isRun = false;
		}
	}
	
	/**
	 * @return indoors confidence of dutyRatioMode 
	 */
	public double getIndoorsConfidenceOfDutyRatioMode() {
		return 1 - outdoorConfiDR;
	}
}
