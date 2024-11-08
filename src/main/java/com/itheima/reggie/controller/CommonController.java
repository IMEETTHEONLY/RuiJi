package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/*
* 文件上传和下载*/
@RestController
@RequestMapping("common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;


    //文件上传
    @PostMapping("/upload")       //这个MultipartFile的名字一定要根前端from表单的名字一样
    public R<String> upload(MultipartFile file){
//        file是一个临时文件，需要转存到指定位置，不然本次请求完成后临时文件会删除
        log.info(file.toString());

        //将临时为文件转存本地文件指定位置

        //获取原始文件名
        String originFilename=file.getOriginalFilename();

        //获取文件后缀
        String suffix=originFilename.substring(originFilename.lastIndexOf("."));


        //使用uuid重新生成文件名，防止文件名重复造成文件覆盖
        String fileName= UUID.randomUUID().toString()+suffix;


        //文件转存核心代码

        //创建一个目录对象
        File dir=new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在，需要创建
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //返回文件名称
        return R.success(fileName);
    }


    //文件下载  不需要返回值，而是通过流response的方法将照片传递给前端
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //输入流是自己new的inputstram，而输出流是response的getOutputStream
              //输入流，读取文件内容
        try {
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));


            //输出流，将文件写回浏览器，在浏览器展示图片
           ServletOutputStream outputStream= response.getOutputStream();

           //设置响应文件类型为图片
            response.setContentType("image/jpeg");

            //从输入流中读出内容
           int len=0;
           byte[] bytes=new byte[1024];
          while ((len= fileInputStream.read(bytes))!=-1){
              outputStream.write(bytes,0,len);
              outputStream.flush();
          }
          //关闭资源
          outputStream.close();;
          fileInputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
