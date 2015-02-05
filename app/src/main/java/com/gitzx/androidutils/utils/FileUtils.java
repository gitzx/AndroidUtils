package com.gitzx.androidutils.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by zhangxiang on 2015/2/5.
 */
public class FileUtils {

    //创建文件
    public static void creatFile(String filePath) {
        File filename = new File(filePath);
        if (!filename.exists()) {
            try {
                filename.createNewFile();
            } catch (IOException e) {
            }
        }
    }

    //读取文件
    public static String readFile(File file){
        BufferedReader reader=null;
        StringBuffer sb=new StringBuffer();
        try{
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while((tempString = reader.readLine())!=null){
                sb.append(tempString + "/r/n");
            }
            reader.close();
            return sb.toString();
        }catch (IOException e){
            e.printStackTrace();;
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return "";
    }

    //写文件
    public static void writeFile(String fileName, String content) {
        File file = new File(fileName);
        BufferedWriter writer = null;
        try {
            // 文件格式为utf-8
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //删除文件
    public static void deleteFile(File file) {
        try {
            if (file.exists()) {
                if (file.isFile()) {
                    boolean de = file.delete();
                } else if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFile(files[i]);
                    }
                }
                file.delete();
            } else {
            }
        } catch (Exception e) {
        }
        return;
    }
}
