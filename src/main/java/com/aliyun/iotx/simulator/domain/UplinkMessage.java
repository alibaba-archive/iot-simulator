package com.aliyun.iotx.simulator.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanliang.hl
 * @date 2018-12-25 2:12 PM
 **/
public class UplinkMessage {
    protected String method = "thing.event.property.post";

    protected String id = System.currentTimeMillis() + "";

    protected String version = "1.0.0";

    protected Map<String, Object> params = new HashMap<>();

    public UplinkMessage put(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public String getMethod() {
        return method;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
