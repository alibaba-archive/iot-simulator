package com.aliyun.iotx.api.client;

import com.alibaba.cloudapi.sdk.client.ApacheHttpClient;
import com.alibaba.cloudapi.sdk.enums.HttpMethod;
import com.alibaba.cloudapi.sdk.enums.Scheme;
import com.alibaba.cloudapi.sdk.model.ApiRequest;
import com.alibaba.cloudapi.sdk.model.ApiResponse;
import com.alibaba.cloudapi.sdk.model.HttpClientBuilderParams;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import static com.alibaba.cloudapi.sdk.enums.HttpConnectionModel.MULTIPLE_CONNECTION;

/**
 * @author hanliang.hl
 */
public final class SyncApiClient extends ApacheHttpClient {
    private String appKey;
    private String appSecret;

    private SyncApiClient(Builder builder) {
        this.appKey = builder.appKey;
        this.appSecret = builder.appSecret;

        HttpClientBuilderParams builderParams = new HttpClientBuilderParams();
        builderParams.setAppKey(this.appKey);
        builderParams.setAppSecret(this.appSecret);
        super.init(builderParams);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public ApiResponse postBody(String host, String path, IoTApiRequest request, boolean isHttps,
                                Map<String, String> headers)
        throws UnsupportedEncodingException {
        byte[] body = JSONObject.toJSONString(request).getBytes("UTF-8");
        ApiRequest apiRequest = new ApiRequest(HttpMethod.POST_BODY, path,
            body);
        apiRequest.setHttpConnectionMode(MULTIPLE_CONNECTION);
        apiRequest.setScheme(isHttps ? Scheme.HTTPS : Scheme.HTTP);
        apiRequest.setHost(host);
        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                apiRequest.getHeaders().put(header.getKey(), Collections.singletonList(header.getValue()));
            }
        }
        return sendSyncRequest(apiRequest);
    }

    public ApiResponse postBody(String host, String path, IoTApiRequest request, boolean isHttps)
        throws UnsupportedEncodingException {

        return postBody(host, path, request, isHttps, null);
    }

    public ApiResponse postBody(String host, String path, IoTApiRequest request)
        throws UnsupportedEncodingException {
        return postBody(host, path, request, false, null);
    }

    public static class Builder {
        private String appKey;
        private String appSecret;

        public Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder appSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public SyncApiClient build() {
            return new SyncApiClient(this);
        }
    }

}
