package com.tuling.mapper;

import com.tuling.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 * @Author 徐庶   QQ:1092002729
 * @Slogan 致敬大师，致敬未来的你
 */
public interface UserMapper {

    // User selectById(Integer id);
    //
    // Integer updateForName(String id,String username);

    // Integer update(User user);

    List<User> listAll(User user);


    List<User> listByParam(@Param("userName")String username , @Param("age") Integer age);
}
