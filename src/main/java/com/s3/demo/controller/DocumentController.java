package com.s3.demo.controller;

import com.s3.demo.service.DocumentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    //http://localhost:8080/api/upload-file
    @PostMapping("${app.endpoint.uploadFile}")
    public void uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        documentService.uploadFile(file);
    }

    //modify temp folder

    //http://localhost:8080/api/download-file?fileName=MOCK_DATA.csv
    @GetMapping("${app.endpoint.downloadFile}")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileName") String fileName) throws IOException {
        documentService.downloadFileFromS3bucket(fileName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //http://localhost:8080/api/generateZip-file?fileNames=MOCK_DATA.csv
    @GetMapping("${app.endpoint.generateZipFile}")
    public void generateZipFile(@RequestParam("fileNames") List<String> fileNames, final HttpServletResponse response) throws IOException {
        documentService.downloadMultipleFileFromS3bucket(fileNames, response);

    }

    //http://localhost:8080/api/delete-file?fileNames=MOCK_DATA.csv
    @DeleteMapping("${app.endpoint.deleteFile}")
    public void deleteFile(@RequestParam("fileNames") List<String> fileNames) throws IOException {
        documentService.deleteFileFromS3bucket(fileNames);

    }

}