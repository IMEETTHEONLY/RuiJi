package com.itheima.test;

import org.junit.jupiter.api.Test;

public class UploadFileTest {
    //验证文件后缀截取是否正确
    @Test
    public void  test1(){
        String fileName="ereawe.jpg";

        String suffix=fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);

    }
}
