package com.ict.iodetector.service.bean;

//import test.betweenDays;

//import java.sql.Date;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;

public class SunTimes {
	static Double start = 0.0;
	static Double end = 0.0;
	static Double sRA = 0.0;
	static Double sdec = 0.0;
	static Double sr = 0.0;
	static Double lon = 0.0;

	double Longitude = 116.32008, Latitude = 39.980577;

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double latitude) {
		Latitude = latitude;
	}

	/*
	 * public static void main(String[] args) throws ParseException {
	 * 
	 * SunTimes st = new SunTimes();
	 * 
	 * System.out.println("1:  " + st.sun_Rise() + "2:   " + st.sun_Set());
	 * 
	 * }
	 */
	public SunTimes() {

	}

	private double readloglongitude() {
		// TODO Auto-generated method stub

		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IODetector/"
					+ "loglatitudeandlongitude.txt");
			if (!file.exists()) {
				return Longitude;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = "";
			// double Longitude,Latitude;
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				int a = str.indexOf(" ");
				if (a == -1) {
					a = 0;
				}
				String newStr = str.substring(a);
				String[] arryStr = newStr.split(" ");

				String[] result = new String[2];

				int flag = 0;
				for (int i = 0; i < arryStr.length; i++) {

					if (!arryStr[i].trim().equals("")) {
						result[flag] = arryStr[i];
						flag++;
					}
				}
				Longitude = Double.parseDouble(result[1]);
				// setLatitude(Double.parseDouble(result[1]));
				// setLongitude(Double.parseDouble(result[0]));
				// Longitude=Double.parseDouble(result[0]);
				// Latitude

				// System.out.println(Latitude-Longitude);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Longitude;

	}

	private double readloglatitude() {
		// TODO Auto-generated method stub

		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IODetector/"
					+ "loglatitudeandlongitude.txt");
			if (!file.exists()) {
				return Latitude;
			}
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = "";
			// double Longitude,Latitude;
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				int a = str.indexOf(" ");
				if (a == -1) {
					a = 0;
				}
				String newStr = str.substring(a);
				String[] arryStr = newStr.split(" ");

				String[] result = new String[2];

				int flag = 0;
				for (int i = 0; i < arryStr.length; i++) {

					if (!arryStr[i].trim().equals("")) {
						result[flag] = arryStr[i];
						flag++;
					}
				}

				Latitude = (Double.parseDouble(result[0]));

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Latitude;

	}

	public static String sun_Rise() {

		betweenDays bd = new betweenDays();
		SunTimes st = new SunTimes();

		// System.out.println("la"+st.readloglatitude()+"lon"+st.readloglongitude());

		try {
			SunRiset(bd.betWeenday(), st.readloglongitude(), st.readloglatitude(), -35.0 / 60.0, 1, start, end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sunrise = ToLocalTime(start);
		return sunrise;
	}

	public static String sun_Set() {
		betweenDays bd = new betweenDays();

		SunTimes st = new SunTimes();

		System.out.println("la" + st.readloglatitude() + "lon" + st.readloglongitude());
		try {
			SunRiset(bd.betWeenday(), st.readloglongitude(), st.readloglatitude(), -35.0 / 60.0, 1, start, end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sunset = ToLocalTime(end);
		return sunset;
	}

	private static String ToLocalTime(Double utTime) {
		int hour = (int) (Math.floor(utTime));
		Double temp = utTime - hour;
		hour += 8;
		temp = temp * 60;
		int minute = (int) (Math.floor(temp));
		String minuteStr = Integer.toString(minute);
		if (minute < 10) {
			minuteStr = "0" + minute;
		}
		return hour + ":" + minuteStr;
	}

	private static void Sunpos(Double d, Double lon, Double r) {
		Double M, //
				w, //
				e, //
				E, //

				x, y, v; //

		M = Revolution(356.0470 + 0.9856002585 * d);//
		w = 282.9404 + 4.70935E-5 * d;//

		e = 0.016709 - 1.151E-9 * d;//

		E = M + e * Radge * Sind(M) * (1.0 + e * Cosd(M));
		x = Cosd(E) - e;
		y = Math.sqrt(1.0 - e * e) * Sind(E);
		setSr(Math.sqrt(x * x + y * y));
		v = Atan2d(y, x);
		lon = v + w;
		setLon(lon);
		if (lon >= 360.0) {
			lon -= 360.0;
			setLon(lon);
		}
	}

	private static void Sun_RA_dec(Double d, Double RA, Double dec, Double r) {
		Double obl_ecl, x, y, z;
		Sunpos(d, lon, r);
		x = sr * Cosd(lon);
		y = sr * Sind(lon);
		obl_ecl = 23.4393 - 3.563E-7 * d;
		z = y * Sind(obl_ecl);
		y = y * Cosd(obl_ecl);

		setsRA(Atan2d(y, x));
		setSdec(Atan2d(z, Math.sqrt(x * x + y * y)));

	}

	private static int SunRiset(long day, Double longitude, Double lat, Double altit, int upper_limb, Double trise,
			Double tset) {
		Double d, /* Days since 2000 Jan 0.0 (negative before) */

				sradius, /* Sun's apparent radius */

				t, /* Diurnal arc */

				tsouth, /* Time when Sun is at south */
				sidtime; /* Local sidereal time */

		int rc = 0; /* Return cde from function - usually 0 */

		/* Compute d of 12h local mean solar time */
		d = day/* Days_since_2000_Jan_0(date) */ + 0.5 - longitude / 360.0;

		/* Compute local sideral time of this moment */
		sidtime = Revolution(GMST0(d) + 180.0 + longitude);

		/* Compute Sun's RA + Decl at this moment */
		Sun_RA_dec(d, sRA, sdec, sr);

		/* Compute time when Sun is at south - in hours UT */
		tsouth = 12.0 - Rev180(sidtime - sRA) / 15.0;

		/* Compute the Sun's apparent radius, degrees */
		sradius = 0.2666 / sr;

		/* Do correction to upper limb, if necessary */
		if (upper_limb != 0)
			altit -= sradius;

		/* Compute the diurnal arc that the Sun traverses to reach */
		/* the specified altitide altit: */

		Double cost;
		cost = (Sind(altit) - Sind(lat) * Sind(sdec)) / (Cosd(lat) * Cosd(sdec));
		if (cost >= 1.0) {
			rc = -1;
			t = 0.0;
		} else {
			if (cost <= -1.0) {
				rc = +1;
				t = 12.0; /* Sun always above altit */
			} else
				t = Acosd(cost) / 15.0; /* The diurnal arc, hours */
		}

		/* Store rise and set times - in hours UT */
		setStart(tsouth - t);
		setEnd(tsouth + t);
		return rc;
	}

	private static Double Revolution(Double x) {
		return (x - 360.0 * Math.floor(x * Inv360));
	}

	private static Double Rev180(Double x) {
		return (x - 360.0 * Math.floor(x * Inv360 + 0.5));
	}

	private static Double GMST0(Double d) {
		Double sidtim0;
		sidtim0 = Revolution((180.0 + 356.0470 + 282.9404) + (0.9856002585 + 4.70935E-5) * d);
		return sidtim0;
	}

	private static Double Inv360 = 1.0 / 360.0;

	private static Double Sind(Double x) {
		return Math.sin(x * Degrad);
	}

	private static Double Cosd(Double x) {
		return Math.cos(x * Degrad);
	}

	private static Double Acosd(Double x) {
		return Radge * Math.acos(x);
	}

	private static Double Atan2d(Double y, Double x) {
		return Radge * Math.atan2(y, x);

	}

	private static Double Radge = 180.0 / Math.PI;
	private static Double Degrad = Math.PI / 180.0;

	public static Double getStart() {
		return start;
	}

	public static void setStart(Double start) {
		SunTimes.start = start;
	}

	public static Double getsRA() {
		return sRA;
	}

	public static void setsRA(Double sRA) {
		SunTimes.sRA = sRA;
	}

	public static Double getSdec() {
		return sdec;
	}

	public static void setSdec(Double sdec) {
		SunTimes.sdec = sdec;
	}

	public static Double getSr() {
		return sr;
	}

	public static void setSr(Double sr) {
		SunTimes.sr = sr;
	}

	public static Double getLon() {
		return lon;
	}

	public static void setLon(Double lon) {
		SunTimes.lon = lon;
	}

	public static Double getEnd() {
		return end;
	}

	public static void setEnd(Double end) {
		SunTimes.end = end;
	}

}
