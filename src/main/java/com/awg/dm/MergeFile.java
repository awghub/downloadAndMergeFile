package com.awg.dm;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * 合并分部文件
 *
 * @author awg
 * @version 1.0
 */
public class MergeFile {

    private static Properties prop = null;

    public static void main(String[] args) throws IOException {

        // 加载配置文件
        prop = new Properties();
        try {
            prop.load(MergeFile.class.getClassLoader().getResourceAsStream("downloadAndMergeFile.properties"));
        } catch (IOException e) {
            System.out.println("配置文件加载失败: " + e.getMessage());
            return;
        }

        // 定义合并后文件的存储路径
        String path = prop.getProperty("merge_localSaveDir");
        // 创建所有的指定后缀名的文件的存放的目录所对应的File对象
        File dir = new File(path);
        // 获取所有的指定后缀名的文件
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(prop.getProperty("merge_fileSuf")));
        if (files == null || files.length < 1){
            System.out.println("在指定的目录中未找到指定后缀名的文件");
            return;
        }
        // 按文件名(前的序号)进行自然排序
        Arrays.sort(files);
        // 将各个文件使用InputStream进行读取并存放在向量之中
        Vector<InputStream> inputStreamVector = new Vector<>();
        for (File f:files) {
            inputStreamVector.add(new FileInputStream(f));
        }
        // 将存放读取之后的字节流向量转为枚举
        Enumeration<InputStream> enumeration = inputStreamVector.elements();
        // 创建合并流对象
        SequenceInputStream sequenceInputStream = new SequenceInputStream(enumeration);
        // 创建合并后的文件的File对象
        File file = new File(path, prop.getProperty("merge_fileName"));
        if (!file.exists()){
            System.out.println("目标文件创建: " + (file.createNewFile() ? "成功" : "失败"));
        }
        // 通过文件输出流写出合并文件
        FileOutputStream fos = new FileOutputStream(file);
        // 定义缓冲区
        byte[] bys = new byte[2048];
        int len;
        while ((len=sequenceInputStream.read(bys)) != -1){
            fos.write(bys,0,len);
        }
        // 关闭流,释放资源
        fos.close();
        sequenceInputStream.close();

        System.out.println("分部文件合并完成");
    }
}
