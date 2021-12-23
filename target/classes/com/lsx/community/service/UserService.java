package com.lsx.community.service;

import com.lsx.community.dao.UserMapper;
import com.lsx.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper ;

    public User findUserById (int id){
        return  userMapper.selectById(id);
    }
}
