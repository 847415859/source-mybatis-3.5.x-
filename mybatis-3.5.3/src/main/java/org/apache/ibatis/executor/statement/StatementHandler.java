/**
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;

/**
 * @author Clinton Begin
 */
public interface StatementHandler {

  // 用于创建一个具体的 Statement 对象的实现类或者是 Statement 对象
  Statement prepare(Connection connection, Integer transactionTimeout)
      throws SQLException;

  // 用于初始化 Statement 对象以及对sql的占位符进行赋值
  void parameterize(Statement statement)
      throws SQLException;

  // 用于通知 Statement 对象将进行批量操作
  void batch(Statement statement)
      throws SQLException;

  // 用于通知 Statement 对象将 insert、update、delete 操作推送到数据库
  int update(Statement statement)
      throws SQLException;

  // 用于通知 Statement 对象将 select 操作推送数据库并返回对应的查询结果
  <E> List<E> query(Statement statement, ResultHandler resultHandler)
      throws SQLException;

  // 游标查询
  <E> Cursor<E> queryCursor(Statement statement)
      throws SQLException;

  // 获取BoundSql对象
  BoundSql getBoundSql();

  // 获取ParameterHandler
  ParameterHandler getParameterHandler();

}
