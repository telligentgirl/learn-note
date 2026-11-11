package com.example.learn_note.controller;

import com.example.learn_note.pojo.Result;
import com.example.learn_note.utils.AliyunOSSUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {
    @PostMapping
    public Result<String> upload(MultipartFile file) throws Exception {
        //把文件的内容存储到本地磁盘
        String originalFilename = file.getOriginalFilename();
        //保证文件的名字是唯一的，防止被覆盖
        String filename= UUID.randomUUID().toString()+originalFilename.substring(originalFilename.lastIndexOf("."));
//        存入本地磁盘
//        file.transferTo(new File("E:\\java_project\\big_event\\files\\"+filename));
        String uploadfileurl = AliyunOSSUtil.uploadfile(filename, file.getInputStream());
        return Result.success(uploadfileurl);
    }
}
