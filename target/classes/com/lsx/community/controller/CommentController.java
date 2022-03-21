package com.lsx.community.controller;

import com.lsx.community.entity.Comment;
import com.lsx.community.entity.DiscussPost;
import com.lsx.community.entity.Event;
import com.lsx.community.event.EventProducer;
import com.lsx.community.service.CommentService;
import com.lsx.community.service.DiscussPostService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.HostHolder;
import com.lsx.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(value = "/comment")
public class CommentController {

    @Autowired
    private CommentService commentService ;

    @Autowired
    private HostHolder hostHolder ;

    @Autowired
    private EventProducer eventProducer ;

    @Autowired
    private DiscussPostService discussPostService ;

    @Autowired
    private RedisTemplate redisTemplate ;

    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment (@PathVariable("discussPostId") int discussPostId , Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论
        Event event = new Event().setTopic(CommunityConstant.TOPIC_COMMENT)
                                  .setUserId(hostHolder.getUser().getId())
                                  .setEntityType(comment.getEntityType())
                                  .setEntityId(comment.getEntityId())
                                  .setData("postId",discussPostId);

        if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST){
            DiscussPost target =  discussPostService.findDiscussPostById(comment.getEntityId()) ;
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST){

            //触发发帖事件
             event = new Event().setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                                .setUserId(comment.getUserId())
                                .setTopic(CommunityConstant.TOPIC_PUBLISH)
                                .setEntityId(discussPostId);

             eventProducer.fireEvent(event);


            //计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey() ;
            redisTemplate.opsForSet().add(redisKey,discussPostId);

        }


        return "redirect:/discuss/detail/"+discussPostId ;
    }
}
