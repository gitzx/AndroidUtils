package com.gitzx.androidutils.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by zhangxiang on 2015/2/6.
 */
public class DeviceUtils {

    private static final String LOG_TAG = DeviceUtils.class.getSimpleName();
    private static final String SYS_PROP_MOD_VERSION = "ro.modversion";
    private static final String CPU_LIST_PATH = "/sys/devices/system/cpu/";
    private static final String ISERIAL = "/sys/class/android_usb/android0/iSerial";
    private static final String CPU_INFO = "/proc/cpuinfo";
    private static final String MEM_INFO = "/proc/meminfo";
    private static final String BUILD_PROP = "/system/build.prop";
    private static final String DALVIK_HEAP_LIMIT = "dalvik.vm.heapgrowthlimit";

    //获取设备信息
    public static JSONObject getDeviceInfo(Context context) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("osversion", "Android" + Build.VERSION.RELEASE);
        json.put("sdkversion", Build.VERSION.SDK_INT);
        json.put("model", Build.MODEL);
        json.put("manufacturer", Build.MANUFACTURER);
        json.put("deviceid", getDeviceId(context));
        json.put("imei", getIMEI(context));
        json.put("cpucorenum", getNumCpuCores());
        json.put("cputype", getCPUInfo());
        json.put("resolution", getResolutionRatio(context));
        json.put("romversion", getModVersion());
        json.put("maxmem", getMaxMemoryPVm(context));
        json.put("totalmem", getTotalMemory());
        json.put("maxvmsize", getMaxVMSize());
        json.put("widthdp", getWidthDp(context));
        json.put("heightdp", getHeightDp(context));
        json.put("densitydp", getDensityDp(context));
        json.put("density", getDensity(context));
        json.put("smallestdp", getSmallestDp(context));
        json.put("customrom", getCustomROM());
        return json;
    }

    //获取设备IMEI号
    public static String getIMEI(Context context) {
        String imei = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
        return imei;
    }

    //获取安卓设备ID
    public static String getDeviceId(Context context) {
        String serialNum = null;
        try {
            FileReader localFileReader = new FileReader(ISERIAL);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            serialNum = localBufferedReader.readLine().trim();
            localBufferedReader.close();
        } catch (Exception e) {
            Log.d(LOG_TAG, "can not find this file");
            return "";
        }
        return serialNum;
    }

    //获取核心CPU数
    public static int getNumCpuCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            File dir = new File(CPU_LIST_PATH);
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    //获取CPU处理器架构
    public static String getCPUInfo() {
        String str2;
        String[] arrayOfString;
        StringBuilder sb = new StringBuilder();
        try {
            FileReader localFileReader = new FileReader(CPU_INFO);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                sb.append(arrayOfString[i] + " ");
            }
            localBufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

   //获取屏幕分辨率
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static String getResolutionRatio(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int[] ratio = { 0, 0 };
        ratio[0] = display.getHeight();
        ratio[1] = display.getWidth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            ratio[0] = size.y;
            ratio[1] = size.x;
        }
        return ratio[0] + "*" + ratio[1];
    }

   //获取ROM版本
    public static String getModVersion() {
        String modVersion = getSystemProperty(SYS_PROP_MOD_VERSION);
        if (modVersion == null || modVersion.length() == 0) {
            modVersion = getCustomROM();
        }
        return (modVersion == null || modVersion.length() == 0 ? "Unknown" : modVersion);
    }

    //获取每个应用程序最大可用内存(MB)
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int getMaxMemoryPVm(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int maxMemory = activityManager.getMemoryClass();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            maxMemory = activityManager.getLargeMemoryClass();
        }

        return maxMemory;
    }

    //获得系统总内存(KB)
    public static int getTotalMemory() {
        String str2;
        String[] arrayOfString;
        long initialMemory = 0;
        try {
            FileReader localFileReader = new FileReader(MEM_INFO);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            initialMemory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB
            localBufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int total = (int) (initialMemory / 1024);
        return total;
    }

    //获得单个虚拟机最大可用内存(MB)
    public static int getMaxVMSize() {
        // 单个应用程序最大可用内存,一个应用程序包含多个进程，一个进程对应一个dalvik虚拟机
        // String pattern = "dalvik.vm.heapsize";
        String temp;
        int maxSize = 0;

        try {
            FileReader fileReader = new FileReader(BUILD_PROP);
            BufferedReader reader = new BufferedReader(fileReader, 1024);
            while ((temp = reader.readLine()) != null) {
                if (temp.contains(DALVIK_HEAP_LIMIT)) {
                    break;
                }
            }
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(temp);
            maxSize = Integer.valueOf(m.replaceAll("").trim()).intValue();
            reader.close();
        } catch (Exception e) {
            Log.w(LOG_TAG, "getMaxVMSize exception");
        }
        return maxSize;
    }

    // 获取widthDp
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getWidthDp(Context context) {
        int widthDp = context.getResources().getDisplayMetrics().widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Configuration config = context.getResources().getConfiguration();
            widthDp = config.screenWidthDp;
        }
        return widthDp;
    }

    //获取heightDp
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getHeightDp(Context context) {
        int heightDp = context.getResources().getDisplayMetrics().heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Configuration config = context.getResources().getConfiguration();
            heightDp = config.screenHeightDp;
        }
        return heightDp;
    }

    //获取densityDp
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getDensityDp(Context context) {
        int densityDp = context.getResources().getDisplayMetrics().densityDpi;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            densityDp = config.densityDpi;
        }
        return densityDp;
    }

    //获取density
    public static float getDensity(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return density;
    }

    //获取smallestDp
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getSmallestDp(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        int smallestPixels = Math.min(display.widthPixels, display.heightPixels);
        int smallestDp = Math.round(smallestPixels / display.density);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Configuration config = context.getResources().getConfiguration();
            smallestDp = config.smallestScreenWidthDp;
        }
        return smallestDp;
    }

   //获取系统属性
    public static String getSystemProperty(String prop) {
        String line;
        BufferedReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("getprop " + prop);
            input = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    //获取自定义机型的ROM
    public static String getCustomROM() {
        String line;
        BufferedReader input = null;
        StringBuilder sb = new StringBuilder();
        String version = Build.VERSION.RELEASE;

        try {
            Process process = Runtime.getRuntime().exec("getprop ro.build.description");
            input = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"), 1024);
            line = input.readLine();
            if (line == null || line.length() == 0) {
                return null;
            }
            String[] s = line.split("\\s");
            for (int i = 0; i < s.length; i++) {
                if (!s[i].equals(version) && !s[i].contains("key")) {
                    sb.append(s[i] + " ");
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    //获取当前的网络类型
    public static int getNetworkType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (null == info) {
            return 0;
        } else {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return 1;
            } else if (info != null && type == ConnectivityManager.TYPE_MOBILE) {
                int mobileType = info.getSubtype();
                if (mobileType == TelephonyManager.NETWORK_TYPE_CDMA || mobileType == TelephonyManager.NETWORK_TYPE_GPRS
                        || mobileType == TelephonyManager.NETWORK_TYPE_EDGE) {
                    return 2;
                } else if (mobileType == TelephonyManager.NETWORK_TYPE_UMTS || mobileType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || mobileType == TelephonyManager.NETWORK_TYPE_EVDO_0 || mobileType == TelephonyManager.NETWORK_TYPE_HSPA) {
                    return 3;
                } else {
                    return 4;
                }
            }
        }
        return 0;
    }
}
