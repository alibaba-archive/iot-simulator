## IoT Simulator
==========
### Description
You can quickly turn your phone(or any other device) into an Aliyun IoT device within 10 lines.


##### Follow the following steps:
1. Visit [Iot Console](https://iot.console.aliyun.com/product) and create an advanced product.(<b>REMEMBER YOUR PRODUCT KEY</b>)
2. Define functions of your product. For example, we can search and add a GeoLocation(地理位置)
3. Visit [Linkdevelop Console](https://linkdevelop.aliyun.com/admin) and create a project.
4. Create a [web application](https://linkdevelop.aliyun.com/p/a124YDB8CScraJgw/web/application) and put them in <b>api.json</b>.(AppKey and AppSecret is an important identity do api request)

##### Coding:
```java
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
```
##### create you map
visit [https://iot.console.aliyun.com/scene](https://iot.console.aliyun.com/scene) to create a scene.
you will see your device's location.

enjoy!







