/**
 *
 * Copyright 2015 YunRang Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : LYY
 *
 * @Description :
 *
 */

package com.lyy.sensordatacollection.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

/**
 * @author LYY
 *
 */
public class FileUtil {
	
   public static final String TAG = "FileUtil";

  /**
   * @Title: FileHelper.java
   * @Package com.tes.textsd
   * @Description: TODO(用一句话描述该文件做什么)
   * @author Alex.Z
   * @date 2013-2-26 下午5:45:40
   * @version V1.0
   */

  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  
  /** 路径 **/
  public static String SDPATH;

  static{
    File  file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ATransSensorDataCollect");
    if(!file.exists())
      file.mkdir();

    SDPATH = file.getPath();
  }

  /**
   * 在SD卡上创建文件
   *
   * @throws IOException
   */
  public static File createSDFile(String fileName) throws IOException {
    File file = new File(SDPATH + File.separator + fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    
    Log.i(TAG,file.getAbsolutePath());
    
    return file;
  }

  /**
   * 删除SD卡上的文件
   *
   * @param fileName
   */
  public static boolean deleteSDFile(String fileName) {
    File file = new File(SDPATH + File.separator + fileName);
    if (file == null || !file.exists() || file.isDirectory())
      return false;
    return file.delete();
  }

  /**
   * 写入内容到SD卡中的txt文本中 str为内容
   */
  public static void writeSDFile(String str, String fileName) {
    try {

      FileWriter fw = new FileWriter(SDPATH+File.separator+fileName,true);
      fw.append(str);
      fw.close();

    }

    catch (Exception e) {

      e.printStackTrace();

    }
  }

  /**
   * 读取SD卡中文本文件
   *
   * @param fileName
   * @return
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public static String readSDFile(String fileName) throws Exception {
    String res = "";

    BufferedReader bufReader = null;
    InputStreamReader isr = null;

    isr = new InputStreamReader(new FileInputStream(fileName), "utf-8");
    bufReader = new BufferedReader(isr);

    while (bufReader.ready()) {
      // 1. 得到每一行数据
      String dataLine = bufReader.readLine();
      System.out.println(dataLine);
    }

    bufReader.close();
    isr.close();

    return res;
  }

}
