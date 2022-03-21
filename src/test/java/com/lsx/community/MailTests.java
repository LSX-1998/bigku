package com.lsx.community;

import com.lsx.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient ;

    @Autowired
    private TemplateEngine templateEngine ;

    @Test
    public void testTextMail(){
        mailClient.sendMail("lsx1209846193@sina.com","Test","Welcome");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","sunday");
        String html = templateEngine.process("/mail/demo", context);
        System.out.println(html);
        mailClient.sendMail("lsx1209846193@sina.com","HTML",html);
    }



}
