package com.lsx.community.config;

import com.lsx.community.util.CommunityConstant;
import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {




    @Bean
    public Queue commentQueue() {
        //名称，是否持久化
        return new Queue(CommunityConstant.COMMENT_QUEUE,true);
    }
    @Bean
    public Queue likeQueue(){
        return new Queue(CommunityConstant.LIKE_QUEUE,true);
    }

    @Bean
    public Queue followQueue(){
        return new Queue(CommunityConstant.FOLLOW_QUEUE,true);
    }


    @Bean
    public Queue publishQueue(){ return new Queue(CommunityConstant.PUBLISH_QUEUE,true);}


    @Bean
    public Queue deleteQueue(){
        return  new Queue(CommunityConstant.DELETE_QUEUE,true);
    }

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(CommunityConstant.DIRECT_EXCHANGE);
    }


    @Bean
    public Binding bindingPublish(){
        return  BindingBuilder.bind(publishQueue()).to(exchange()).with(CommunityConstant.TOPIC_PUBLISH);
    }




    @Bean
    public Binding bindingLike(){
        return  BindingBuilder.bind(likeQueue()).to(exchange()).with(CommunityConstant.TOPIC_LIKE);
    }
    @Bean
    public Binding bindingComment(){
        return  BindingBuilder.bind(commentQueue()).to(exchange()).with(CommunityConstant.TOPIC_COMMENT);
    }

    @Bean
    public Binding bindingFollow(){
        return  BindingBuilder.bind(followQueue()).to(exchange()).with(CommunityConstant.TOPIC_FOLLOW);
    }

    @Bean
    public Binding bindingDelete(){
        return BindingBuilder.bind(deleteQueue()).to(exchange()).with(CommunityConstant.TOPIC_DELETE);
    }




}
