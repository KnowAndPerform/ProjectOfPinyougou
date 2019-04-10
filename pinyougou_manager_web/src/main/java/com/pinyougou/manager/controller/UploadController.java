package com.pinyougou.manager.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.FastDFSClient;

/**
 * 实现文件上传功能,返回上传成功后的url 以便访问:
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    //MultipartFile  文件上传的API
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file){
        //用工具类实现文件上传功能:
        try {
            //将配置文件路径传过去,里边有Tracker追踪服务器的地址和端口口号
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //通过页面传来的文件获取文件内容:
            byte[] fileContext = file.getBytes();
            //获取文件的扩展名,从源文件的最后一个点之后一位开始截取字符串:
            String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            //通过文件内容上传,返回服务器所在组数和文件存储的路径:
            // group1/M00/00/00/wKgZhVyQ3ceAH7PqAAF80JLXpt4149.jpg
            String path = fastDFSClient.uploadFile(fileContext, extName);
            //http://192.168.25.133/group1/M00/00/00/wKgZhVyQ3ceAH7PqAAF80JLXpt4149.jpg
            //通过url可以访问到图片
            String url = FILE_SERVER_URL+path;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"文件上传失败!");
        }
    }
}
