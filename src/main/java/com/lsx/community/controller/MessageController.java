package com.lsx.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.lsx.community.entity.Message;
import com.lsx.community.entity.Page;
import com.lsx.community.entity.User;
import com.lsx.community.service.MessageService;
import com.lsx.community.service.UserService;
import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import com.lsx.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController {


    @Autowired
    private MessageService messageService ;

    @Autowired
    private HostHolder hostHolder ;

    @Autowired
    private UserService userService ;
    //私信列表
    @RequestMapping(value = "/letter/list",method = RequestMethod.GET)
    public String getLetterList (Model model , Page page){


            //分页信息
           User user = hostHolder.getUser();
           page.setRows(messageService.findConversationCount(user.getId()));
           page.setPath("/letter/list");
           page.setLimit(5);

           //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>>  conversations = new ArrayList<>() ;
        if(conversationList!=null){
            for(Message message:conversationList){
                  Map<String,Object> map = new HashMap<>() ;
                  map.put("conversation",message);
                  map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                  map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                  int targetId =  user.getId()==message.getFromId()?message.getToId():message.getFromId() ;
                  map.put("target",userService.findUserById(targetId));
                  conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null) ;
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return  "/site/letter" ;

    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail (@PathVariable("conversationId") String conversationId,Page page ,Model model){
        //分页信息设置
        page.setLimit(5);
        page.setPath("/letter/detail"+"/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>() ;
        if(letterList!=null){
            for(Message message: letterList){
                Map<String,Object> map = new HashMap<>() ;
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail" ;
    }


    public User getLetterTarget (String conversationId){
        String[] str = conversationId.split("_");
        int id1 = Integer.parseInt(str[0]) ;
        int id2 = Integer.parseInt(str[1]) ;

        if(hostHolder.getUser().getId()==id1){
            return userService.findUserById(id2);
        }else{
            return userService.findUserById(id1);
        }
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter (String toName ,String content){


       User target = userService.findUserByName(toName) ;
       if(target==null){
           return CommunityUtil.getJSONString(1,"目标用户不存在");
       }
       Message message = new Message();
       message.setFromId(hostHolder.getUser().getId());
       message.setContent(content);
       message.setToId(target.getId());
       if(message.getFromId()<message.getToId()){
           message.setConversationId(message.getFromId()+"_"+message.getToId());
       }else{
           message.setConversationId(message.getToId()+"_"+message.getFromId());
       }

       message.setCreateTime(new Date());
       messageService.addMessage(message);

       return CommunityUtil.getJSONString(0);
    }

    public List<Integer> getLetterIds (List<Message> letterList){
        List<Integer> ids = new ArrayList<>() ;
        if(letterList!=null){
            for(Message message : letterList){
                if(hostHolder.getUser().getId()==message.getToId()&&message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids ;
    }

    @RequestMapping(value = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList (Model model){
        User user = hostHolder.getUser() ;

        //查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(), CommunityConstant.TOPIC_COMMENT) ;
        Map<String,Object> messageVo = new HashMap<>() ;
        if(message!=null){
            messageVo.put("message",message) ;

            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String,Object>  data  = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count  =  messageService.findNoticeCount(user.getId(),CommunityConstant.TOPIC_COMMENT);
            messageVo.put("count",count);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(),CommunityConstant.TOPIC_COMMENT);
            messageVo.put("unreadCount",unreadCount);
        }
        model.addAttribute("commentNotice",messageVo);

        //查询点赞类通知
          message = messageService.findLatestNotice(user.getId(),CommunityConstant.TOPIC_LIKE);
          messageVo = new HashMap<>() ;
          if(message!=null){
              messageVo.put("message",message);

              String content = HtmlUtils.htmlEscape(message.getContent()) ;
              Map<String,Object> data = JSONObject.parseObject(content,HashMap.class) ;

              messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
              messageVo.put("entityType",data.get("entityType"));
              messageVo.put("entityId",data.get("entityId"));
              messageVo.put("postId",data.get("postId"));

              int count  =  messageService.findNoticeCount(user.getId(),CommunityConstant.TOPIC_LIKE);
              messageVo.put("count",count);
              int unreadCount = messageService.findNoticeUnreadCount(user.getId(),CommunityConstant.TOPIC_LIKE);
              messageVo.put("unreadCount",unreadCount);
          }
         model.addAttribute("likeNotice",messageVo);

        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(),CommunityConstant.TOPIC_FOLLOW) ;
        messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message",message) ;
            String content = HtmlUtils.htmlEscape(message.getContent()) ;
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class) ;

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType")) ;
            messageVo.put("entityId",data.get("entityId"));


            int count = messageService.findNoticeCount(user.getId(),CommunityConstant.TOPIC_FOLLOW) ;
            messageVo.put("count",count) ;
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(),CommunityConstant.TOPIC_FOLLOW) ;
            messageVo.put("unreadCount",unreadCount) ;
        }
        model.addAttribute("followNotice",messageVo);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount) ;
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null) ;
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice" ;
    }

    @RequestMapping(value = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail (@PathVariable("topic") String topic ,Page page ,Model model){
         User user = hostHolder.getUser() ;
         page.setLimit(5);
         page.setPath( "/notice/detail/"+topic);
         page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>() ;

        if(noticeList!=null){
            for(Message notice : noticeList){
                Map<String,Object> map = new HashMap<>() ;
                //通知
                map.put("notice",notice) ;

                //内容
                String content = HtmlUtils.htmlEscape(notice.getContent()) ;
                Map<String,Object> data = JSONObject.parseObject(content) ;
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);

            }
        }

        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList) ;
        if(!ids.isEmpty()){
            messageService.readMessage(ids) ;
        }

        return "/site/notice-detail" ;

    }
}
