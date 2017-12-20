package com.lyy.sensordatacollection;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lyy.sensordatacollection.beans.CellTowerMode;
import com.lyy.sensordatacollection.beans.Vector3D;
import com.lyy.sensordatacollection.utils.FileUtil;

public class MainActivity extends Activity implements SensorEventListener {

	private static final String TAG = "MainActivity";
	TelephonyManager telephonyManager;
	// 当前状态，是否为静止
	private int currentState;
	// 判断是否为暂停状态
	private boolean isSuspend;

	// 当前交通方式
	private String transpMode;
	// 加速度
	private TextView mAccXTv;
	private TextView mAccYTv;
	private TextView mAccZTv;

	// 角速度
	private TextView mWXTv;
	private TextView mWYTv;
	private TextView mWZTv;

	// 磁传感器
	private TextView mMagXTv;
	private TextView mMagYTv;
	private TextView mMagZTv;

	// 拐弯和气压
	// private TextView mTurnCntTv;
	private TextView mPressureTv;

	private String singnalStrenth;

	private ToggleButton mStartTbtn;

	private ToggleButton staticbtn;

	private ToggleButton suspendbtn;

	RadioGroup transModegrp;
	RadioButton transModeButton;

	private SensorManager mSensorManager;
	// 加速度传感器
	private Sensor mAccSensor;
	// 陀螺仪
	private Sensor mGyroSensor;
	// 气压传感器
	private Sensor mPresSensor;
	// 地磁
	private Sensor mMagSensor;

	CellTowerMode cellmode;

	private Vector3D mAcc;
	private Vector3D mW;
	private Vector3D mMag;
	private float pressureValue;
	// private Vector3D mMag;

	// 时间
	private Timer mTimer;
	private float mT;

	private StringBuilder gsmCellInfo;
	private StringBuilder datalist;
	private ExecutorService threadPool;

