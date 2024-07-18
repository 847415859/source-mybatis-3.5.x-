/**
 * Copyright 2009-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

    private final Configuration configuration;
    // 解析出来的动态sql节点
    private final SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    /**
     * 这个方法主要做2件事：
     * 1. 解析所有sqlNode  解析成一条完整sql语句
     * 2. 将sql语句中的#{} 替换成问号， 并且把#{}中的参数解析成ParameterMapping （里面包含了typeHandler)
     * @param parameterObject:参数对象
     * @return
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 1.构建动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        // 2.归 责任链 处理一个个SqlNode   编译出一个完整sql
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        // 3.接下来处理 处理sql中的#{...} 并构建对应的 ParameterMapping，生成最终的 StaticSqlSource
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        // 怎么处理呢？ 很简单， 就是拿到#{}中的内容 封装为parameterMapper，  替换成?,
        // 在最后设置参数的时候就可以拿到队形转换器设置对应的方法  set{Type}()
        // 得到 StaticSqlSource
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        // 得到 BoundSql
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        //由于可能包含动态节点，所以要将动态节点生成的变量添加到 BoundSql 的附加参数上
        context.getBindings().forEach(boundSql::setAdditionalParameter);
        return boundSql;
    }


























}
