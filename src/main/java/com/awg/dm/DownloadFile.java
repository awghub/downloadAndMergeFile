package com.awg.dm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 下载分部文件
 *
 * @author awg
 * @version 1.0
 */
public class DownloadFile {

    private static Properties prop = null;

    public static void main(String[] args){

        // 加载配置文件
        prop = new Properties();
        try {
            prop.load(DownloadFile.class.getClassLoader().getResourceAsStream("downloadAndMergeFile.properties"));
        } catch (IOException e) {
            System.out.println("配置文件加载失败: " + e.getMessage());
            return;
        }

        File dir = new File(prop.getProperty("download_localSaveDir"));
        if (!dir.exists()){
            System.out.println(dir.mkdirs() ? "本地目录创建成功" : "本地目录创建失败");
        }

        // 读取配置文件
        String fileSuf = prop.getProperty("download_fileSuf");
        String filePre = prop.getProperty("download_filePre");
        String URLPre = prop.getProperty("download_URLPre") + filePre;
        int fileIndexMin = Integer.parseInt(prop.getProperty("download_fileIndexMin", "0"));
        int fileIndexMax = Integer.parseInt(prop.getProperty("download_fileIndexMax", "0"));
        int fileIndexFormatLength = prop.getProperty("download_fileIndexFormat").length();
        int FileIndexFormatLengthMin = (int)Math.pow(10, fileIndexFormatLength);
        StringBuilder fileNameIndexPre;

        for (int i = fileIndexMin; i <= fileIndexMax; i++) {
            fileNameIndexPre = new StringBuilder();
            if (i < FileIndexFormatLengthMin){
                for (int j = fileIndexFormatLength-String.valueOf(i).length(); j > 0; j--) {
                    fileNameIndexPre.append("0");
                }
            }
            fileNameIndexPre.append(i);
            //下载
            // 文件的本地存放路径对应的File对象
            File localFile = new File(dir, filePre+fileNameIndexPre.toString()+fileSuf);
            try {
                downloadFile(URLPre+fileNameIndexPre.toString()+fileSuf, localFile);
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            fileNameIndexPre.delete(0,fileNameIndexPre.length());
        }
    }


    /**
     * 从网络上下载文件
     *
     * @param urlStr 代表URL地址的字符串
     * @param localFile 将从网络上下载的文件存放到本地的文件
     * @throws IOException IO异常
     */
    public static void downloadFile(String urlStr, File localFile) throws IOException {
        System.out.println(formatNow() + " - Downloading[" + urlStr + "]");
        if (!localFile.exists()) {
            // 文件不存在，则创建
            System.out.println("目标文件创建: " + (localFile.createNewFile() ? "成功" : "失败"));
        } else {
            System.out.println("目标文件已存在, 删除旧文件: " + (localFile.delete() ? "成功" : "失败"));
            downloadFile(urlStr, localFile);
        }
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-agent", prop.getProperty("download_requestUerAgent"));
        connection.setRequestMethod(prop.getProperty("download_requestMethod"));
        connection.setConnectTimeout(Integer.parseInt(prop.getProperty("download_connectTimeout", "5000")));
        connection.setReadTimeout(Integer.parseInt(prop.getProperty("download_readTimeout", "5000")));
        InputStream is = connection.getInputStream();
        FileOutputStream fos = new FileOutputStream(localFile);
        byte[] cs = new byte[2048];
        int size;
        while ((size=is.read(cs, 0, cs.length)) != -1) {
            fos.write(cs, 0, size);
        }
        is.close();
        fos.close();
        is.close();
        connection.disconnect();
    }

    /**
     * 当前时间的格式化显示
     *
     * @return 当前时间的格式化后的字符串表示
     */
    private static String formatNow(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
