package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.minio.MinioProperties;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;
    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //todo 文件名唯一
        String filename =
                new SimpleDateFormat("yyyyMMdd")//todo /前面是一级路径
                        .format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        boolean bucketExists =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        if(!bucketExists){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .config(createBucketPolicyConfig(minioProperties.getBucketName()))
                    .build());
        }
        minioClient.putObject(PutObjectArgs.builder()//todo upload data from a stream to an object
                .bucket(minioProperties.getBucketName())
                .stream(file.getInputStream(),file.getSize(),-1)//todo 流 文件大小 分块大小
                .object(filename)
                .contentType(file.getContentType())//todo 设置文件访问方式 下载图片或者展示图片 getContentType实现了以前是什么类型就用什么类型访问
                .build());
        //todo 三者之间用/分割
        return String.join("/", minioProperties.getEndpoint(), minioProperties.getBucketName(), filename);
    }
    private String createBucketPolicyConfig(String bucketName) {

        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }
}
