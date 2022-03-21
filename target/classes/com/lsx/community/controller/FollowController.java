package com.lsx.community.controller;

import com.lsx.community.entity.Event;
import com.lsx.community.entity.Page;
import com.lsx.community.entity.User;
import com.lsx.community.event.EventProducer;
import com.lsx.community.service.FollowService;
import com.lsx.community.service.UserService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import com.lsx.community.util.HostHolder;
import javafx.scene.chart.ValueAxis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService ;

    @Autowired
    private HostHolder hostHolder ;

    @Autowired
    private UserService userService ;

    @Autowired
    private EventProducer eventProducer ;

    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    public String follow (int entityType ,int entityId){
        User user = hostHolder.getUser() ;
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event().setTopic(CommunityConstant.TOPIC_FOLLOW)
                                 .setUserId(hostHolder.getUser().getId())
                                 .setEntityType(entityType)
                                 .setEntityId(entityId)
                                 .setEntityId(entityId);

        eventProducer.fireEvent(event);



        return CommunityUtil.getJSONString(0,"已关注");
    }

    @RequestMapping(value = "/unFollow",method = RequestMethod.POST)
    public String unFollow (int entityType ,int entityId){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    @RequestMapping(value = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId , Page page , Model model){
        User user =  userService.findUserById(userId) ;
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("userList",userList);
        return "/site/followee" ;
    }

    @RequestMapping(value = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers (@PathVariable("userId") int userId ,Page page ,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(userId,CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            for(Map<String,Object> map:userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }

        }
        model.addAttribute("userList",userList);
        return "site/follower" ;
    }


    private boolean hasFollowed (int userId){
        User user = hostHolder.getUser() ;
        if(user==null){
            return false ;
        }
        return  followService.hasFollowed(user.getId(),userId,CommunityConstant.ENTITY_TYPE_USER);
    }
}
