package com.s3.demo.service;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface DocumentService {
    void uploadFile(MultipartFile file) throws IOException;

    void downloadFileFromS3bucket(String fileName) throws IOException;

    void downloadMultipleFileFromS3bucket(List<String> fileNames, HttpServletResponse response) throws IOException;

    void deleteFileFromS3bucket(List<String> fileNames) throws IOException;

}
