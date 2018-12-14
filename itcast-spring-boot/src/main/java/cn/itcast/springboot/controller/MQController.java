package cn.itcast.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/mq")
@RestController
public class MQController {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 发送MQ消息
     * @return 操作结果
     */
    @GetMapping("/sendMsg")
    public String sendMsg(){
        Map<String,Object> map = new HashMap<>();
        map.put("id",123);
        map.put("name","黑马");

        jmsMessagingTemplate.convertAndSend("spring.boot.mq.queue",map);
        return "发送MQ消息到队列spring.boot.mq.queue";
    }

    @GetMapping("/sendSmsMsg")
    public String sendSmsMsg(){
        Map<String,Object> map = new HashMap<>();
        map.put("mobile","15256548937");
        map.put("signName","黑马");
        map.put("templateCode","SMS_152514105");
        map.put("templateParam","{\"code\":\"123456\"}");

        //发送消息到mq
        jmsMessagingTemplate.convertAndSend("itcast_sms_queue",map);

        return "发送MQ队列itcast_sms_queue消息成功";
    }
}
