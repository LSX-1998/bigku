package com.lsx.community;

import com.lsx.community.dao.DiscussPostMapper;
import com.lsx.community.dao.LoginTicketMapper;
import com.lsx.community.dao.MessageMapper;
import com.lsx.community.entity.DiscussPost;
import com.lsx.community.entity.LoginTicket;
import com.lsx.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapperTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper ;

    @Autowired
    private MessageMapper messageMapper ;

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10,0);
        for(DiscussPost post : list){
            System.out.println(post);
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test

    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicket.setId(66);
        loginTicket.setStatus(0);
        loginTicket.setUserId(100);
        loginTicket.setTicket("lll");
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("lll");
        System.out.println(loginTicket.toString());

    }

    @Test
    public void testUpdateLoginTicket(){
        int id = loginTicketMapper.updateStatus("lll",6);
        System.out.println(id);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for(Message message : list){
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for(Message message : list){
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);
    }


}
