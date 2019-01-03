package com.aliyun.iotx.simulator.handler;

/**
 * @author hanliang.hl
 * @date 2018-12-25 1:47 PM
 **/
@FunctionalInterface
public interface MessageHandler {
    void messageReceived(String msgJson);
}
