package com.example.demo.text;

/*
引入依赖包
最低SDK版本要求：viapi20230117的SDK版本需大于等于2.0.1。
可以在此仓库地址中引用最新版本SDK：https://mvnrepository.com/artifact/com.aliyun/viapi20230117
<!-- https://mvnrepository.com/artifact/com.aliyun/viapi20230117 -->
<dependency>
      <groupId>com.aliyun</groupId>
      <artifactId>viapi20230117</artifactId>
      <version>${aliyun.viapi.version}</version>
</dependency>
*/

import com.aliyun.tea.TeaModel;
import com.aliyun.viapi20230117.models.GetAsyncJobResultResponse;

public class GetAsyncJobResult {
    public static com.aliyun.viapi20230117.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        /*
          初始化配置对象com.aliyun.teaopenapi.models.Config
          Config对象存放 AccessKeyId、AccessKeySecret、endpoint等配置
         */
         com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "viapi.cn-shanghai.aliyuncs.com";
        return new com.aliyun.viapi20230117.Client(config);
    }

    public static void main(String[] args_) throws Exception {
        // 创建AccessKey ID和AccessKey Secret，请参考https://help.aliyun.com/document_detail/175144.html
        // 如果您使用的是RAM用户的AccessKey，还需要为子账号授予权限AliyunVIAPIFullAccess，请参考https://help.aliyun.com/document_detail/145025.html
        // 从环境变量读取配置的AccessKey ID和AccessKey Secret。运行代码示例前必须先配置环境变量。
        String accessKeyId = "abc";//id
        String accessKeySecret = "abc"; //secret
        com.aliyun.viapi20230117.Client client = GetAsyncJobResult.createClient(accessKeyId, accessKeySecret);
        com.aliyun.viapi20230117.models.GetAsyncJobResultRequest getAsyncJobResultRequest = new com.aliyun.viapi20230117.models.GetAsyncJobResultRequest()
                .setJobId("F4897407-47FF-5337-9E83-86238C568607");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            GetAsyncJobResultResponse getAsyncJobResultResponse = client.getAsyncJobResultWithOptions(getAsyncJobResultRequest, runtime);
            // 获取整体结果。
            System.out.println(TeaModel.toMap(getAsyncJobResultResponse));
            // 获取单个字段。
            System.out.println(getAsyncJobResultResponse.getBody());
        } catch (com.aliyun.tea.TeaException teaException) {
            // 获取整体报错信息。
            System.out.println("Error: " + teaException.getMessage());
            // 获取单个字段。
            System.out.println(teaException.getCode());
        }
    }
}
