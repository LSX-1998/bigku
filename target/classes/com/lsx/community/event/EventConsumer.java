package com.lsx.community.event;

import com.alibaba.fastjson.JSONObject;
import com.lsx.community.entity.DiscussPost;
import com.lsx.community.entity.Event;
import com.lsx.community.entity.Message;
import com.lsx.community.service.DiscussPostService;
import com.lsx.community.service.ElasticsearchService;
import com.lsx.community.service.MessageService;
import com.lsx.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.dc.pr.PRError;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService ;

    @Autowired
    private DiscussPostService discussPostService ;

    @Autowired
    private ElasticsearchService elasticService ;

    @RabbitListener(queues = {CommunityConstant.LIKE_QUEUE,CommunityConstant.COMMENT_QUEUE,CommunityConstant.FOLLOW_QUEUE})
    public void handleCommentMessage(String msg){

        if(StringUtils.isBlank(msg)){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(msg,Event.class);

        if(event==null){
            logger.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(CommunityConstant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>() ;
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());


        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                   content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }


    //消费发帖事件
    @RabbitListener(queues = CommunityConstant.PUBLISH_QUEUE)
    public void handlePublishMessage(String msg){
        if(StringUtils.isBlank(msg)){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(msg,Event.class);

        if(event==null){
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticService.saveDiscussPost(post);
    }



    //消费删帖事件

    @RabbitListener(queues = CommunityConstant.DELETE_QUEUE)
    public void handleDeleteMessage (String msg){
        if(StringUtils.isBlank(msg)){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(msg,Event.class) ;
        if(event==null){
            logger.error("消息格式错误");
            return;
        }

        elasticService.deleteDiscussPost(event.getEntityId());

    }





}