	private String mFileName;
	private PowerManager manager = null;
	private WakeLock mWakeLock = null;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!isSuspend) {
				mAccXTv.setText("" + mAcc.getX());
				mAccYTv.setText("" + mAcc.getY());
				mAccZTv.setText("" + mAcc.getZ());

				mWXTv.setText("" + mW.getX());
				mWYTv.setText("" + mW.getY());
				mWZTv.setText("" + mW.getZ());

				mMagXTv.setText("" + mMag.getX());
				mMagYTv.setText("" + mMag.getY());
				mMagZTv.setText("" + mMag.getZ());

				mPressureTv.setText("" + pressureValue);

				String data;
				updateGsmCellInfo();

				data = mT + " " + MainActivity.this.currentState + " "
						+ mAcc.toString() + mW.toString() + mMag.toString()
						+ " " + pressureValue + gsmCellInfo.toString()
						+ FileUtil.LINE_SEPARATOR;

				datalist.append(data);

				if (mT % 1000 == 0) {
					final String dataString = datalist.toString();
					Log.d("length", dataString.length() + "");
					datalist.setLength(0);
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							FileUtil.writeSDFile(dataString,
									MainActivity.this.mFileName);
						}
					});
				}
			}
			mT += 10;
		}
	};
	// the moment of screen triggering (from close to open)/(from open to close)
	public static long ScreenOnTime = 0, ScreenOffTime = 0;
	private BroadcastReceiver screenOnorOffReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (Intent.ACTION_SCREEN_ON.equals(arg1.getAction())) {// when the
																	// screen is
				ScreenOnTime = System.currentTimeMillis();
			}
			if (Intent.ACTION_SCREEN_OFF.equals(arg1.getAction())) {
				ScreenOffTime = System.currentTimeMillis();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.manager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = this.manager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");
		// 注册屏幕开启关闭的广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenOnorOffReceiver, filter);
		initData();
		initView();
	}

	private void initData() {

		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

		mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		mPresSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

		mMagSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		mAcc = new Vector3D(0, 0, 0);
		mW = new Vector3D(0, 0, 0);
		mMag = new Vector3D(0, 0, 0);
		pressureValue = 0;
		gsmCellInfo = new StringBuilder();
		gsmCellInfo.append(" 0");
		datalist = new StringBuilder(10000);
		threadPool = Executors.newSingleThreadExecutor();
		currentState = 1;// 1表示行驶状态，0表示静止状态
		isSuspend = false;
	}

	private void initView() {

		mAccXTv = (TextView) findViewById(R.id.tv_acc_x);
		mAccYTv = (TextView) findViewById(R.id.tv_acc_y);
		mAccZTv = (TextView) findViewById(R.id.tv_acc_z);

		mWXTv = (TextView) findViewById(R.id.tv_w_x);
		mWYTv = (TextView) findViewById(R.id.tv_w_y);
		mWZTv = (TextView) findViewById(R.id.tv_w_z);

		mMagXTv = (TextView) findViewById(R.id.tv_mag_x);
		mMagYTv = (TextView) findViewById(R.id.tv_mag_y);
		mMagZTv = (TextView) findViewById(R.id.tv_mag_z);

		mPressureTv = (TextView) findViewById(R.id.tv_pressure_dif);

		transModegrp = (RadioGroup) this.findViewById(R.id.transmodegroup);
		this.mStartTbtn = (ToggleButton) this.findViewById(R.id.tbtn_start);

		this.mStartTbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (MainActivity.this.mStartTbtn.isChecked()) {
					MainActivity.this.start();
				} else {
					MainActivity.this.stop();
				}

			}

		});
		this.staticbtn = (ToggleButton) this.findViewById(R.id.static_button);
		this.staticbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (MainActivity.this.staticbtn.isChecked()) {
					MainActivity.this.currentState = 0;
				} else {
					MainActivity.this.currentState = 1;
				}
			}
		});
		this.suspendbtn = (ToggleButton) this.findViewById(R.id.temp_start);
		this.suspendbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (MainActivity.this.suspendbtn.isChecked()) {
					MainActivity.this.isSuspend = true;
				} else {
					MainActivity.this.isSuspend = false;
				}
			}
		});

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:

			mAcc.setValues(event.values);

			break;

		case Sensor.TYPE_GYROSCOPE:

			mW.setValues(event.values);

			break;

		case Sensor.TYPE_MAGNETIC_FIELD:

			mMag.setValues(event.values);

			break;

		case Sensor.TYPE_PRESSURE:
			this.setPressureValue(event.values[0]);
			break;

		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (this.mTimer != null) {
			this.mTimer.cancel();
		}
		this.mSensorManager.unregisterListener(this);
		unregisterReceiver(screenOnorOffReceiver);
	}

	private void start() {

		mWakeLock.acquire();

		mSensorManager.registerListener(this, mAccSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mGyroSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mPresSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mMagSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		cellmode = new CellTowerMode(telephonyManager, this, true);
		cellmode.start();
		try {
			String currentTime = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss")
					.format(new Date(System.currentTimeMillis()));

			transModeButton = (RadioButton) this.findViewById(transModegrp
					.getCheckedRadioButtonId());
			this.transpMode = (String) transModeButton.getText();
			this.mFileName = this.transpMode + currentTime + ".txt";
			FileUtil.createSDFile(this.mFileName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("exception", e.toString());
		}

		this.mT = 0;
		this.mTimer = new Timer();
		this.mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				MainActivity.this.mHandler
						.sendMessage(MainActivity.this.mHandler.obtainMessage());
			}
		}, 0, 10);// 10毫秒执行一次

	}

	private void stop() {
		this.mSensorManager.unregisterListener(this);
		cellmode.stop();
		mWakeLock.release();
		this.mTimer.cancel();
		this.staticbtn.setChecked(false);
		this.suspendbtn.setChecked(false);
		this.mTimer = null;

	}

	public float getPressureValue() {
		return pressureValue;
	}

	public void setPressureValue(float pressureValue) {
		this.pressureValue = pressureValue;
	}

	private void updateGsmCellInfo() {

		gsmCellInfo.setLength(0);
		gsmCellInfo.append(" " + cellmode.getCurrentCID() + " "
				+ cellmode.getCurrentSignalStrength());

	}
}
