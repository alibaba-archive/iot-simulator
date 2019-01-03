package com.aliyun.iotx.simulator;

import com.alibaba.cloudapi.sdk.model.ApiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iotx.api.client.IoTApiRequest;
import com.aliyun.iotx.api.client.SyncApiClient;
import com.aliyun.iotx.simulator.domain.ApiIdentity;
import com.aliyun.iotx.simulator.domain.Device;
import com.aliyun.iotx.simulator.domain.UplinkMessage;
import com.aliyun.iotx.simulator.handler.MessageHandler;
import com.aliyun.iotx.simulator.util.AliyunIotX509TrustManager;
import com.aliyun.iotx.simulator.util.FileUtil;
import com.aliyun.iotx.simulator.util.SignUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hanliang.hl
 * @date 2018-12-25 11:38 AM
 **/
public class Simulator{
    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

    private static SyncApiClient apiClient;
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 600, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(100), new ThreadFactoryBuilder().setNameFormat("iotx-publish-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());
    private static final String OK = "200";
    static {
        String apiJsonPath = Simulator.class.getResource("/api.json").getPath();
        String apiJson = FileUtil.readFile(apiJsonPath);
        ApiIdentity apiIdentity = JSON.parseObject(apiJson, ApiIdentity.class);
        apiClient = SyncApiClient.newBuilder()
            .appKey(apiIdentity.getAccessKey())
            .appSecret(apiIdentity.getAccessSecret())
            .build();
    }

    private Device device;
    private String subTopic;
    private String pubTopic;
    private MqttClient mqttClient;

    /**
     * 已知三元组可以直接使用
     * @param productKey
     * @param deviceName
     * @param deviceSecret
     */
    public Simulator(String productKey, String deviceName, String deviceSecret) {
        this.device = new Device(productKey, deviceName, deviceSecret);
        this.subTopic = "/sys/" + productKey + "/" + deviceName + "/thing/service/property/set";
        this.pubTopic = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
    }

    /**
     * 没有三元组会从平台申请一个，但是你必须指定productKey和deviceName
     * @param productKey 产品，可以从阿里云官网>物联网套件>创建高级版产品
     * @param deviceName 设备名称，可以使用mac或者自定义名称
     */
    public Simulator(String productKey, String deviceName) {
        //创建设备
        this.device = createDevice(productKey, deviceName);
        this.subTopic = "/sys/" + productKey + "/" + deviceName + "/thing/service/property/set";
        this.pubTopic = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
        //持久化一下device 文件名：pk&dn, 内容：ds
        persistent(device);
    }

    /**
     * 连接
     * @param messageHandler
     * @throws Exception
     */
    public void connect(MessageHandler messageHandler) {
        try {
            String productKey = device.getProductKey();
            String deviceName = device.getDeviceName();
            String secret = device.getDeviceSecret();
            // 客户端设备自己的一个标记，建议是MAC或SN，不能为空，32字符内
            String clientId = InetAddress.getLocalHost().getHostAddress();
            // 设备认证
            Map<String, String> params = new HashMap<String, String>(16);
            // 这个是对应用户在控制台注册的 设备productkey
            params.put("productKey", productKey);
            // 这个是对应用户在控制台注册的 设备name
            params.put("deviceName", deviceName);
            params.put("clientId", clientId);
            String t = System.currentTimeMillis() + "";
            params.put("timestamp", t);

            // MQTT服务器地址，TLS连接使用ssl开头
            String targetServer = "ssl://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";

            // 客户端ID格式，两个||之间的内容为设备端自定义的标记，字符范围[0-9][a-z][A-Z]
            String mqttclientId = clientId + "|securemode=2,signmethod=hmacsha1,timestamp=" + t + "|";
            // mqtt用户名格式
            String mqttUsername = deviceName + "&" + productKey;
            // 签名
            String mqttPassword = SignUtil.sign(params, secret, "hmacsha1");
            MemoryPersistence persistence = new MemoryPersistence();
            SSLSocketFactory socketFactory = createSSLSocket();
            final MqttClient sampleClient = new MqttClient(targetServer, mqttclientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // MQTT 3.1.1
            connOpts.setMqttVersion(4);
            connOpts.setSocketFactory(socketFactory);
            // 设置是否自动重连
            connOpts.setAutomaticReconnect(true);
            // 如果是true，那么清理所有离线消息，即QoS1或者2的所有未接收内容
            connOpts.setCleanSession(false);

            connOpts.setUserName(mqttUsername);
            connOpts.setPassword(mqttPassword.toCharArray());
            connOpts.setKeepAliveInterval(65);
            logger.info(clientId + "进行连接, 目的地: " + targetServer);
            sampleClient.connect(connOpts);
            logger.info("连接成功！");
            sampleClient.subscribe(subTopic, (topic, mqttMessage) -> {
                String msgJson = mqttMessage.toString();
                //logger.info("topic: {} received message: {}", topic, msgJson);
                UplinkMessage uplinkMessage = new UplinkMessage();
                JSONObject jsonObject = (JSONObject)JSON.parseObject(msgJson).get("params");
                jsonObject.forEach(uplinkMessage::put);
                this.uplink(uplinkMessage);
                messageHandler.messageReceived(msgJson);
            });
            this.mqttClient =  sampleClient;
        } catch (Exception e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 上行消息
     * @param message
     */
    public void uplink(UplinkMessage message) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(0);
        mqttMessage.setPayload(JSON.toJSONString(message).getBytes());
        executorService.submit(() -> {
            try {
                this.mqttClient.publish(pubTopic, mqttMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 创建设备
     * @param productKey
     * @param deviceName
     * @return
     */
    private Device createDevice(String productKey, String deviceName){
        IoTApiRequest request = new IoTApiRequest();
        //设置api的版本
        request.setApiVer("1.1.2");
        // 接口参数
        request.putParam("productKey", productKey);
        request.putParam("deviceName", deviceName);

        //请求参数域名、path、request
        ApiResponse response;
        try {
            response = apiClient.postBody("api.link.aliyun.com",
                "/thing/device/create", request, true);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        JSONObject responseMap = (JSONObject)JSON.parse(response.getBody());
        if(responseMap == null) {
            throw new RuntimeException("server error. api: /thing/device/create return null. args[pk:"
                + productKey + ", dn:" + deviceName + "]");
        }
        String code = responseMap.getString("code");
        if(OK.equals(code)) {
            String deviceSecret = ((JSONObject) responseMap.get("data")).getString("deviceSecret");
            return new Device(productKey, deviceName, deviceSecret);
        } else {
            logger.error("createDevice({}, {}) error. response: {}", productKey, deviceName, responseMap.toJSONString());
            throw new RuntimeException(responseMap.toJSONString());
        }
    }

    private static void persistent(Device device) {
        String deviceFile = Simulator.class.getResource("/devices").getPath();
        FileUtil.writeFile(deviceFile,
            device.getProductKey() + "\t" + device.getDeviceName() + "\t" + device.getDeviceSecret(),
            true);
    }

    private static SSLSocketFactory createSSLSocket() throws Exception {
        SSLContext context = SSLContext.getInstance("TLSV1.2");
        context.init(null, new TrustManager[] { new AliyunIotX509TrustManager() }, null);
        return context.getSocketFactory();
    }
}
