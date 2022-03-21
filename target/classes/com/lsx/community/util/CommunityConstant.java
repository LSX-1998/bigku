package com.lsx.community.util;


public interface CommunityConstant {
  /*
   *  激活成功
   */
    final static int ACTIVATION_SUCCESS = 0 ;
    /*
       重复激活
     */
    final static int ACTIVATION_REPEAT = 1 ;
    /*
      激活失败
     */
    final static int ACTIVATION_FAILURE =2 ;

    /*
       默认状态的登录凭证的超时时间
     */
   final static int DEFAULT_EXPIRED_SECONDS = 3600*12 ;

   /*
      记住状态的登录凭证超时时间
    */
  final static int REMEMBER_EXPIRED_SECONDS = 3600*24*100 ;


  /*
     实体类型：帖子

   */
    final static int  ENTITY_TYPE_POST = 1 ;

    /*
      实体类型评论
     */
    final static  int ENTITY_TYPE_COMMENT = 2 ;

    /*
       实体类型用户
     */
   final static int ENTITY_TYPE_USER = 3 ;

  /**
   * 主题：评论
   */
  final static String TOPIC_COMMENT = "comment";

  /**
   * 主题：点赞
   */
  final static String TOPIC_LIKE = "like";

  /**
   * 主题：关注
   */
  final static String TOPIC_FOLLOW = "follow";

  /**
   * 主题：发帖
   */
  final static String  TOPIC_PUBLISH = "publish";

  /**
   * 主题：删帖
   */
  final static String  TOPIC_DELETE = "delete";

  /**
   * 主题：分享
   */
  final static String  TOPIC_SHARE = "share";






  /*
    系统用户Id
   */
    final static int SYSTEM_USER_ID =1 ;


    /*
       评论队列
     */
   static final String COMMENT_QUEUE="comment.queue";

   /*
      点赞队列
    */
   static final String LIKE_QUEUE="like.queue";

   /*
      关注队列
    */
   static final String FOLLOW_QUEUE = "follow.queue" ;



   /*
     发帖队列
    */

    static final String PUBLISH_QUEUE = "publish.queue" ;



    /*
      删帖队列
     */

    static final String DELETE_QUEUE = "delete.queue" ;

   /*
      交换机
    */
   static final String DIRECT_EXCHANGE = "directExchange" ;

   /*
      普通用户权限
    */

   static final String AUTHORITY_USER ="user" ;

   /*
    * 权限：管理员
    */

   static final String AUTHORITY_ADMIN = "admin" ;

   /*
     管理员
    */
   static final String AUTHORITY_MODERATOR="moderator" ;





}
