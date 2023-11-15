package com.cyan.springcloud.media;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-06-16
 */
@SpringBootTest
public class BigFileTest {

    /**
     * 文件分块测试
     *
     * @throws Exception
     */
    @Test
    public void testChunk() throws Exception {
        // 源文件
        File sourceFile = new File("D:\\data\\upload\\测试文件.mp4");
        // 分块文件存储路径
        String chunkFilePath = "D:\\data\\upload\\chunk\\";
        // 分块文件大小
        int chunkSize = 1024 * 1024 * 5; // 5M
        // 分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读取数据，向分块文件中读写数据
        RandomAccessFile file_r = new RandomAccessFile(sourceFile, "r");
        // 缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunckFile = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile file_rw = new RandomAccessFile(chunckFile, "rw");
            int len = -1;
            while ((len = file_r.read(bytes)) != -1) {
                file_rw.write(bytes, 0, len);
                if (chunckFile.length() >= chunkSize) {
                    break;
                }
            }
            file_rw.close();
        }
        file_r.close();
    }

    /**
     * 文件合并测试
     *
     * @throws Exception
     */
    @Test
    public void testMerge() throws Exception{
        // 分块文件目录
        File chunkFolder = new File("D:\\data\\upload\\chunk");
        // 源文件
        File sourceFile = new File("D:\\data\\upload\\测试文件.MP4");
        // 合并后的文件
        File mergeFile = new File("D:\\data\\upload\\合并后的文件.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        List<File> filesList = Arrays.asList(files);
        // 对分块文件进行排序
        Collections.sort(filesList, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));

        RandomAccessFile file_rw = new RandomAccessFile(mergeFile, "rw");

        byte[] bytes = new byte[1024];

        for (File file : filesList) {
            RandomAccessFile file_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = file_r.read(bytes)) != -1) {
                file_rw.write(bytes, 0, len);
            }
            file_r.close();
        }
        file_rw.close();

        // 校验
        String merge = DigestUtils.md5Hex(new FileInputStream(mergeFile));
        String source = DigestUtils.md5Hex(new FileInputStream(sourceFile));

        if (merge.equals(source)) {
            System.out.println("merge success");
        }

    }
}
