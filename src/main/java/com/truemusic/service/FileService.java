package com.truemusic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${rustfs.bucket-name}")
    private String bucketName;

    // 上传文件
    public String uploadFile(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) return null;
        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String key = dir + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName).key(key).contentType(file.getContentType()).build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            throw new RuntimeException("文件上传 RustFS 失败", e);
        }
    }

    // 生成预签名 URL
    public String getPresignedUrl(String key) {
        if (key == null || key.isEmpty()) return null;
        GetObjectRequest req = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(6)) // 6小时有效
                .getObjectRequest(req).build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}