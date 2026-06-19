package com.example.demo.text;

/*
引入依赖包
<!-- https://mvnrepository.com/artifact/com.aliyun/videorecog20200320 -->
<dependency>
      <groupId>com.aliyun</groupId>
      <artifactId>videorecog20200320</artifactId>
      <version>${aliyun.videorecog.version}</version>
</dependency>
*/

import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaModel;
import com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListResponse;

public class RecognizeVideoCastCrewList {

    public static com.aliyun.videorecog20200320.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        /*
          初始化配置对象com.aliyun.teaopenapi.models.Config
          Config对象存放 AccessKeyId、AccessKeySecret、endpoint等配置
         */
         com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "videorecog.cn-shanghai.aliyuncs.com";
        return new com.aliyun.videorecog20200320.Client(config);
    }

    public static void main(String[] args) throws Exception {
        // 创建AccessKey ID和AccessKey Secret，请参见：https://help.aliyun.com/document_detail/175144.html
        // 如果您使用的是RAM用户的AccessKey，还需要为子账号授予权限AliyunVIAPIFullAccess，请参见：https://help.aliyun.com/document_detail/145025.html
        // 从环境变量读取配置的AccessKey ID和AccessKey Secret。运行代码示例前必须先配置环境变量。
        String accessKeyId = "abc";//id
        String accessKeySecret = "abc"; //secret
        com.aliyun.videorecog20200320.Client client = RecognizeVideoCastCrewList.createClient(accessKeyId, accessKeySecret);
        com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListRequest recognizeVideoCastCrewListRequest = new com.aliyun.videorecog20200320.models.RecognizeVideoCastCrewListRequest()
                .setVideoUrl("http://viapi-test.oss-cn-shanghai.aliyuncs.com/viapi-3.0domepic/videorecog/videorecog/videorecog2.mp4");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            RecognizeVideoCastCrewListResponse response = client.recognizeVideoCastCrewListWithOptions(recognizeVideoCastCrewListRequest, runtime);
            System.out.println(TeaModel.toMap(response));
        } catch (TeaException error) {
            // 获取整体报错信息
            System.out.println("Error: " + error.getMessage());
            // 获取单个字段
            System.out.println(error.getCode());
        }
    }
}