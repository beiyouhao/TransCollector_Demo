package com.lyy.sensordatacollection.beans;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
//import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.util.SparseIntArray;
import com.ict.iodetector.service.bean.DetectionProfile;
import com.ict.iodetector.service.utils.StatisticsUtil;

public class CellTowerMode {

	/**
	 * @author SuS four status
	 */
	public enum PrevStatus {
		NO_INPUT(0), INDOOR(1), SEMI_OUTDOOR(2), OUTDOOR(3);

		private int value;

		PrevStatus(int value) {
			this.setValue(value);
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	TelephonyManager telephonyManager;
	// GsmCellLocation cellLocation;

	private DetectionProfile[] listProfile = new DetectionProfile[3];
	private DetectionProfile indoor, semi, outdoor;
	private int cellCount = 0;

	// private FileStorageUtil fsUtil;

	/**
	 * @return cellCount
	 */
	public int getCellCount() {
		return cellCount;
	}

	/**
	 * @param cellCount
	 */
	public void setCellCount(int cellCount) {
		this.cellCount = cellCount;
	}

	private int currentCID = -1;

	/**
	 * @return currentCID
	 */
	public int getCurrentCID() {
		return currentCID;
	}

	/**
	 * @param currentCID
	 */
	public void setCurrentCID(int currentCID) {
		this.currentCID = currentCID;
	}

	private int currentSignalStrength = -1;

	/**
	 * @return currentSignalStrength
	 */
	public int getCurrentSignalStrength() {
		if (enable)
			return currentSignalStrength;
		else
			return 0;
	}

	/**
	 * @param currentSignalStrength
	 */
	public void setCurrentSignalStrength(int currentSignalStrength) {
		this.currentSignalStrength = currentSignalStrength;
	}

	final Calendar c = Calendar.getInstance();
	int Month = c.get(Calendar.MONTH) + 1;
	String currentTime = c.get(Calendar.YEAR) + "_" + Month + "_"
			+ c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY)
			+ "_" + c.get(Calendar.MINUTE);

	private SparseIntArray cellArray;

	private int THRESHOLD = 15;

	private PrevStatus prevStatus = PrevStatus.NO_INPUT;
	private final double CONAVEHIGH = -70.5;
	private final double NEIAVEHIGH = -80.5;
	private final double AVELOW = -107.5;

	private boolean enable;

	/**
	 * @return enable
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * @param tManager
	 * @param context
	 * @param enable
	 */
	public CellTowerMode(TelephonyManager tManager, Context context,
			boolean enable) {
		this.enable = enable;
		indoor = new DetectionProfile("indoor");
		semi = new DetectionProfile("semi-outdoor");
		outdoor = new DetectionProfile("outdoor");
		listProfile[0] = indoor;
		listProfile[1] = semi;
		listProfile[2] = outdoor;

		if (enable) {
			this.telephonyManager = tManager;

			cellArray = new SparseIntArray(7);
		}
	}

	public void start() {
		if (enable)
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
							| PhoneStateListener.LISTEN_CELL_LOCATION);
	}

	/**
	 * unregister
	 */
	public void stop() {
		if (enable)
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
	}

	/**
	 * @param status
	 */
	public void setPrevStatus(int status) {
		if (status == 0)
			prevStatus = PrevStatus.INDOOR;
		else if (status == 1) {
			prevStatus = PrevStatus.SEMI_OUTDOOR;
		} else {
			prevStatus = PrevStatus.OUTDOOR;
		}
	}

	/**
	 * get Neighboring information
	 * 
	 * @return temp
	 */
	public HashMap<String, String> getNeighboringInfo() {
		HashMap<String, String> temp = new HashMap<String, String>();
		if (enable) {
			List<NeighboringCellInfo> neighboringList = telephonyManager
					.getNeighboringCellInfo();
			for (int i = 0; i < neighboringList.size(); i++) {
				temp.put(String.valueOf(neighboringList.get(i).getCid()),
						String.valueOf(neighboringList.get(i).getRssi()));
			}
		}
		return temp;
	}

