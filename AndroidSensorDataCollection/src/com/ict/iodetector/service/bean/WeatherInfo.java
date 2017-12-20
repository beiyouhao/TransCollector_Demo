package com.ict.iodetector.service.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.os.Environment;

public class WeatherInfo {

	String city = "";
	String cityid = "";
	String temp = "28";
	String wd = "";
	String ws = "";
	String sd = "";
	String wse = "";
	String time = "";
	String isRadar = "";
	String radar = "";
	String sun_rise = "6:40";
	String sun_set = "17:10";
	String tempHigh = "15";
	String tempLow = "3";
	String pressure;
	String weathertext;

	public String getWeathertext() {
		return weathertext;
	}

	public void setWeathertext(String weathertext) {
		this.weathertext = weathertext;
	}

	/**
	 * @return the tempHigh
	 */
	public String getTempHigh() {
		return tempHigh;
	}

	/**
	 * @param tempHigh
	 *            the tempHigh to set
	 */
	public void setTempHigh(String tempHigh) {
		this.tempHigh = tempHigh;
	}

	/**
	 * @return the tempLow
	 */
	public String getTempLow() {
		return tempLow;
	}

	/**
	 * @param tempLow
	 *            the tempLow to set
	 */
	public void setTempLow(String tempLow) {
		this.tempLow = tempLow;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the cityid
	 */
	public String getCityid() {
		return cityid;
	}

	/**
	 * @param cityid
	 *            the cityid to set
	 */
	public void setCityid(String cityid) {
		this.cityid = cityid;
	}

	/**
	 * @return the temp
	 */
	public String getTemp() {
		return temp;
	}

	/**
	 * @param temp
	 *            the temp to set
	 */
	public void setTemp(String temp) {
		this.temp = temp;
	}

	/**
	 * @return the wd
	 */
	public String getWd() {
		return wd;
	}

	/**
	 * @param wd
	 *            the wd to set
	 */
	public void setWd(String wd) {
		this.wd = wd;
	}

	/**
	 * @return the ws
	 */
	public String getWs() {
		return ws;
	}

	/**
	 * @param ws
	 *            the ws to set
	 */
	public void setWs(String ws) {
		this.ws = ws;
	}

	/**
	 * @return the sd
	 */
	public String getSd() {
		return sd;
	}

	/**
	 * @param sd
	 *            the sd to set
	 */
	public void setSd(String sd) {
		this.sd = sd;
	}

	/**
	 * @return the wse
	 */
	public String getWse() {
		return wse;
	}

	/**
	 * @param wse
	 *            the wse to set
	 */
	public void setWse(String wse) {
		this.wse = wse;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the isRadar
	 */
	public String getIsRadar() {
		return isRadar;
	}

	/**
	 * @param isRadar
	 *            the isRadar to set
	 */
	public void setIsRadar(String isRadar) {
		this.isRadar = isRadar;
	}

	/**
	 * @return the radar
	 */
	public String getRadar() {
		return radar;
	}

	/**
	 * @param radar
	 *            the radar to set
	 */
	public void setRadar(String radar) {
		this.radar = radar;
	}

	/**
	 * @return the sun_rise
	 */
	public String getSun_rise() {
		return sun_rise;
	}

	/**
	 * @param sun_rise
	 *            the sun_rise to set
	 */
	public void setSun_rise(String sun_rise) {
		this.sun_rise = sun_rise;
	}

	/**
	 * @return the sun_set
	 */
	public String getSun_set() {
		return sun_set;
	}

	/**
	 * @param sun_set
	 *            the sun_set to set
	 */
	public void setSun_set(String sun_set) {
		this.sun_set = sun_set;
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// @Override
	public String toString() {
		// TODO Auto-generated method stub
		String content = "city:" + this.city + "\n" + "humid:" + this.sd + "\n" + "temperature" + this.temp + "��\n"
				+ "time:" + this.time + "\n" + "sun_rise:" + this.sun_rise + "\n" + "sun_set" + this.sun_set + "\n";
		return content;
	}

	// Get a weather when the Internet
	public static boolean readWeather() {
		// TODO Auto-generated method stub

		boolean Flg = false;

		File file = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() + "/IODetector/" + "logWeather.txt");
		if (!file.exists()) {
			return Flg;
		}
		FileReader reader;
		try {
			reader = new FileReader(file);
			int fileLen = (int) file.length();
			char[] chars = new char[fileLen];
			reader.read(chars);
			String txt = String.valueOf(chars);

			String Weather1 = "Haze";
			String Weather2 = "Smoke";

			if (txt.equals(Weather1) && txt.equals(Weather2)) {
				Flg = true;

			} else {
				Flg = false;

			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Flg;

	}
}
