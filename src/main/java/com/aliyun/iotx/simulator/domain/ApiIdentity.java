package com.aliyun.iotx.simulator.domain;

/**
 * @author hanliang.hl
 * @date 2018-12-25 1:02 PM
 **/
public class ApiIdentity {
    private String accessKey;
    private String accessSecret;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }
}
