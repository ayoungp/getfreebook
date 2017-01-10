package com.peaceb.getfreebook;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

class Setting {
    private static final String TAG = "Setting";
    private static final String PREF_NAME = "getfreebook_pref";

    public static final String KEY_EMAIL = "pref_key_email";
    public static final String KEY_PASS = "pref_key_pass";
    public static final String KEY_LAST_BOOK_TITLE = "pref_key_last_book_title";
    public static final String KEY_USE_NOTIFY_NEWBOOK = "pref_key_use_notify_newbook";
    public static final String KEY_USE_AUTO_CLAIM = "pref_key_use_auto_claim";
    public static final String KEY_CHECK_FIRST = "pref_key_check_first";
    public static final String KEY_USE_VIBRATE = "pref_key_use_vibrate";
    public static final String KEY_USE_SOUND = "pref_key_use_sound";
    public static final String KEY_USE_LOG = "pref_key_use_log";

    public static String get(Context context, String key) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String val = prefs.getString(key, "");

        return val;
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean val = prefs.getBoolean(key, false);

        return val;
    }

    public static void set(Context context, String key, String val) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static void set(Context context, String key, boolean val) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public static void remove(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    public static String encrypt(Context context, String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] key = md.digest(Util.getMacAddress(context).getBytes());
            key = Arrays.copyOf(key, 16);
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(plain.getBytes());
            String encrypyedStr = Base64.encodeToString(encrypted, Base64.NO_WRAP);

            return encrypyedStr;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return plain;
    }

    public static String decrypt(Context context, String encryptedStr) {
        if (TextUtils.isEmpty(encryptedStr)) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] key = md.digest(Util.getMacAddress(context).getBytes());
            key = Arrays.copyOf(key, 16);
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");

            byte[] encrypted = Base64.decode(encryptedStr, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] plain = cipher.doFinal(encrypted);

            return new String(plain);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encryptedStr;
    }

    public static void startCheckNewBook(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = createServiceIntent(context);

        long triggerTime = System.currentTimeMillis() + 60000; // start after 1 min.
        long intervalTime = AlarmManager.INTERVAL_HOUR * 2; // interval 2 hours
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalTime, pendingIntent);

        Log.i(TAG, "job scheduled! trigger:" + triggerTime + " interval:" + intervalTime);
    }

    public static void stopCheckNewBook(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = createServiceIntent(context);

        am.cancel(pendingIntent);

        Log.i(TAG, "job canceled!");
    }

    private static PendingIntent createServiceIntent(Context context) {
        Intent intent = new Intent(context, WorkerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        return pendingIntent;
    }
}

