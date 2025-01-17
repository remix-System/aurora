package com.aurora.strategy.impl;

import com.aurora.exception.BizException;
import com.aurora.strategy.UploadStrategy;
import com.aurora.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
//    TODO 文件上传的抽象类
@Service
public abstract class AbstractUploadStrategyImpl implements UploadStrategy {

//    TODO 传入文件以及文件路径执行上传
    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            String md5 = FileUtil.getMd5(file.getInputStream());
            String extName = FileUtil.getExtName(file.getOriginalFilename());
            String fileName = md5 + extName;
            if (!exists(path + fileName)) {
                upload(path, fileName, file.getInputStream());
            }
            return getFileAccessUrl(path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("文件上传失败");
        }
    }

    @Override
    public String uploadFile(String fileName, InputStream inputStream, String path) {
        try {
            upload(path, fileName, inputStream);
            return getFileAccessUrl(path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("文件上传失败");
        }
    }
//    TODO 以下三个类交给继承的类重写
    public abstract Boolean exists(String filePath);

    public abstract void upload(String path, String fileName, InputStream inputStream) throws IOException;

    public abstract String getFileAccessUrl(String filePath);

}
