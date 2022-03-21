package com.lsx.community.controller;

import com.lsx.community.entity.Event;
import com.lsx.community.entity.User;
import com.lsx.community.event.EventProducer;
import com.lsx.community.service.LikeService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import com.lsx.community.util.HostHolder;
import com.lsx.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService ;

    @Autowired
    private HostHolder hostHolder ;

    @Autowired
    private EventProducer eventProducer ;

    @Autowired
    private RedisTemplate redisTemplate ;

    @RequestMapping(value = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like (int entityType ,int entityId ,int entityUserId ,int postId){
       User user = hostHolder.getUser();

       //点赞

        likeService.like(user.getId(),entityType,entityId,entityUserId);

        //数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);

        //状态
        int likeStatus = likeService.findEntityLikeStatus(entityType, entityId, user.getId());

        //返回的结果
        Map<String,Object> map = new HashMap<>() ;
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        //触发点赞事件
        if(likeStatus==1){
            Event event = new Event().setTopic(CommunityConstant.TOPIC_LIKE)
                                     .setUserId(hostHolder.getUser().getId())
                                     .setEntityId(entityId)
                                     .setEntityType(entityType)
                                     .setEntityUserId(entityUserId)
                                     .setData("postId",postId);

            eventProducer.fireEvent(event);




        }

        if(entityType==CommunityConstant.ENTITY_TYPE_POST){
            //计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey() ;
            redisTemplate.opsForSet().add(redisKey,postId);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }


}
