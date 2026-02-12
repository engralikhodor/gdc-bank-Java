package com.alikhdr.bankingApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service
{
    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    public String uploadBillReceipt(Long billId, String content)
    {
        String key = "receipts/bill_" + billId + ".txt";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("text/plain")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(content));

        // public URL of the receipt
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

}
