package com.cyan.springcloud.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import sun.nio.ch.IOUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Minio
 *
 * @author Cyan Chau
 * @create 2023-06-15
 */
@SpringBootTest
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void testUpload() throws Exception {

        // 通过扩展名得到媒资类型mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".png");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        // 上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("miniotest")
                .filename("D:\\img\\taotao.png") // 本地文件路径
                .object("taotao.png") // 对象
                .contentType(mimeType)
                .build();

        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test
    public void testDelete() throws Exception {

        // 删除文件的参数信息
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("miniotest")
                .object("taotao.png")
                .build();

        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    public void testGetFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("miniotest").object("/taotao.png").build();

        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream outputStream = new FileOutputStream("D:\\img\\taotao_new.png");
        IOUtils.copy(inputStream, outputStream);

        // 校验
        String sourceMd5 = DigestUtils.md5Hex(inputStream);
        String localMd5 = DigestUtils.md5Hex(new FileInputStream("D:\\img\\taotao.png"));
        if (sourceMd5.equals(localMd5)) {
            System.out.println("Download Success");
        }
    }

    @Test
    public void testUploadChunk() throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {

        for (int i = 0; i < 6; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("miniotest")
                    .filename("D:\\data\\upload\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功");
        }
    }

    @Test
    public void testMerge() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {

//        List<ComposeSource> sources = new ArrayList<>();
//        // 指定分块文件的信息
//        for (int i = 0; i < 9; i++) {
//            ComposeSource composeSource = ComposeSource.builder()
//                    .bucket("miniotest")
//                    .object("chunk/" + i)
//                    .build();
//            sources.add(composeSource);
//        }
        // 分块文件信息
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(6)
                .map(i -> ComposeSource.builder().bucket("miniotest").object("chunk/" + i).build())
                .collect(Collectors.toList());

        // 参数信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("miniotest")
                .object("merge.mp4")
                .sources(sources) // 指定源文件
                .build();

        // 合并文件 minio 分块默认5M
        minioClient.composeObject(composeObjectArgs);
    }

}
