package com.aliyun.iotx.simulator.util;

import java.io.*;

/**
 * @author hanliang.hl
 * @date 2018-12-25 12:58 PM
 **/
public class FileUtil {
    public static String readFile(String path) {
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        try {
            File filename = new File(path);
            reader = new InputStreamReader(new FileInputStream(filename));
            bufReader = new BufferedReader(reader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                sb.append(line);

            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufReader != null){
                    bufReader.close();
                }
                if (bufReader != null){
                    bufReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void writeFile(String path, String content, boolean append) {
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            writer = new FileWriter(path, append);
            writer.append(content);
            writer.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(writer != null){
               try {
                   writer.close();
               } catch (Exception ignore) {}
            }
        }
    }
}
