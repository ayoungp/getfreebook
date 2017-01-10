package com.peaceb.getfreebook;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLog {
    private static final String TAG = "FileLog";

    private static boolean _isLogEnable = true;

    private static final String VERBOSE = "V";
    private static final String DEBUG = "D";
    private static final String INFO = "I";
    private static final String WARN = "W";
    private static final String ERROR = "E";

    public static final String LOG_FILE_NAME = "log.txt";

    private static boolean _isInit = false;
    private static String _logFilePath = null;
    private static SimpleDateFormat _dateFormatter = null;

    public static void init(Context context) {
        if (_isInit) {
            return;
        }

        _isInit = true;
        _logFilePath = getLogFilePath(context);
        _dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static void v(String msg) {
        writeLog(VERBOSE, msg);
    }

    public static void d(String msg) {
        writeLog(DEBUG, msg);
    }

    public static void i(String msg) {
        writeLog(INFO, msg);
    }

    public static void w(String msg) {
        writeLog(WARN, msg);
    }

    public static void e(String msg) {
        writeLog(ERROR, msg);
    }

    private synchronized static void writeLog(String level, String msg) {
        if (!_isLogEnable) {
            return;
        }

        if (!_isInit) {
            Log.w(TAG, "filelog not initialized");
            return;
        }

        String contents = String.format("%s [%s] %s\r\n",
                _dateFormatter.format(new Date()), level, msg);
        try {
            RandomAccessFile file = new RandomAccessFile(_logFilePath, "rw");
            file.seek(file.length());
            file.write(contents.getBytes());
            file.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setEnable(boolean enable) {
        _isLogEnable = enable;
    }

    public synchronized static String getLogContents(Context context) {
        File file = new File(getLogFilePath(context));
        if (!file.exists()) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        try {
            String s;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((s = reader.readLine()) != null) {
                sb.append(s);
                sb.append("\r\n");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Delete log file
     *
     * @param context
     * @return
     */
    public synchronized static boolean delete(Context context) {
        String path = getLogFilePath(context);
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                return true;
            }
        }

        return false;
    }

    private static String getLogFilePath(Context context) {
        return getLogFilePath(context.getFilesDir().getAbsolutePath());
    }

    private static String getLogFilePath(String logDir) {
        return logDir + "/" + LOG_FILE_NAME;
    }
}
