package com.lsx.community.event;

import com.alibaba.fastjson.JSONObject;
import com.lsx.community.entity.Event;
import com.lsx.community.util.CommunityConstant;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    private AmqpTemplate amqpTemplate ;

    //处理事件
    public void fireEvent (Event event){
        //将事件发布到指定的主题
        String msg = JSONObject.toJSONString(event);
        System.out.println(event.getTopic());
        amqpTemplate.convertAndSend(CommunityConstant.DIRECT_EXCHANGE,event.getTopic(), msg);
    }
}
