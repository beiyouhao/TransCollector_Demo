package com.ict.iodetector.service.utils;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class FileStorageUtil {
	private final static Calendar c = Calendar.getInstance();

	/**
	 * result storage method, single file, every hour a file and so on
	 */
	public final static int SINGLE_FILE = 0;
	public final static int EVERY_ONE_HOUR = 1;
	public final static int EVERY_HALF_DAY = 2;
	public final static int EVERY_ONE_DAY = 3;

	private int type;
	private long startTime;
	private String startTimeString;
	private File directory;
	private final static String baseDir = "/IODetector/";

	/**
	 * 写文件函数，默认单文件
	 * 
	 * @param dirName
	 * @param fileName
	 * @param content
	 */
	public static void writeAutoFile(String dirName, String fileName, String content) {
		FileStorageUtil fsu = new FileStorageUtil(SINGLE_FILE, dirName);
		fsu.ResultStorage(content, fileName);
		return;
	}

	/**
	 * 写入目录Iodetector中,只有一个文件，默认文件名为iodetector.log
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void writeManualFile(String fileName, String content) {
		// SD Card Storage
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File(sdCard.getAbsolutePath() + "/IODetector/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (fileName.isEmpty()) {
			fileName = "iodetector.log";
		}
		File file = new File(directory, fileName);

		try {
			// if not exist, create it
			if (!file.exists())
				file.createNewFile();

			BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
			output.append(content);
			output.flush();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	/**
	 * initialize file storage util
	 * 
	 * @param type
	 * @param directoryName
	 */
	public FileStorageUtil(int type, String directoryName) {
		this.type = type;
		startTime = System.currentTimeMillis();
		startTimeString = c.get(Calendar.YEAR) + "_" + (c.get(Calendar.DAY_OF_MONTH) + 1) + "_"
				+ c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE);
		// SD Card Storage
		File sdCard = Environment.getExternalStorageDirectory();
		directory = new File(sdCard.getAbsolutePath() + baseDir + directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	/**
	 * store content
	 * 
	 * @param result
	 * @param tag
	 */
	public void ResultStorage(String result, String tag) {
		final String tempResult = result;
		final String tempTag = tag;

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ResultStorageRun(tempResult, tempTag);
			}

		}.start();
	}

	/**
	 * @param result
	 * @param tag
	 */
	private void ResultStorageRun(String result, String tag) {
		long currentTime = System.currentTimeMillis();
		File file;
		long peroid = Long.MAX_VALUE;
		switch (type) {
		case SINGLE_FILE:
			peroid = Long.MAX_VALUE;
			break;
		case EVERY_ONE_HOUR:
			peroid = 60 * 60 * 1000;
			break;
		case EVERY_HALF_DAY:
			peroid = 12 * 60 * 60 * 1000;
			break;
		case EVERY_ONE_DAY:
			peroid = 24 * 60 * 60 * 1000;
			break;
		}

		if (currentTime - startTime > peroid) {
			startTime = currentTime;
			startTimeString = c.get(Calendar.YEAR) + "_" + (c.get(Calendar.DAY_OF_MONTH) + 1) + "_"
					+ c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + ".txt";
		}
		file = new File(directory, tag + startTimeString + ".txt");

		try {
			// if not exist, create it
			if (!file.exists())
				file.createNewFile();

			BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
			output.append(result + "\r\n");
			output.flush();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 以下为IODetectorTest使用
	 */
	public final static String TEST_DIR = "test";
	public final static String USER_AUTO_DETECTION = "userDetectionandAutoDetection.txt";
	public static final String SPLITLINE = ";";
	public static final String SPLITSTR = ",";

	/**
	 * 保存用户检测结果和计算检测结果比较数据，默认目录在IODetector,下次可直接获取历史记录，计算正确率
	 * 
	 * @param content
	 * @param intent
	 */
	public static void save(String content, Context intent) {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File file = new File(Environment.getExternalStorageDirectory() + baseDir, USER_AUTO_DETECTION);

				if (!file.exists()) {
					file.createNewFile();
					content = "user input" + SPLITSTR + "auto Input" + SPLITSTR + "light ret" + SPLITSTR + "light val"
							+ SPLITSTR + "magnet ret" + SPLITSTR + "magnet val" + SPLITSTR + "pressure ret" + SPLITSTR
							+ "pressure val" + SPLITSTR + "direction ret" + SPLITSTR + "direction val" + SPLITSTR
							+ "dutyRatio ret" + SPLITSTR + "dutyRatio val" + SPLITSTR + "gpgsv ret" + SPLITSTR
							+ "gpgsv val" + SPLITSTR + "auto" + SPLITSTR + "hmm" + SPLITSTR + "user" + SPLITSTR + "time"
							+ "\r\n" + content;
				}
				FileOutputStream outputStream = new FileOutputStream(file, true);
				outputStream.write(content.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();
			} else {
				Toast.makeText(intent, "save failed", Toast.LENGTH_LONG).show();

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件或目录
	 * 
	 * @param url：文件所在地址,IODetector目录下的地址前面不加/
	 * @throws IOException
	 */
	public static void delete(String url) throws IOException {
		File file = new File(Environment.getExternalStorageDirectory() + baseDir + url);

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files.length == 0) {
				if (file.getAbsolutePath().indexOf(".txt") != -1) {
					System.out.println(file.getAbsoluteFile());
					file.delete();
				}
			} else {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						if (files[i].listFiles().length > 0) {
							delete(files[i].getAbsolutePath());
						} else {
							if (files[i].getAbsolutePath().indexOf(".txt") != -1) {
								System.out.println(file.getAbsoluteFile());
								files[i].delete();
							}
						}
					} else {
						if (files[i].getAbsolutePath().indexOf(".txt") != -1) {
							System.out.println(files[i].getAbsoluteFile());
							files[i].delete();
						}
					}
				}
			}
			if (file.getAbsolutePath().indexOf(".txt") != -1) {
				System.out.println(file.getAbsoluteFile());
				file.delete();
			}
		} else {
			if (file.getAbsolutePath().indexOf(".txt") != -1) {
				System.out.println(file.getAbsoluteFile());
				file.delete();
			}
		}
	}

	/**
	 * read compare result at first time
	 * 
	 * @param intent
	 */
	public static String readFile(Context intent) {
		StringBuilder ret = new StringBuilder();
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

				File file = new File(Environment.getExternalStorageDirectory() + baseDir, USER_AUTO_DETECTION);
				if (!file.exists()) {
					Toast.makeText(intent, "There is no historical data", Toast.LENGTH_LONG).show();
					return ret.toString();
				}

				FileInputStream inputStream = new FileInputStream(file);
				DataInputStream dataIO = new DataInputStream(inputStream);
				String strLine = null;
				while ((strLine = dataIO.readLine()) != null) {
					ret.append(strLine + SPLITLINE);
				}
				dataIO.close();
				inputStream.close();
			} else {
				Toast.makeText(intent, "read failed", Toast.LENGTH_LONG).show();

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret.toString();
	}
}
