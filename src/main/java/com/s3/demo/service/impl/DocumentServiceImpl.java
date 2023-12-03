package com.s3.demo.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.s3.demo.config.ApplicationProperties;
import com.s3.demo.service.DocumentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    private ApplicationProperties applicationProperties;

    private final String path = "/Documents/tmp/";

    @Override
    public void uploadFile(MultipartFile file) throws IOException {
        // String uniqueFileName = generateFileName(file);
        String filename = file.getOriginalFilename();
        File inputFile = convertMultiPartFileToFile(file);
        uploadFileToS3bucket(filename, inputFile, applicationProperties.getAwsServices().getBucketName());
    }

    private void uploadFileToS3bucket(String fileName, File file, String bucketName) {
        amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, file));

    }

    @Override
    public void downloadFileFromS3bucket(String fileName) throws IOException {
        S3Object object = amazonS3Client.getObject(applicationProperties.getAwsServices().getBucketName(), fileName);
        S3ObjectInputStream objectInputStream = object.getObjectContent();

        try (FileOutputStream fos = new FileOutputStream(path + fileName)) {
            fos.write(IOUtils.toByteArray(objectInputStream));
        }
    }


    @Override
    public void downloadMultipleFileFromS3bucket(List<String> fileNames, HttpServletResponse response) throws IOException {
        List<S3ObjectInputStream> fileObjects = new ArrayList<>();
        fileNames.forEach(fileName -> {
            S3Object object = amazonS3Client.getObject(applicationProperties.getAwsServices().getBucketName(), fileName);

            try (FileOutputStream fos = new FileOutputStream(path + fileName)) {
                fos.write(IOUtils.toByteArray(object.getObjectContent()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=compressed.zip");

        // final FileOutputStream fos = new FileOutputStream(path + "/compressed.zip");
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        byte[] bytes = new byte[1024];

        for (String srcFile : fileNames) {
            File fileToZip = new File(path + srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            fileToZip.delete();
        }

        zipOut.close();
        // fos.close();
        //return bytes;
    }

    @Override
    public void deleteFileFromS3bucket(List<String> fileNames) throws IOException {
        fileNames.forEach(fileName -> {
            amazonS3Client.deleteObject(applicationProperties.getAwsServices().getBucketName(), fileName);
        });
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }
}
