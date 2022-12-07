package com.brightpattern.mobileagent;

import android.content.Context;
import android.text.format.DateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

public class LogUtils {
  public static void writeLogToFile (Context context, String string) {
    String logsAreEnabled = UserDefaultsModule.getUserDefaults("logsAreEnabled");

    if (!logsAreEnabled.equals("enabled")) return;

    File dir = new File(context.getApplicationInfo().dataDir + "/files");
    File[] files = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".log");
      }
    });

    if (files == null) return;

    long currentLogFile = 0;
    for (File file : files) {
      long fileName = Long.parseLong(file.getName().substring(0, file.getName().length() - 4));

      if (fileName > currentLogFile) {
        currentLogFile = fileName;
      }
    }

    File file = new File(dir.getPath() + "/" + currentLogFile + ".log");

    if (!file.exists()) return;

    String dateString = (String) DateFormat.format("yyyy-MM-dd HH:mm:ss:sss", new Date());

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

      try {
        writer.newLine();
        writer.write(dateString + " " + string);
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