	PhoneStateListener phoneStateListener = new PhoneStateListener() {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			Log.d("signalstren",""+ signalStrength.getGsmSignalStrength());
			super.onSignalStrengthsChanged(signalStrength);

			if (signalStrength.isGsm()) {
				currentSignalStrength = -113 + 2
						* signalStrength.getGsmSignalStrength();
			} else {
				currentSignalStrength = signalStrength.getCdmaDbm();
				if (currentSignalStrength == -1) {
					currentSignalStrength = signalStrength.getEvdoDbm();
				}
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.telephony.PhoneStateListener#onCellLocationChanged(android
		 * .telephony.CellLocation)
		 */
		@Override
		public void onCellLocationChanged(CellLocation location) {
			// TODO Auto-generated method stub
			super.onCellLocationChanged(location);
			// cellLocation = (GsmCellLocation)
			// telephonyManager.getCellLocation();
			if (location instanceof GsmCellLocation) {
				currentCID = ((GsmCellLocation) location).getCid();
			} else {
				CdmaCellLocation cdma = (CdmaCellLocation) location;
				currentCID = cdma.getBaseStationId();
			}
		}
	};

	/**
	 * Get all the cell rssi at time = 0s
	 */
	@SuppressLint("NewApi")
	public void initialProfile() {
		List<NeighboringCellInfo> NeighboringList = telephonyManager
				.getNeighboringCellInfo();
		

		// Save the current connected cell
		cellArray.put(currentCID, currentSignalStrength);

		// Save the current detected neighbour
		for (int i = 0; i < NeighboringList.size(); i++) {
			NeighboringCellInfo cellInfo = NeighboringList.get(i);
			int rssi = cellInfo.getRssi();
			rssi = -113 + rssi * 2;
			if (rssi == NeighboringCellInfo.UNKNOWN_RSSI || rssi == 85) {
				continue;
			}
			cellArray.put(cellInfo.getCid(), rssi);
		}
	}

	/**
	 * Check the cell variance after 10s
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public DetectionProfile[] getProfile() {
		if (enable) {
			int oldCellRssi, newCellRssi;
			double inToOut = 0, outToIn = 0, semiAround = 0;

			newCellRssi = currentSignalStrength;// New connected cell rssi
			cellCount++;

			if ((oldCellRssi = cellArray.get(currentCID, 0)) != 0) {// Compare
																	// the
																	// variance
																	// with
																	// the old
																	// connected

				if (newCellRssi - oldCellRssi >= THRESHOLD)
					inToOut++;
				else if (newCellRssi - oldCellRssi <= -THRESHOLD) {
					outToIn++;
				} else {
					if (prevStatus == PrevStatus.INDOOR) {
						outToIn += 1;
						semiAround += 1;
					} else if (prevStatus == PrevStatus.OUTDOOR
							|| prevStatus == PrevStatus.SEMI_OUTDOOR) {
						inToOut += 1;
						semiAround += 1;
					}
				}
				cellArray.put(currentCID, newCellRssi);
				// cell rssi
			}

			List<NeighboringCellInfo> NeighboringList = telephonyManager
					.getNeighboringCellInfo();
			/*List<CellInfo> lCells = telephonyManager.getAllCellInfo();
			System.out.println("llllllllllllllllllllllll:"+lCells.toString());*/

