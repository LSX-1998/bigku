package com.lsx.community.config;

import com.lsx.community.util.CommunityConstant;
import com.lsx.community.util.CommunityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web)  {
       web.ignoring().antMatchers("/resources/**") ;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                ).hasAnyAuthority(
                CommunityConstant.AUTHORITY_USER,
                CommunityConstant.AUTHORITY_MODERATOR,
                CommunityConstant.AUTHORITY_ADMIN
               )
                .antMatchers("/discuss/top",
                        "/discuss/wonderful")
                .hasAnyAuthority(CommunityConstant.AUTHORITY_MODERATOR)
                .antMatchers("/discuss/delete","/data/**")
                .hasAnyAuthority(CommunityConstant.AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();




        //权限不够处理

        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {

                    //没有登录时的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {



                        String   xRequestedWith = request.getHeader("x-requested-with") ;
                        if("XMLHttpRequest".equals(xRequestedWith)){
                              response.setContentType("application/plain;charset=utf-8");
                              PrintWriter writer = response.getWriter();
                              writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦！"));

                        }else{
                              response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {

                    //权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String   xRequestedWith = request.getHeader("x-requested-with") ;
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有访问此功能的权限"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });


        // Security底层默认会拦截/logout请求,进行退出处理.
        // 覆盖它默认的逻辑,才能执行我们自己的退出代码.

        http.logout().logoutUrl("/securitylogout");
    }
}
