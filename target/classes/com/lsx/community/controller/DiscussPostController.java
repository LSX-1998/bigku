package com.lsx.community.controller;

import com.lsx.community.entity.*;
import com.lsx.community.event.EventProducer;
import com.lsx.community.service.CommentService;
import com.lsx.community.service.DiscussPostService;
import com.lsx.community.service.LikeService;
import com.lsx.community.service.UserService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import com.lsx.community.util.HostHolder;
import com.lsx.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@RequestMapping(value = "/discuss")
@Controller
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService ;

    @Autowired
    private HostHolder hostHolder ;

    @Autowired
    private UserService userService ;

    @Autowired
    private CommentService commentService ;

    @Autowired
    private LikeService likeService ;

    @Autowired
    private EventProducer eventProducer ;

    @Autowired
    private RedisTemplate redisTemplate ;

    @RequestMapping(value = "/add" ,method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost (String title ,String content){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录");
        }

        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setUserId(user.getId());
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                                 .setEntityId(post.getId())
                                 .setUserId(user.getId())
                                 .setEntityType(CommunityConstant.ENTITY_TYPE_POST);

        eventProducer.fireEvent(event);

        //计算帖子分数

        String redisKey = RedisKeyUtil.getPostScoreKey() ;
        redisTemplate.opsForSet().add(redisKey,post.getId());







        //报错的情况以后统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(value = "/detail/{discussPostId}" ,method = RequestMethod.GET)
    public String getDiscussPost (@PathVariable("discussPostId") int discussPostId, Model model, Page page){

        //帖子
         DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
         model.addAttribute("post",discussPost);

        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);

        //点赞状态
        int likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(CommunityConstant.ENTITY_TYPE_POST,discussPostId,hostHolder.getUser().getId());
        model.addAttribute("likeStatus",likeStatus);

        //分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());


        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntityId(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        //评论Vo列表
        List<Map<String,Object>> commentVoList =  new ArrayList<>() ;
        if(commentList!=null){
            for(Comment comment:commentList){
                //评论Vo
                Map<String,Object> commentVo = new HashMap<>() ;

                //评论
                commentVo.put("comment",comment);

                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);

                //点赞状态
                likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId(),hostHolder.getUser().getId());
                commentVo.put("likeStatus",likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntityId(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                //回复Vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply:replyList){
                        //回复Vo
                        Map<String,Object> replyVo = new HashMap<>() ;

                        //回复
                        replyVo.put("reply",reply);

                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));

                        //回复的目标
                        User target  = reply.getTargetId() ==0? null:userService.findUserById(reply.getTargetId()) ;
                        replyVo.put("target",target);

                        //点赞的数量
                        likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);

                        //点赞的状态
                        likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId(),hostHolder.getUser().getId());
                        replyVo.put("likeStatus",likeStatus);


                        replyVoList.add(replyVo);
                    }

                }
              commentVo.put("replys",replyVoList);

              //回复数量
                int replyCount = commentService.findCommentCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

              commentVoList.add(commentVo);
            }
            model.addAttribute("comments",commentVoList);
        }
        return "site/discuss-detail" ;
    }


    //置顶
    @RequestMapping(value = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        //触发发帖事件
        Event event = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setEntityId(id)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0) ;
    }

    //加精
    @RequestMapping(value = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        //触发发帖事件
        Event event = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setEntityId(id)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        //计算帖子分数

        String redisKey = RedisKeyUtil.getPostScoreKey() ;
        redisTemplate.opsForSet().add(redisKey,id);
        return CommunityUtil.getJSONString(0) ;
    }

    //删帖
    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        //触发发帖事件
        Event event = new Event().setTopic(CommunityConstant.TOPIC_DELETE)
                .setEntityId(id)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0) ;
    }


    //









}
