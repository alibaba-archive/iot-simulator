package com.aliyun.iotx.test;

import com.aliyun.iotx.simulator.Simulator;
import com.aliyun.iotx.simulator.domain.GeoLocation;
import com.aliyun.iotx.simulator.domain.UplinkMessage;

/**
 * @author hanliang.hl
 * @date 2018-12-25 2:19 PM
 **/
public class SimulatorTest {
    public static void main(String[] args) {
        //去阿里云官网上创建一个产品，设置好地理位置属性。可以变相用做分组
        String productKey = "<your productKey>";
        String deviceName = "anyDeviceName";
        //自动创建设备的构造方法
        Simulator simulator = new Simulator(productKey, deviceName);
        //如果已经有了三元组（productKey, deviceName, deviceSecret）可以使用下面的构造方法
        // Simulator simulator = new Simulator("<your productKey>", "<your deviceName>", "<your deviceSecret>");
        simulator.connect(msgJson -> {
            System.out.println("received: " + msgJson);
            //可以变相当成mq使用，执行收到消息之后的逻辑

        });
        //可以放在带有定位的设备中，手机，手表等等。通过api获取经纬度上报
        UplinkMessage uplinkMessage = new UplinkMessage()
            .put("GeoLocation", new GeoLocation(122.250852f,30.193851f, 12f, 2));
            //可以根据产品定义上报额外属性
            //.put("PM25", 60)
            //.put("CO2Value", 123)
            //.put("HeatSwitch", 0)
            //.put("LightSwitch", 1);
        simulator.uplink(uplinkMessage);
    }
}
