package com.lsx.community.controller;

import com.lsx.community.entity.DiscussPost;
import com.lsx.community.entity.Page;
import com.lsx.community.entity.User;
import com.lsx.community.service.DiscussPostService;
import com.lsx.community.service.LikeService;
import com.lsx.community.service.UserService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService ;

    @Autowired
    private UserService userService ;

    @Autowired
    private LikeService likeService ;

    @RequestMapping(path = "/index" ,method = RequestMethod.GET)
    public String getIndexPage (Model model, Page page ,@RequestParam(name = "orderMode",defaultValue = "0") int orderMode){

        //方法调用之前，会自动实例化Model和Page，并将Page注入Model,所以在thymeleaf里中可以直接访问Page对象


        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);


        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost post: list){
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                map.put("post",post);
                map.put("page",page);

                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("disCussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);

        return "/index" ;
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500" ;
    }

    @RequestMapping(value = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404" ;
    }

}
