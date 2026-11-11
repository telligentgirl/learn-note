package com.example.learn_note.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.FileInputStream;
import java.io.InputStream;

public class AliyunOSSUtil {
//    这五个变量不太会改动，所以定义成静态常量
    private static final String ENDPOINT = LocalConfig.require("OSS_ENDPOINT");
    private static final String ACCESSID=LocalConfig.require("OSS_ACCESS_KEY_ID");
    private static final String ACCESSKEY=LocalConfig.require("OSS_ACCESS_KEY_SECRET");
    private static final String BUCKETNAME = LocalConfig.require("OSS_BUCKET");
    private static final String REGION = LocalConfig.require("OSS_REGION");

//    objectName与输入流才会频繁发生变化，所以以参数的形式暴露出去
//    图片上传成功后需要返回这个图片的url,所以返回类型是string
    public static String uploadfile(String objectName, InputStream inputStream) throws Exception {

        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
//        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        CredentialsProvider credentialsProvider =
                new DefaultCredentialProvider(ACCESSID, ACCESSKEY);


        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(REGION)
                .build();
//        用来接收最终结果
        String url="";
        try {

            // 创建PutObjectRequest对象。
//            第三个参数是输入流
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKETNAME, objectName,inputStream);

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

//            url组成；https://bucket名称.区域节点/objectname
            url="https://"+BUCKETNAME+"."+ENDPOINT.substring(ENDPOINT.lastIndexOf("/")+1)+"/"+objectName;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }
}