			for (int i = 0; i < NeighboringList.size(); i++) {// Calculate the
																// cell
																// variance for
																// all
																// the detected
																// neighbour
																// rssi
				NeighboringCellInfo cellInfo = NeighboringList.get(i);
				if (cellInfo.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI
						&& cellInfo.getRssi() != 85) {
					cellCount++;
				}

				if ((oldCellRssi = cellArray.get(cellInfo.getCid(), 0)) != 0) {

					newCellRssi = cellInfo.getRssi();

					newCellRssi = -113 + newCellRssi * 2;

					if (newCellRssi == NeighboringCellInfo.UNKNOWN_RSSI
							|| newCellRssi == 85) {
						continue;
					}

					if (newCellRssi - oldCellRssi >= THRESHOLD)
						inToOut++;
					else if (newCellRssi - oldCellRssi <= -THRESHOLD) {
						outToIn++;
					} else {// if the changes is between 15dB
						if (prevStatus == PrevStatus.INDOOR) {
							outToIn += 1;
							semiAround += 1;
						} else if (prevStatus == PrevStatus.OUTDOOR
								|| prevStatus == PrevStatus.SEMI_OUTDOOR) {
							inToOut += 1;
							semiAround += 1;
						}
					}
					cellArray.put(cellInfo.getCid(), newCellRssi);
				}
			}
			setCellCount(cellCount);
			// currentSignalStrength+113-2*NeighboringList.get(0).getRssi()
			if (cellCount == 0) {
				indoor.setConfidence(0);
				semi.setConfidence(0);
				outdoor.setConfidence(0);
			} else {

				/*
				 * if (NeighboringList.size() == 0 || (NeighboringList.size() ==
				 * 1 && currentSignalStrength + 113 - 2 *
				 * NeighboringList.get(0).getRssi() > 45)) {
				 * indoor.setConfidence((0.4+outToIn / cellCount)/2);
				 * semi.setConfidence((0.4+inToOut / cellCount)/2);
				 * outdoor.setConfidence((0.2+inToOut / cellCount)); } else {
				 */
				indoor.setConfidence((outToIn / cellCount));

				semi.setConfidence(0);

				outdoor.setConfidence((inToOut / cellCount));

			}

			if (indoor.getConfidence() > outdoor.getConfidence()
					&& indoor.getConfidence() >= semi.getConfidence()) {// Indoor
				prevStatus = PrevStatus.INDOOR;
			} else if (outdoor.getConfidence() > indoor.getConfidence()
					&& outdoor.getConfidence() >= semi.getConfidence()) {
				prevStatus = PrevStatus.OUTDOOR;
			} else if (semi.getConfidence() > indoor.getConfidence()
					&& semi.getConfidence() > outdoor.getConfidence()) {// Semi
				prevStatus = PrevStatus.SEMI_OUTDOOR;
			} else {// Outdoor
				prevStatus = PrevStatus.NO_INPUT;
			}
		}
		return listProfile;
	}

	private double[] allRssi;

	/**
	 * @return visiableCellNum
	 */
	public int getVisibleCellNum() {
		int visiableCellNum = 0;
		if (enable) {
			List<NeighboringCellInfo> neighboringList = telephonyManager
					.getNeighboringCellInfo();
			visiableCellNum = neighboringList.size() + 1;
			allRssi = new double[visiableCellNum];
			for (int i = 0; i < visiableCellNum - 1; i++) {
				allRssi[i] = neighboringList.get(i).getRssi();
			}

			allRssi[visiableCellNum - 1] = currentSignalStrength;
		}
		return visiableCellNum;
	}

	/**
	 * @return average
	 */
	public double getRssiAverage() {
		if (enable)
			return StatisticsUtil.getAverage(allRssi,
					NeighboringCellInfo.UNKNOWN_RSSI);
		else
			return 0;
	}

	/**
	 * @return variation
	 */
	public double getRssiVariation() {
		if (enable)
			return StatisticsUtil.getVariation(allRssi,
					NeighboringCellInfo.UNKNOWN_RSSI);
		else
			return 0;
	}

	/**
	 * @return THRESHOLD
	 */
	public int getTHRESHOLD() {
		return THRESHOLD;
	}

	/**
	 * @param tHRESHOLD
	 */
	public void setTHRESHOLD(int tHRESHOLD) {
		THRESHOLD = tHRESHOLD;
	}
}
