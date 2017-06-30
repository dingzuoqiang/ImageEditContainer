package com.dzq.imageeditcontainer.utils;


import android.os.Environment;

import java.io.File;

/**
 * Created by dingzuoqiang on 2017/6/20.
 * Email: 530858106@qq.com
 */
public class FilePathUtils {
    public static String savePath() {
        return rootSavePath;
    }

    //  文件存储根目录
    private static final String rootSavePath = Environment.getExternalStorageDirectory() + File.separator
            + "dzq" + File.separator + "imageeditcontainer" + File.separator;

    //  图片文件存储路径
    private static final String imageSavePath = rootSavePath + "image" + File.separator;
    //  图片文件存储路径


    //  更新包文件存储路径
    private static final String updateSavePath = rootSavePath + "update" + File.separator;

    //  错误日志文件存储路径
    private static final String crashLogPath = rootSavePath + "log" + File.separator;

    //
    private static final String matHprofPath = rootSavePath + "hprof" + File.separator;

    public static String getRootSavePath() {
        return rootSavePath;
    }

    public static String getImageSavePath() {
        return imageSavePath;
    }

    public static String getUpdateSavePath() {
        return updateSavePath;
    }

    public static String getCrashLogPath() {
        return crashLogPath;
    }

    public static String getMatHprofPath() {
        return matHprofPath;
    }

    public static boolean deleteDirectory(String dir, boolean deleteDir) {
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                flag = deleteFile(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                flag = deleteDirectory(file.getAbsolutePath(), true);
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            return false;
        }

        if (deleteDir) {
            if (dirFile.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean deleteFile(String strFullFileName) {
        if (null == strFullFileName || 0 >= strFullFileName.length())
            return false;

        File file = new File(strFullFileName);
        return file.isFile() && file.delete();
    }

    public static void deleteFileAtDirByExt(String dir, String ext) {
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return;
        }

        File[] files = dirFile.listFiles();
        if (null != files) {
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                if (file.isFile()
                        && (filePath.endsWith(ext.toLowerCase())
                        || filePath.endsWith(ext.toUpperCase()))) {

                    deleteFile(filePath);
                }
            }
        }
    }

    public static final String videoSavePath() {
        File file = new File(rootSavePath + "video/");
        if (!file.exists()) {
            file.mkdirs();
        }
        return rootSavePath + "video/";
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        //%.2f 即是保留两位小数的浮点数，后面跟上对应单位就可以了，不得不说java很方便
        if (size >= gb) {
            return String.format("%.2f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            //如果大于100MB就不用保留小数位啦
            return String.format(f > 100 ? "%.0f MB" : "%.2f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            //如果大于100kB就不用保留小数位了
            return String.format(f > 100 ? "%.0f KB" : "%.2f KB", f);
        } else
            return String.format("%d B", size);
    }
}

