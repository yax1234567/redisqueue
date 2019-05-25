package com.yax.redisqueue.util;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class OkHttpClient implements HttpClient{

    private static final Logger log = LoggerFactory.getLogger(OkHttpClient.class);

    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    private static okhttp3.OkHttpClient okHttpClient =
            new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .build();

    public  String post(String url,Object obj){
        RequestBody requestBody = RequestBody.create(JSON, JSONObject.toJSONString(obj));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200){
                return response.body().string();
            }else {
                return null;
            }
        } catch (Exception e) {
            log.error("网络异常1:" + e.getMessage());
            return null;
        }
    }
}
