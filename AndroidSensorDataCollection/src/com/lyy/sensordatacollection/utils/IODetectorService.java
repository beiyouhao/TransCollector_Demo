//package com.ict.iodetector.service;
//
//import java.io.File;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import com.ict.iodetector.service.aidl.IOAggregatedResultListener;
//import com.ict.iodetector.service.aidl.IODetailDataListener;
//import com.ict.iodetector.service.aidl.IOSeparatedResultListener;
//import com.ict.iodetector.service.aidl.IOService;
//import com.ict.iodetector.service.bean.DetectionProfile;
//import com.ict.iodetector.service.conf.Configuration;
//import com.ict.iodetector.service.conf.Mode;
//import com.ict.iodetector.service.conf.Threshold;
//import com.ict.iodetector.service.mode.CellTowerMode;
//import com.ict.iodetector.service.mode.CinrMode;
//import com.ict.iodetector.service.mode.DirectionMode;
//import com.ict.iodetector.service.mode.DutyRatioMode;
//import com.ict.iodetector.service.mode.GPGSVMode;
//import com.ict.iodetector.service.mode.HMMMode;
//import com.ict.iodetector.service.mode.LightMode;
//import com.ict.iodetector.service.mode.MagnetMode;
//import com.ict.iodetector.service.mode.PressureMode;
//import com.ict.iodetector.service.mode.SoundMode;
//import com.ict.iodetector.service.mode.TemperatureMode;
//import com.ict.iodetector.service.mode.UVSenserMode;
//import com.ict.iodetector.service.utils.FileStorageUtil;
//import com.ict.iodetector.service.utils.FuncUtil;
//import com.ict.iodetector.service.utils.SingleUtil;
//import com.ict.iodetector.service.utils.StatisticsUtil;
//import com.ict.iodetector.service.utils.TimeCompareUtil;
//import com.ict.iodetector.service.weather.SearchWeather;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.hardware.SensorManager;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.Message;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.os.RemoteCallbackList;
//import android.os.RemoteException;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//
///**
// * this service detect the indoor/outdoor scene
// * 
// */
//public class IODetectorService extends Service {
//	public static final String TAG = "IODetectorService";
//	private final static int WINDOW_SIZE = 3;
//	// the confidence of outdoor
//	public static double outDoorConfidence;
//
//	// the moment of screen triggering (from close to open)/(from open to close)
//	public static long ScreenOnTime = 0, ScreenOffTime = 0;
//
//	private int[] detectionStatusWindow = new int[WINDOW_SIZE];
//	private int timer = 0;
//
//	private SensorManager sensorManager;
//	private TelephonyManager telephonyManager;
//
//	private LightMode lightMode;
//	private MagnetMode magnetMode;
//	private CellTowerMode cellTowerMode;
//	private SoundMode soundMode;
//	private TemperatureMode temperatureMode;
//	// humidity mode doesn't work for iodetector
//	// private HumidityMode humidityMode;
//	private DirectionMode directionMode;
//	private GPGSVMode gpgsvMode;
//	private PressureMode pressureMode;
//	private DutyRatioMode dutyRatioMode;
//	private CinrMode cinrMode;
//	private UVSenserMode uvSenserMode;
//	// HMM
//	private HMMMode hmmMode;
//
//	private boolean aggregationFinish;
//	private Timer uiTimer;
//
//	// these 2 timer used for debug and display
//	private Timer collectDataTimer;
//	private Timer reportDetailDataTimer;
//
//	public SearchWeather sw;
//
//	// last known status
//	public static int detectionStatus;
//	private int maxWindowDetectionStatus;
//	private int previousDetectionStatus;
//	public static int detectionStatusHMM;
//
//	private static final String PHONE_MODEL = android.os.Build.MODEL;
//	private Calendar calendar = Calendar.getInstance();
//	// proportion of each mode
//	private double LIGHT_WEI = 0;
//	private double LIGHTWEI_ADD = 0;
//	private double MAGNET_WEI = 0;
//	private double CELL_WEI = 0;
//	private double TEMP_WEI = 0;
//	private double SOUND_WEI = 0;
//	private double GPGSV_WEI = 0;
//	private double DIRECTION_WEI = 0;
//	private double PRESSURE_WEI = 0;
//	private double PRESSUREWEI_ADD = 0;
//	private double DUTYRATIO_WEI = 0;
//	private double CINR_WEI = 0;
//	private boolean someSensorClosed = true;
//
//	private boolean isReportAggregated;
//	private boolean isReportSeparated;
//
//	private BroadcastReceiver screenOnorOffReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context arg0, Intent arg1) {
//			// TODO Auto-generated method stub
//			if (Intent.ACTION_SCREEN_ON.equals(arg1.getAction())) {// when the
//																	// screen is
//																	// on
//				if (LightMode.hasRegister == 3) {
//					// synchronized(dutyRatioMode) {
//					// dutyRatioMode.notify();
//					// }
//					// android.os.Debug.waitForDebugger();
//					lightMode.start1();
//					// dutyRatioMode.start1();
//				}
//				ScreenOnTime = System.currentTimeMillis();
//			}
//			if (Intent.ACTION_SCREEN_OFF.equals(arg1.getAction())) {
//				if (LightMode.hasRegister == 2) {
//					// synchronized(dutyRatioMode) {
//					// try {
//					// dutyRatioMode.wait();
//					// } catch (InterruptedException e) {
//					// // TODO Auto-generated catch block
//					// e.printStackTrace();
//					// }
//					// }
//					lightMode.stop1();
//					// dutyRatioMode.stop1();
//				}
//				ScreenOffTime = System.currentTimeMillis();
//			}
//		}
//
//	};
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onBind(android.content.Intent)
//	 */
//	@Override
//	public IBinder onBind(Intent intent) {
//		return mBinder;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onCreate()
//	 */
//	@Override
//	public void onCreate() {
//
//		super.onCreate();
//		Log.i(TAG, "onCreate");
//		isReportAggregated = false;
//		isReportSeparated = false;
//		// 注册屏幕开启关闭的广播
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Intent.ACTION_SCREEN_ON);
//		filter.addAction(Intent.ACTION_SCREEN_OFF);
//		registerReceiver(screenOnorOffReceiver, filter);
//
//		detectionStatus = DetectionProfile.NO_INPUT;
//		previousDetectionStatus = DetectionProfile.NO_INPUT;
//		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//		for (int i = 0; i < WINDOW_SIZE; i++) {
//			detectionStatusWindow[i] = DetectionProfile.NO_INPUT;
//		}
//		// if the config file exist, set up configuration, otherwise create the
//		// default mode
//		File f = new File(Configuration.CONFIG_FITH);
//		if (f.exists()) {
//			Log.i(TAG, "flie exists,do setUpConfig");
//			setupConfig();
//		} else {
//			Log.i(TAG, "flie not,do setUpConfig");
//			cellTowerMode = new CellTowerMode(telephonyManager, this, false);
//			cellTowerMode.start();
//			dutyRatioMode = new DutyRatioMode(sensorManager, this, true);
//			dutyRatioMode.start();
//			gpgsvMode = new GPGSVMode(this, true);
//			lightMode = new LightMode(sensorManager, this, true);
//			lightMode.start();
//			soundMode = new SoundMode(this, false);
//			soundMode.start();
//			temperatureMode = new TemperatureMode(sensorManager, this, false);
//			temperatureMode.start();
//			directionMode = new DirectionMode(sensorManager, this, true);
//			directionMode.start();
//			magnetMode = new MagnetMode(sensorManager, this, true);
//			magnetMode.start();
//			pressureMode = new PressureMode(sensorManager, this, true, magnetMode, dutyRatioMode);
//			pressureMode.start();
//			cinrMode = new CinrMode(telephonyManager, this, false);
//			cinrMode.start();
//
//			uvSenserMode = new UVSenserMode(this, false);
//			uvSenserMode.start();
//
//			hmmMode = new HMMMode(lightMode, true);
//			someSensorClosed = false;
//
//			mHandler.obtainMessage(MSG_START_DETECT).sendToTarget();
//		}
//	}
//
//	/**
//	 * reading the configuration file will consume more time, so do it in a
//	 * newthread
//	 */
//	private void setupConfig() {
//		new Thread() {
//			@Override
//			public void run() {
//				super.run();
//				Looper.prepare();
//				Configuration conf = new Configuration();
//				ArrayList<Mode> modes = conf.getModes();
//				for (int i = 0; i < modes.size(); i++) {
//					setupModes(modes.get(i));
//					Log.i(TAG, "mode:" + modes.get(i).getName() + ",enable:" + modes.get(i).isEnable());
//				}
//				mHandler.obtainMessage(MSG_START_DETECT).sendToTarget();
//				Looper.loop();
//			}
//		}.start();
//	}
//
//	/**
//	 * @param m
//	 *            mode set up the mode and every mode's threshold
//	 */
//	private void setupModes(Mode m) {
//		Threshold t = m.getThresholds().containsKey(PHONE_MODEL) ? m.getThresholds().get(PHONE_MODEL)
//				: m.getThresholds().get(Threshold.DEFAULT_MODE);
//
//		if (m.getName().equals("CellTower")) {
//			cellTowerMode = new CellTowerMode(telephonyManager, this, m.isEnable());
//			if (m.isEnable() && t != null && !t.getValue().equals("-1"))
//				cellTowerMode.setTHRESHOLD(Integer.parseInt(t.getValue()));
//			cellTowerMode.start();
//		} else if (m.getName().equals("GPGSV")) {
//			gpgsvMode = new GPGSVMode(this, m.isEnable());
//			if (m.isEnable() && t != null && !t.getValue().equals("-1"))
//				gpgsvMode.setTHRESHOLD(Integer.parseInt(t.getValue()));
//		} else if (m.getName().equals("DutyRatio")) {
//			dutyRatioMode = new DutyRatioMode(sensorManager, this, m.isEnable());
//			dutyRatioMode.start();
//			// dutyRatioMode = new DutyRatioMode(this, true,directionMode);
//		} else if (m.getName().equals("Light")) {
//			lightMode = new LightMode(sensorManager, this, m.isEnable());
//			if (m.isEnable() && t != null) {
//				if (!t.getHigh().equals("-1"))
//					lightMode.setHIGH_THRESHOLD(Integer.parseInt(t.getHigh()));
//				if (!t.getLow().equals("-1"))
//					lightMode.setLOW_THRESHOLD(Integer.parseInt(t.getLow()));
//				if (!t.getVariance().equals("-1"))
//					lightMode.setLightVariance(Integer.parseInt(t.getVariance()));
//			}
//			lightMode.start();
//		} else if (m.getName().equals("Sound")) {
//			soundMode = new SoundMode(this, m.isEnable());
//			if (m.isEnable() && t != null && !t.getValue().equals("-1")) {
//				soundMode.setTHRESHOLD(Integer.parseInt(t.getValue()));
//			}
//			soundMode.start();
//		} else if (m.getName().equals("Temperature")) {
//			// temperature mode doesn't have threshold, so don't set that in the
//			// config file
//			temperatureMode = new TemperatureMode(sensorManager, this, m.isEnable());
//			temperatureMode.start();
//		} else if (m.getName().equals("Direction")) {
//			directionMode = new DirectionMode(sensorManager, this, m.isEnable());
//			if (m.isEnable() && t != null) {
//				if (!t.getTurnthreshold().equals("-1"))
//					directionMode.setTurnThreshold(Double.parseDouble(t.getTurnthreshold()));
//			}
//			directionMode.start();
//		} else if (m.getName().equals("UV") && m.isEnable()) {
//			if (PHONE_MODEL.equals("SM-N9100")) {
//				uvSenserMode = new UVSenserMode(this, m.isEnable());
//				uvSenserMode.start();
//
//			}
//		}
//
//		else if (m.getName().equals("Pressure")) {
//			pressureMode = new PressureMode(sensorManager, this, m.isEnable(), magnetMode, dutyRatioMode);
//			pressureMode.start();
//		} else if (m.getName().equals("Cinr")) {
//			cinrMode = new CinrMode(telephonyManager, this, m.isEnable());
//			cinrMode.start();
//		} else if (m.getName().equals("Magnet")) {
//			magnetMode = new MagnetMode(sensorManager, this, m.isEnable());
//			if (m.isEnable() && t != null && !t.getValue().equals("-1")) {
//				magnetMode.setTHRESHOLD(Integer.parseInt(t.getValue()));
//			}
//			magnetMode.start();
//		}
//		someSensorClosed = false;
//		// humidityMode = new HumidityMode(sensorManager, this);
//	}
//
//	public static final int MSG_SET_WEATHER = 0;
//	public static final int MSG_START_DETECT = 1;
//
//	// set the weather information into the mode which need them
//	private Handler mHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			switch (msg.what) {
//			case MSG_SET_WEATHER:
//				// System.out.println("sun rise:" +
//				// sw.getWeather().getSun_rise());
//				// System.out.println("sun set:" +
//				// sw.getWeather().getSun_set());
//				// lightMode.setSunRiseTime(sw.getWeather().getSun_rise());
//				// System.out.println("sunonline: " +
//				// sw.getWeather().getSun_rise() + "length: " +
//				// sw.getWeather().getSun_rise().length());
//				lightMode.setSunRiseTime(sw.getWeather().getSun_rise());
//				lightMode.setSunSetTime(sw.getWeather().getSun_set());
//				temperatureMode.setTEMPHIGH(Float.parseFloat(sw.getWeather().getTempHigh()));
//				temperatureMode.setTEMPLOW(Float.parseFloat(sw.getWeather().getTempLow()));
//				temperatureMode.setTEMP(Float.parseFloat(sw.getWeather().getTemp()));
//				temperatureMode.setNetworkconnected(true);
//				break;
//			case MSG_START_DETECT:
//				Log.i(TAG, "do MSG_START_DETECT,first searchWeather");
//				sw = new SearchWeather(IODetectorService.this, mHandler);
//				sw.start();
//				aggregationFinish = true;
//
//				// the detection modes take 2s to collect the data for the first
//				// time
//				Log.i(TAG, "do first detect");
//				Timer firstDetectionTimer = new Timer();
//				firstDetectionTimer.schedule(new TimerTask() {
//
//					@Override
//					public void run() {
//						/*
//						 * update the profile for the first time, save the
//						 * profile of last detection
//						 */
//						if (cellTowerMode.isEnable())
//							cellTowerMode.initialProfile();
//						// Check the detection for the first time
//						new AggregatedIODetector().execute("");
//					}
//
//				}, 2000);
//
//				/*
//				 * This timer handle the drawing of the graph and start the
//				 * aggregated calculation for the detection Interval 1 seconds.
//				 */
//				Log.i(TAG, "do detect every 1 second when walking");
//				uiTimer = new Timer();
//				uiTimer.scheduleAtFixedRate(new TimerTask() {
//					private int detectionTimer = 0;
//					private int unWalked = 0;
//
//					@Override
//					public void run() {
//						/*
//						 * System.out.println("user walked:" +
//						 * DutyRatioMode.isWalking);
//						 */
//						if (DutyRatioMode.isWalking) {// Check if the user
//														// is walking
//							Log.i(TAG, "is walking,open directionMode,magnetMode,pressureMode");
//							if (someSensorClosed && detectionTimer > 3) {
//								directionMode.startIfBeginWalking();
//								magnetMode.startIfBeginWalking();
//								pressureMode.startIfBeginWalking();
//								someSensorClosed = false;
//							}
//							unWalked = 0;
//						} else {
//							Log.i(TAG, "is not walking,close directionMode,magnetMode,pressureMode");
//							if (!someSensorClosed && detectionTimer > 3) {
//								magnetMode.stopIfBecomingStandstill();
//								directionMode.stopIfBecomingStandstill();
//								pressureMode.stopIfBecomingStandstill();
//								someSensorClosed = true;
//							}
//							unWalked++;
//							if (unWalked == Integer.MAX_VALUE) {
//								unWalked = 10;
//							}
//						}
//
//						// System.out.println("unWalked:" + unWalked);
//						detectionTimer++;
//						/*
//						 * Check if the user has walked for at least 3 second,
//						 * and the previous calculation has been finish
//						 */
//						if (detectionTimer > 3 && unWalked < 4) {
//							if (aggregationFinish) {
//								aggregationFinish = false;
//								detectionTimer = 0;
//								Log.i(TAG, "is walking,begin AggregatedIODetector");
//								new AggregatedIODetector().execute("");
//							}
//						}
//					}
//				}, 0, 1000);
//				break;
//			}
//		}
//	};
//
//	/**
//	 * @author SuS This class is to handle the Aggregated detection
//	 */
//	private class AggregatedIODetector extends AsyncTask<String, Void, String> {
//
//		private DetectionProfile lightProfile[];
//		private DetectionProfile cellProfile[];
//		private DetectionProfile magnetProfile[];
//		private DetectionProfile temperatureProfile[];
//		// private DetectionProfile humidityProfile[];
//		private DetectionProfile soundProfile[];
//		private DetectionProfile gpgsvProfile[];
//		private DetectionProfile pressureProfile[];
//		private DetectionProfile directionProfile[];
//		private DetectionProfile dutyRatioProfile[];
//		private DetectionProfile cinrProfile[];
//
//		private double indoor, transition, outdoor;
//
//		private long lightTime;
//		private long magnetTime;
//		private long cellTime;
//		private long temperatureTime;
//		// private long humidityTime;
//		private long soundTime;
//		private long gpgsvTime;
//		private long pressureTime;
//		private long directionTime;
//		private long dutyRatioTime;
//		private long cinrTime;
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
//		 */
//		@Override
//		protected String doInBackground(String... param) {
//			// 获取光模块的检测结果
//			long t = System.currentTimeMillis();
//			lightProfile = lightMode.getProfile();
//			lightTime = System.currentTimeMillis() - t;
//			// 获取地磁模块的检测结果
//			t = System.currentTimeMillis();
//			magnetProfile = magnetMode.getProfile();
//			magnetTime = System.currentTimeMillis() - t;
//			// 获取基站模块的检测结果
//			t = System.currentTimeMillis();
//			cellProfile = cellTowerMode.getProfile();
//			cellTime = System.currentTimeMillis() - t;
//			//// 获取温度模块的检测结果
//			// t = System.currentTimeMillis();
//			// temperatureProfile = temperatureMode.getProfile();
//			// temperatureTime = System.currentTimeMillis() - t;
//			//// 获取湿度模块的检测结果
//			// // t = System.currentTimeMillis();
//			// // humidityProfile = humidityMode.getProfile();
//			// // humidityTime = System.currentTimeMillis() - t;
//			//// 获取声音模块的检测结果
//			// t = System.currentTimeMillis();
//			// soundProfile = soundMode.getProfile();
//			// soundTime = System.currentTimeMillis() - t;
//			// 获取gps模块的检测结果
//			t = System.currentTimeMillis();
//			gpgsvProfile = gpgsvMode.getProfile();
//			gpgsvTime = System.currentTimeMillis() - t;
//			// 获取气压模块的检测结果
//			t = System.currentTimeMillis();
//			pressureProfile = pressureMode.getProfile();
//			pressureTime = System.currentTimeMillis() - t;
//			// 获取行走方向模块的检测结果
//			t = System.currentTimeMillis();
//			directionProfile = directionMode.getProfile();
//			directionTime = System.currentTimeMillis() - t;
//			// 获取走停占空比模块的检测结果
//			t = System.currentTimeMillis();
//			dutyRatioProfile = dutyRatioMode.getProfile();
//			dutyRatioTime = System.currentTimeMillis() - t;
//			// 获取载波干扰噪声比模块的检测结果
//			// t = System.currentTimeMillis();
//			// cinrProfile = cinrMode.getProfile();
//			// cinrTime = System.currentTimeMillis() - t;
//
//			// Aggregate the result
//			LIGHT_WEI = FuncUtil.getLightModeWeight(lightMode, LIGHTWEI_ADD);
//			MAGNET_WEI = 0.2;
//			// CELL_WEI = 0.05;
//			// TEMP_WEI = 0.1;
//			// SOUND_WEI = 0.05;
//			// GPGSV_WEI = 0.0;
//			DIRECTION_WEI = 0.3;
//			PRESSURE_WEI = FuncUtil.getPressureModeWeight(PRESSUREWEI_ADD);// 0.15
//			DUTYRATIO_WEI = 0.3;
//			// CINR_WEI = 0;
//			if (lightProfile[2].getConfidence() == 1) {
//				// LIGHT_WEI += 0.1;
//				LIGHTWEI_ADD += 0.1;
//				if (LIGHTWEI_ADD > 0.1)
//					LIGHTWEI_ADD = 0.1;
//			} else {
//				LIGHTWEI_ADD -= 0.1;
//				if (LIGHTWEI_ADD < 0)
//					LIGHTWEI_ADD = 0;
//			}
//
//			if (pressureProfile[0].getConfidence() == 1) {
//				// PRESSURE_WEI += 0.2;
//				PRESSUREWEI_ADD += 0.1;
//				if (PRESSUREWEI_ADD > 0.3)
//					PRESSUREWEI_ADD = 0.3;
//			} else {
//				PRESSUREWEI_ADD -= 0.1;
//				if (PRESSUREWEI_ADD < 0)
//					PRESSUREWEI_ADD = 0;
//			}
//			
//			indoor = lightProfile[0].getConfidence() * LIGHT_WEI + magnetProfile[0].getConfidence() * MAGNET_WEI
//					+ directionProfile[0].getConfidence() * DIRECTION_WEI
//					+ pressureProfile[0].getConfidence() * PRESSURE_WEI
//					+ dutyRatioProfile[0].getConfidence() * DUTYRATIO_WEI;
//			Log.i(TAG, "indoor confidence:" + indoor);
//
//			transition = 0;
//
//			outdoor = lightProfile[2].getConfidence() * LIGHT_WEI + magnetProfile[2].getConfidence() * MAGNET_WEI
//					+ directionProfile[2].getConfidence() * DIRECTION_WEI
//					+ pressureProfile[2].getConfidence() * PRESSURE_WEI
//					+ dutyRatioProfile[2].getConfidence() * DUTYRATIO_WEI;
//			Log.i(TAG, "outdoor confidence:" + outdoor);
//
//			outDoorConfidence = outdoor / (outdoor + indoor);
//
//			// 根据置信度判断是否打开gps模块
//			if (!gpgsvMode.isEnable() && FuncUtil.isAmbigious(indoor, outdoor)) {
//				gpgsvMode.start();
//				GPGSV_WEI = 0.8;
//			}
//			// 如果gps可用，开启30s后且其他模块检测结果与gps相同，关闭gps模块
//			if (gpgsvMode.isEnable()) {
//				if (!gpgsvMode.isGpsIsOpenning() && DetectionProfile.getDetectionResult(gpgsvProfile) == StatisticsUtil
//						.getMaxCountDetection(detectionStatusWindow)) {
//					gpgsvMode.stop();
//					GPGSV_WEI = 0;
//				} else {
//					indoor += gpgsvProfile[0].getConfidence() * GPGSV_WEI;
//					outdoor += gpgsvProfile[2].getConfidence() * GPGSV_WEI;
//				}
//			}
//			/*
//			 * if (lightProfile[2].getConfidence() == 1) { indoor = 0;
//			 * transition = 0; outdoor = 1; }
//			 * 
//			 * if (pressureProfile[0].getConfidence() == 1) { indoor = 1;
//			 * transition = 0; outdoor = 0; }
//			 * 
//			 * if (temperatureMode.isIndoor()) { indoor = 1; transition = 0;
//			 * outdoor = 0; }
//			 * 
//			 * if (gpgsvProfile[0].getConfidence() == 1) { indoor = 1;
//			 * transition = 0; outdoor = 0; } temperatureMode.setIndoor(false);
//			 * 
//			 * cellTowerMode.updateProfile();// save the profile of last
//			 * detection
//			 */
//			return null;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) After
//		 * calculation has been done, post the result to the user
//		 */
//		@Override
//		protected void onPostExecute(String result2) {
//			// save the confidence data
//			// result may be no_input,which leads to
//			detectionStatus = DetectionProfile.getDetectionResult(indoor, outdoor, transition);
//			detectionStatusWindow[timer % WINDOW_SIZE] = detectionStatus;
//			timer++;
//			maxWindowDetectionStatus = StatisticsUtil.getMaxCountDetection(detectionStatusWindow);
//			if (maxWindowDetectionStatus != DetectionProfile.NO_INPUT)
//				detectionStatus = maxWindowDetectionStatus;
//
//			// the status will be logged when the detection status changed
//			if (detectionStatus != previousDetectionStatus) {
//				// aggregatedResultChangedCallBack(detectionStatus);
//				FileStorageUtil.writeManualFile("", DetectionProfile.getDetectionResultAsString(detectionStatus) + "\t"
//						+ SingleUtil.getSimpleDateFormat().format(new Date()) + "\r\n");
//
//				previousDetectionStatus = detectionStatus;
//			}
//			// 使用HMM过滤
//			detectionStatusHMM = hmmMode.getProfile(detectionStatus);
//			// 记录检测室内室外的置信度
//			// FuncUtil.recordServiceFile(lightProfile, magnetProfile,
//			// gpgsvProfile, pressureProfile, directionProfile,
//			// dutyRatioProfile, LIGHT_WEI, MAGNET_WEI, GPGSV_WEI, PRESSURE_WEI,
//			// DIRECTION_WEI, DUTYRATIO_WEI,
//			// indoor, outdoor, detectionStatus);
//
//			if (isReportSeparated) {
//				DecimalFormat df1 = SingleUtil.getDecimalFormat();
//
//				HashMap<String, String> lightRow = new HashMap<String, String>();
//				lightRow.put("title", "L");
//				lightRow.put("indoor", df1.format(lightProfile[0].getConfidence()));
//				lightRow.put("transition", df1.format(lightProfile[1].getConfidence()));
//				lightRow.put("extraParam", df1.format(lightMode.getLightTemperature()));
//				lightRow.put("outdoor", df1.format(lightProfile[2].getConfidence()));
//				lightRow.put("time", String.format("%.2f", LIGHT_WEI));
//				lightRow.put("result", DetectionProfile.getDetectionResultAsString(lightProfile));
//
//				HashMap<String, String> magnetRow = new HashMap<String, String>();
//				magnetRow.put("title", "M");
//				magnetRow.put("indoor", df1.format(magnetProfile[0].getConfidence()));
//				magnetRow.put("transition", df1.format(magnetProfile[1].getConfidence()));
//				magnetRow.put("outdoor", df1.format(magnetProfile[2].getConfidence()));
//				magnetRow.put("time", String.valueOf(MAGNET_WEI));
//				magnetRow.put("result", DetectionProfile.getDetectionResultAsString(magnetProfile));
//
//				HashMap<String, String> cellTowerRow = new HashMap<String, String>();
//				cellTowerRow.put("title", "C");
//				cellTowerRow.put("indoor", "0");
//				cellTowerRow.put("transition", "0");
//				cellTowerRow.put("outdoor", "0");
//				cellTowerRow.put("time", String.valueOf(CELL_WEI));
//				cellTowerRow.put("result", DetectionProfile.getDetectionResultAsString(cellProfile));
//				cellTowerMode.setCellCount(0);
//
//				// 废弃
//				HashMap<String, String> temperatureRow = new HashMap<String, String>();
//				temperatureRow.put("title", "T");
//				temperatureRow.put("indoor", "0");
//				temperatureRow.put("transition", "0");
//				temperatureRow.put("outdoor", "0");
//				temperatureRow.put("time", String.valueOf(TEMP_WEI));
//				temperatureRow.put("result", DetectionProfile.getDetectionResultAsString(0, 0, 0));
//
//				// 废弃
//				// HashMap<String, String> humidityRow = new HashMap<String,
//				// String>();
//				// humidityRow.put("title", "H");
//				// humidityRow.put("indoor",
//				// df1.format(humidityProfile[0].getConfidence()));
//				// humidityRow.put("transition",
//				// df1.format(humidityProfile[1].getConfidence()));
//				// humidityRow.put("outdoor",
//				// df1.format(humidityProfile[2].getConfidence()));
//				// humidityRow.put("time", String.valueOf(humidityTime));
//				//
//				// humidityRow.put(
//				// "result",
//				// getDetectionResultAsString(
//				// humidityProfile[0].getConfidence(),
//				// humidityProfile[2].getConfidence(),
//				// humidityProfile[1].getConfidence()));
//				// 废弃
//				HashMap<String, String> soundRow = new HashMap<String, String>();
//				soundRow.put("title", "S");
//				soundRow.put("indoor", "0");
//				soundRow.put("transition", "0");
//				soundRow.put("outdoor", "0");
//				soundRow.put("time", String.valueOf(SOUND_WEI));
//				soundRow.put("result", DetectionProfile.getDetectionResultAsString(0, 0, 0));
//
//				HashMap<String, String> gpgsvRow = new HashMap<String, String>();
//				gpgsvRow.put("title", "G");
//				gpgsvRow.put("indoor", df1.format(gpgsvProfile[0].getConfidence()));
//				gpgsvRow.put("transition", df1.format(gpgsvProfile[1].getConfidence()));
//				gpgsvRow.put("outdoor", df1.format(gpgsvProfile[2].getConfidence()));
//				gpgsvRow.put("time", String.valueOf(GPGSV_WEI));
//				gpgsvRow.put("result", DetectionProfile.getDetectionResultAsString(gpgsvProfile));
//
//				HashMap<String, String> pressureRow = new HashMap<String, String>();
//				pressureRow.put("title", "P");
//				pressureRow.put("indoor", df1.format(pressureProfile[0].getConfidence()));
//				pressureRow.put("transition", df1.format(pressureProfile[1].getConfidence()));
//				pressureRow.put("outdoor", df1.format(pressureProfile[2].getConfidence()));
//				pressureRow.put("time", String.format("%.2f", PRESSURE_WEI));
//				pressureRow.put("result", DetectionProfile.getDetectionResultAsString(pressureProfile));
//
//				HashMap<String, String> directionRow = new HashMap<String, String>();
//				directionRow.put("title", "D");
//				directionRow.put("indoor", df1.format(directionProfile[0].getConfidence()));
//				directionRow.put("transition", df1.format(directionProfile[1].getConfidence()));
//				directionRow.put("outdoor", df1.format(directionProfile[2].getConfidence()));
//				directionRow.put("time", String.valueOf(DIRECTION_WEI));
//				directionRow.put("result", DetectionProfile.getDetectionResultAsString(directionProfile));
//
//				HashMap<String, String> dutyRatioRow = new HashMap<String, String>();
//				dutyRatioRow.put("title", "R");
//				dutyRatioRow.put("indoor", df1.format(dutyRatioProfile[0].getConfidence()));
//				dutyRatioRow.put("transition", df1.format(dutyRatioProfile[1].getConfidence()));
//				dutyRatioRow.put("outdoor", df1.format(dutyRatioProfile[2].getConfidence()));
//				dutyRatioRow.put("time", String.valueOf(DUTYRATIO_WEI));
//				dutyRatioRow.put("result", DetectionProfile.getDetectionResultAsString(dutyRatioProfile));
//
//				HashMap<String, String> cinrRow = new HashMap<String, String>();
//				cinrRow.put("title", "CI");
//				cinrRow.put("indoor", "0");
//				cinrRow.put("transition", "0");
//				cinrRow.put("outdoor", "0");
//				cinrRow.put("time", String.valueOf(CINR_WEI));
//				cinrRow.put("result", DetectionProfile.getDetectionResultAsString(0, 0, 0));
//
//				separatedResultUpdatedCallBack(detectionStatus,detectionStatusHMM, lightRow, magnetRow, cellTowerRow, temperatureRow,
//						soundRow, gpgsvRow, pressureRow, directionRow, dutyRatioRow, cinrRow);
//			}
//
//			if (isReportAggregated) {
//				aggregatedResultUpdatedCallBack(detectionStatus,detectionStatusHMM);
//			}
//			aggregationFinish = true;// calculation finish
//		}
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
//	 */
//	WakeLock mWakeLock;
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i(TAG, "service on start command");
//		PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
//		mWakeLock.acquire();
//		flags = START_STICKY;
//		// return START_STICKY;
//		return super.onStartCommand(intent, flags, startId);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onDestroy()
//	 */
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Log.i(TAG, "service on destory");
//		// stop all the mode
//		unregisterReceiver(screenOnorOffReceiver);
//		magnetMode.stop();
//		// humidityMode.setRun(false);
//
//		// humidityMode.unregister();
//		lightMode.stop();
//		temperatureMode.stop();
//		cellTowerMode.stop();
//		pressureMode.stop();
//		directionMode.stop();
//		pressureMode.stop();
//		soundMode.stop();
//		cinrMode.stop();
//		dutyRatioMode.stop();
//		gpgsvMode.stop();
//		// cancel the timer
//		uiTimer.cancel();
//
//		// stop the weather fetching thread
//		sw.stop();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onLowMemory()
//	 */
//	@Override
//	public void onLowMemory() {
//		super.onLowMemory();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see android.app.Service#onUnbind(android.content.Intent)
//	 */
//	@Override
//	public boolean onUnbind(Intent intent) {
//		return super.onUnbind(intent);
//	}
//
//	// registered listeners for aggregated result
//	final RemoteCallbackList<IOAggregatedResultListener> mAggregatedListeners = new RemoteCallbackList<IOAggregatedResultListener>();
//	/**
//	 * registered listeners for separated result, these listener is used for
//	 * display the detail result of separated modes
//	 **/
//	final RemoteCallbackList<IOSeparatedResultListener> mSeparatedListeners = new RemoteCallbackList<IOSeparatedResultListener>();
//
//	final RemoteCallbackList<IODetailDataListener> mDetailDataListeners = new RemoteCallbackList<IODetailDataListener>();
//	private final IOService.Stub mBinder = new IOService.Stub() {
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#
//		 * removeIOSeparatedResultListener
//		 * (com.ict.iodetector.service.aidl.IOSeparatedResultListener)
//		 */
//		@Override
//		public void removeIOSeparatedResultListener(IOSeparatedResultListener mSeparatedListener)
//				throws RemoteException {
//			if (mSeparatedListener != null)
//				mSeparatedListeners.unregister(mSeparatedListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#
//		 * removeIOAggregatedResultListener
//		 * (com.ict.iodetector.service.aidl.IOAggregatedResultListener)
//		 */
//		@Override
//		public void removeIOAggregatedResultListener(IOAggregatedResultListener mAggregatedListener)
//				throws RemoteException {
//			if (mAggregatedListener != null)
//				mAggregatedListeners.unregister(mAggregatedListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#
//		 * addIOSeparatedResultListener
//		 * (com.ict.iodetector.service.aidl.IOSeparatedResultListener)
//		 */
//		@Override
//		public void addIOSeparatedResultListener(IOSeparatedResultListener mSeparatedListener) throws RemoteException {
//			if (mSeparatedListener != null)
//				mSeparatedListeners.register(mSeparatedListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#
//		 * addIOAggregatedResultListener
//		 * (com.ict.iodetector.service.aidl.IOAggregatedResultListener)
//		 */
//		@Override
//		public void addIOAggregatedResultListener(IOAggregatedResultListener mAggregatedListener)
//				throws RemoteException {
//
//			if (mAggregatedListener != null)
//				mAggregatedListeners.register(mAggregatedListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#getLastKnownStatus()
//		 */
//		@Override
//		public int getLastKnownStatus() throws RemoteException {
//
//			return detectionStatus;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see
//		 * com.ict.iodetector.service.aidl.IOService#addIODetailDataListener
//		 * (com.ict.iodetector.service.aidl.IODetailDataListener)
//		 */
//		@Override
//		public void addIODetailDataListener(IODetailDataListener mDetailDataListener) throws RemoteException {
//
//			if (mDetailDataListener != null)
//				mDetailDataListeners.register(mDetailDataListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see
//		 * com.ict.iodetector.service.aidl.IOService#removeIODetailDataListener
//		 * (com.ict.iodetector.service.aidl.IODetailDataListener)
//		 */
//		@Override
//		public void removeIODetailDataListener(IODetailDataListener mDetailDataListener) throws RemoteException {
//
//			if (mDetailDataListener != null)
//				mDetailDataListeners.unregister(mDetailDataListener);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see
//		 * com.ict.iodetector.service.aidl.IOService#startReportDetailData()
//		 */
//		@Override
//		public void startReportDetailData() throws RemoteException {
//
//			if (reportDetailDataTimer == null)
//				reportDetailDataTimer = new Timer();
//
//			// report data to the ui every 1 second
//			reportDetailDataTimer.scheduleAtFixedRate(new TimerTask() {
//
//				@Override
//				public void run() {
//					reportDetailData(
//
//							cellTowerMode.getCurrentCID(), cinrMode.getCinrValue(),
//							// cellTowerMode.getCurrentSignalStrength(),
//							cellTowerMode.getNeighboringInfo(), lightMode.getLightValue(),
//							temperatureMode.getTemperatureValue(),
//							// magnetMode.getMagnetDifference(),
//							// magnetMode.getMagnetIntensity(),
//							// magnetMode.getMagnetVariation(),
//							0, magnetMode.getMagnetIntensity(), 0, soundMode.getSoundDb(),
//							pressureMode.getPressureStrength(), pressureMode.getPressureVariance());
//				}
//
//			}, 0, 1000);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#stopReportDetailData()
//		 */
//		@Override
//		public void stopReportDetailData() throws RemoteException {
//
//			if (reportDetailDataTimer != null) {
//				reportDetailDataTimer.cancel();
//				reportDetailDataTimer = null;
//			}
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#startCollectData(long)
//		 * start to collect data in a interval
//		 */
//		@Override
//		public void startCollectData(long interval) throws RemoteException {
//
//			if (collectDataTimer == null)
//				collectDataTimer = new Timer();
//
//			collectDataTimer.scheduleAtFixedRate(new TimerTask() {
//
//				@Override
//				public void run() {
//
//					// String isWalking = "";
//					// if (DutyRatioMode.isWalking) {
//					// isWalking = "1";
//					// } else
//					// isWalking = "0";
//					DecimalFormat df2 = new DecimalFormat("0.000");
//					collectData(df2.format(lightMode.getIndoorsConfidenceOfLightMode()),
//							df2.format(magnetMode.getIndoorsConfidenceOfMagnetMode()),
//							df2.format(pressureMode.getIndoorsConfidenceOfPressureMode()),
//							df2.format(directionMode.getIndoorsConfidenceOfDirectionMode()),
//							df2.format(dutyRatioMode.getIndoorsConfidenceOfDutyRatioMode()));
//				}
//
//			}, 0, interval);
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see com.ict.iodetector.service.aidl.IOService#stopCollectData()
//		 */
//		@Override
//		public void stopCollectData() throws RemoteException {
//
//			if (collectDataTimer != null) {
//				collectDataTimer.cancel();
//				collectDataTimer = null;
//			}
//		}
//
//		@Override
//		public void startReportAggregatedResult() throws RemoteException {
//			// TODO Auto-generated method stub
//			isReportAggregated = true;
//		}
//
//		@Override
//		public void stopReportAggregatedResult() throws RemoteException {
//			// TODO Auto-generated method stub
//			isReportAggregated = false;
//		}
//
//		@Override
//		public void startReportSeparatedResult() throws RemoteException {
//			// TODO Auto-generated method stub
//			isReportSeparated = true;
//		}
//
//		@Override
//		public void stopReportSeparatedResult() throws RemoteException {
//			// TODO Auto-generated method stub
//			isReportSeparated = false;
//		}
//	};
//
//	/**
//	 * callback when the aggregated result changed, this happens when the scene
//	 * is switching
//	 * 
//	 * @param result
//	 */
//	private void aggregatedResultChangedCallBack(int result,int resultHMM) {
//		int N = mAggregatedListeners.beginBroadcast();
//		try {
//			for (int i = 0; i < N; i++) {
//				mAggregatedListeners.getBroadcastItem(i).onAggregatedResultChanged(result,resultHMM);
//			}
//		} catch (RemoteException e) {
//		}
//		mAggregatedListeners.finishBroadcast();
//	}
//
//	/**
//	 * callback method when the aggregated result updated, this happens every 3s
//	 * when the user is walking
//	 * 
//	 * @param result
//	 */
//	private void aggregatedResultUpdatedCallBack(int result,int resultHMM) {
//		int N = mAggregatedListeners.beginBroadcast();
//		try {
//			for (int i = 0; i < N; i++) {
//				mAggregatedListeners.getBroadcastItem(i).onAggregatedResultUpdated(result,resultHMM);
//			}
//		} catch (RemoteException e) {
//		}
//		mAggregatedListeners.finishBroadcast();
//	}
//
//	/**
//	 * callback when the separated result updated, this happens every 3s when
//	 * the user is walking, just for displaying more detail
//	 * 
//	 * @param aggregatedResult
//	 * @param lightResult
//	 * @param magnetResult
//	 * @param cellTowerResult
//	 * @param temperatureResult
//	 * @param soundResult
//	 * @param gpgsvResult
//	 * @param pressureResult
//	 * @param directionResult
//	 */
//	private void separatedResultUpdatedCallBack(int aggregatedResult,int aggregatedResultHMM, HashMap<String, String> lightResult,
//			HashMap<String, String> magnetResult, HashMap<String, String> cellTowerResult,
//			HashMap<String, String> temperatureResult, HashMap<String, String> soundResult,
//			HashMap<String, String> gpgsvResult, HashMap<String, String> pressureResult,
//			HashMap<String, String> directionResult, HashMap<String, String> dutyRatioResult,
//			HashMap<String, String> cinrRatioResult) {
//		int N = mSeparatedListeners.beginBroadcast();
//		try {
//			for (int i = 0; i < N; i++) {
//				mSeparatedListeners.getBroadcastItem(i).onSeparatedResultUpdated(aggregatedResult,aggregatedResultHMM, lightResult,
//						magnetResult, cellTowerResult, temperatureResult, soundResult, gpgsvResult, pressureResult,
//						directionResult, dutyRatioResult, cinrRatioResult);
//			}
//		} catch (RemoteException e) {
//		}
//		mSeparatedListeners.finishBroadcast();
//	}
//
//	/**
//	 * Detail data reported can be used for drawing callback when drawing the
//	 * chart, this happens every 1s
//	 * 
//	 * @param currentCID
//	 * @param currentSignalStrength
//	 * @param NeighboringList
//	 * @param lightIntensity
//	 * @param temperature
//	 * @param magnetDifference
//	 * @param magnetIntensity
//	 * @param magnetVariation
//	 * @param soundDb
//	 * @param pressureStrength
//	 * @param pressureVariance
//	 */
//	private void reportDetailData(int currentCID, int cinrValue, HashMap<String, String> NeighboringList,
//			float lightIntensity, float temperature, double magnetDifference, double magnetIntensity,
//			double magnetVariation, double soundDb, double pressureStrength, double pressureVariance) {
//		int N = mDetailDataListeners.beginBroadcast();
//		try {
//			for (int i = 0; i < N; i++) {
//				mDetailDataListeners.getBroadcastItem(i).onDetailDataUpdated(currentCID, cinrValue, NeighboringList,
//						lightIntensity, temperature, magnetDifference, magnetIntensity, magnetVariation, soundDb,
//						pressureStrength, pressureVariance);
//			}
//		} catch (RemoteException e) {
//		}
//		mDetailDataListeners.finishBroadcast();
//	}
//
//	/**
//	 * callback when collecting data, this happens in a interval just for mining
//	 * the overall feature
//	 * 
//	 * @param signalStrength
//	 * @param visibleCellNum
//	 * @param rssiAverage
//	 * @param rssiVariation
//	 * @param temperature
//	 * @param lightValue
//	 * @param magnetIntensity
//	 * @param soundDb
//	 * @param isWalking
//	 * 
//	 */
//	private void collectData(String indoorsConfidenceOfLightMode, String indoorsConfidenceOfMagnetMode,
//			String indoorsConfidenceOfPressureMode, String indoorsConfidenceOfDirectionMode,
//			String indoorsConfidenceOfDutyRatioMode) {
//		int N = mDetailDataListeners.beginBroadcast();
//		try {
//			for (int i = 0; i < N; i++) {
//				// mDetailDataListeners.getBroadcastItem(i).onCollectData(
//				// signalStrength, visibleCellNum, rssiAverage,
//				// rssiVariation, temperature, lightValue,
//				// magnetIntensity, soundDb, satelliteNum, turns,
//				// isWalking);
//				mDetailDataListeners.getBroadcastItem(i).onCollectData(indoorsConfidenceOfLightMode,
//						indoorsConfidenceOfMagnetMode, indoorsConfidenceOfPressureMode,
//						indoorsConfidenceOfDirectionMode, indoorsConfidenceOfDutyRatioMode);
//			}
//		} catch (RemoteException e) {
//		}
//		mDetailDataListeners.finishBroadcast();
//	}
//}
