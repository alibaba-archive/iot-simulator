package com.aliyun.iotx.simulator.domain;

/**
 * @author hanliang.hl
 * @date 2018-12-25 1:02 PM
 **/
public class Device {
    private String productKey;
    private String deviceName;
    private String deviceSecret;

    public Device(String productKey, String deviceName, String deviceSecret) {
        this.productKey = productKey;
        this.deviceName = deviceName;
        this.deviceSecret = deviceSecret;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }
}
