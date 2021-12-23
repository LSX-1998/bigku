package com.lsx.community.dao;

import com.lsx.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId ,int offset ,int limit) ;

    //@Param 给参数取别名，只有一个参数在if里使用必须用别名
    int selectDiscussPostRows (@Param("userId") int userId) ;

}
