package com.lsx.community.service;

import com.lsx.community.dao.DiscussPostMapper;
import com.lsx.community.entity.DiscussPost;
import com.lsx.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper ;

    @Autowired
    private SensitiveFilter sensitiveFilter ;



    public List<DiscussPost> findDiscussPosts (int userId ,int offset ,int limit ,int orderMode){
        return  discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode) ;
    }
    public int findDiscussPostRows (int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public  int addDiscussPost (DiscussPost discussPost){
         if(discussPost==null){
             throw new IllegalArgumentException("参数不能为空！") ;
         }
         //转义HTML标记
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));

        //敏感词过滤
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));

        return  discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById (int id ){
        return  discussPostMapper.selectDiscussById(id);
    }

    public int updateCommentCount (int id ,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType (int id,int type){
        return discussPostMapper.updateType(id,type) ;
    }

    public int updateStatus (int id ,int status){
        return discussPostMapper.updateStatus(id,status);
    }


    public int updateScore (int id ,double score){
        return discussPostMapper.updateScore(id,score);
    }

}
