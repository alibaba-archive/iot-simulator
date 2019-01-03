package com.aliyun.iotx.simulator.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 可以根据TSL自行定义一些struct类型
 * @author hanliang.hl
 * @date 2018-12-25 2:12 PM
 **/
public class GeoLocation {
    private Float longitude;
    private Float latitude;
    private Float altitude;
    @JSONField(name = "CoordinateSystem")
    private Integer coordinateSystem;

    public GeoLocation(Float longitude, Float latitude, Float altitude, Integer coordinateSystem) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.coordinateSystem = coordinateSystem;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getAltitude() {
        return altitude;
    }

    public Integer getCoordinateSystem() {
        return this.coordinateSystem;
    }

}
