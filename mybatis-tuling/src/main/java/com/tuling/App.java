package com.tuling;

import com.tuling.entity.User;
import com.tuling.mapper.UserMapper;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/***
 * @Author 徐庶   QQ:1092002729
 * @Slogan 致敬大师，致敬未来的你
 */
public class App {
    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        //将XML配置文件构建为Configuration配置类
        Reader reader = Resources.getResourceAsReader(resource);
        // 通过加载配置文件流构建一个SqlSessionFactory   解析xml文件  1
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        // 数据源 执行器  DefaultSqlSession
        SqlSession session = sqlSessionFactory.openSession();
        try {
            // 执行查询 底层执行jdbc 3
            // (1).根据statamentid来从 Configuration 中的 map 集合中获取到了指定的MappedStatement 对象
            // (2) 将查询任务委派给 executor 执行器
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = new User();
            user.setUserName("乾坤");
            user.setAge(18);
            // List<User> users = mapper.listAll(user);
            List<User> users = mapper.listByParam("乾坤", 18);
            System.out.println(users);
            // session.commit();
            // User user1 = mapper.selectById(1);
            // System.out.println(user1);
            // User user = new User();
            // user.setId(1L);
            // user.setAge(8);
            // user.setUserName("乾坤");
            // user.setCreateTime(new Date());
            // session.update("com.tuling.mapper.UserMapper.update", user);
            // User user = mapper.selectById(1);
            // System.out.println(user);
            // 创建动态代理
            // System.out.println(user.getUserName());
            // mapper.updateForName("1","田坤");
            // session.commit();
            List<Integer> integers = Arrays.asList(null, 1);
            System.out.println(integers.toArray().length);

        } catch (Exception e) {
            e.printStackTrace();
            // session.rollback();
        } finally {
            // session.close();
        }
    }


    // public static void main(String[] args) throws IOException {
    //     String resource = "mybatis-config.xml";
    //     //将XML配置文件构建为Configuration配置类
    //     Reader reader = Resources.getResourceAsReader(resource);
    //     // 通过加载配置文件流构建一个SqlSessionFactory   解析xml文件  1
    //     SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    //     // 数据源 执行器  DefaultSqlSession
    //     SqlSession session = sqlSessionFactory.openSession();
    //     try {
    //         User user =  session.selectOne("com.tuling.mapper.UserMapper.selectById", 1);
    //         session.commit();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         session.rollback();
    //     } finally {
    //         session.close();
    //     }
    // }


}
