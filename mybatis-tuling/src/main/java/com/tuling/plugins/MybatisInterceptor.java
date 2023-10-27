package com.tuling.plugins;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @Description:
 * @Date : 2023/10/20 18:41
 * @Auther : tiankun
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
)})
public class MybatisInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MybatisInterceptor.class);

    private static String[] KEY_PROPERTIES = new String[]{"entity.id", "id"};
    private static final Void __STATIC_SECTION__ = null;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // Method method = invocation.getMethod();
        // System.out.println("method.getName() = " + method.getName());
        // Object[] args = invocation.getArgs();
        // if(args.length > 0){
        //     for (int i = 0; i < args.length; i++) {
        //         System.out.println("args[i] = " + args[i]);
        //     }
        // }
        // Object target = invocation.getTarget();
        // return invocation.proceed();
        Object[] args = invocation.getArgs();
        MybatisInterceptor.MysqlContext ctx = new MybatisInterceptor.MysqlContext();
        if (invocation.getTarget() instanceof Executor) {
            Executor executor = (Executor)invocation.getTarget();
            MappedStatement ms = (MappedStatement)args[0];
            args[0] = beforeExecutorUpdate(ctx, executor, ms, args[1]);
        }

        long timer = System.currentTimeMillis();
        Object ret = invocation.proceed();
        timer = System.currentTimeMillis() - timer;
        if (invocation.getTarget() instanceof Executor) {
            MappedStatement ms = (MappedStatement)args[0];
            afterExecutorUpdate(ctx, ms, args[1], (Integer)ret, timer);
        }

        return ret;
    }


    private static MappedStatement beforeExecutorUpdate(MybatisInterceptor.MysqlContext ctx, Executor exc, MappedStatement ms, Object para) {
        ctx.parameterObject = para;
        // 处理新增
        if (SqlCommandType.INSERT.equals(ms.getSqlCommandType())) {
            if (ms.getKeyGenerator() == null || ms.getKeyGenerator() instanceof NoKeyGenerator || ms.getKeyProperties() == null || ms.getKeyProperties().length <= 0) {
                String idProperty = getAlienKeyProperty(ms, para);
                if (idProperty != null) {
                    ms = newAlienKeyGenMappedStatement(ms, idProperty);
                }
            }

            if (ms.getKeyProperties() != null && ms.getKeyProperties().length == 1) {
                ctx.keyProperty = ms.getKeyProperties()[0];
            }

            ctx.isInsert = true;
        }

        return ms;
    }

    private static void afterExecutorUpdate(MybatisInterceptor.MysqlContext ctx, MappedStatement ms, Object para, Integer rows, Long mills) {
        if (rows > 0) {
            // 获取原生sql
            BoundSql boundSql = ms.getBoundSql(para);
            String sql = boundSql.getSql();
            List<Object> params = getParameters(ms, boundSql, para);
            // 处理新增
            if (SqlCommandType.INSERT.equals(ms.getSqlCommandType())) {
                if (ctx.keyProperty != null) {
                    List<Object> keys = new ArrayList();
                    String[] keyProperties = ms.getKeyProperties();
                    if (keyProperties != null && keyProperties.length == 1) {
                        Configuration configuration = ms.getConfiguration();
                        Collection<Object> keyGenObj = getParameters(boundSql.getParameterObject());
                        Iterator var12 = keyGenObj.iterator();

                        while(var12.hasNext()) {
                            Object kgo = var12.next();
                            MetaObject metaParam = configuration.newMetaObject(kgo);
                            if (metaParam.hasGetter(keyProperties[0])) {
                                keys.add(metaParam.getValue(keyProperties[0]));
                            }
                        }
                    }
                    logger.info("StackTraceSession.onSqlUpdate(ctx.database : {}, " +
                            "sql：{}, " +
                            "params.toArray() :{}, " +
                            "keys.toArray() :{}, " +
                            "mills :{})",ctx.database, sql, params.toArray(), keys.toArray(), mills);
                    // StackTraceSession.onSqlUpdate(ctx.database, sql, params.toArray(), keys.toArray(), mills);

                }
            // 修改失败
            } else {
                // StackTraceSession.onSqlUpdate(ctx.database, sql, params.toArray(), (Object[])null, mills);
                logger.info("StackTraceSession.onSqlUpdate(ctx.database : {}, " +
                        "sql：{}, " +
                        "params.toArray() :{}, " +
                        "keys.toArray() :{}, " +
                        "mills :{})",ctx.database, sql, params.toArray(), (Object[])null, mills);
            }

        }
    }

    private static String getAlienKeyProperty(MappedStatement ms, Object para) {
        MetaObject meta = ms.getConfiguration().newMetaObject(getParameters(para).stream().findFirst().get());
        String[] var3 = KEY_PROPERTIES;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String keyProperty = var3[var5];
            if (meta.hasGetter(keyProperty)) {
                return keyProperty;
            }
        }

        return null;
    }

    private static MappedStatement newAlienKeyGenMappedStatement(MappedStatement ms, String keyProperty) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), ms.getSqlSource(), ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(Jdbc3KeyGenerator.INSTANCE);
        builder.keyProperty(keyProperty);
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    public static List<Object> getParameters(MappedStatement ms, BoundSql boundSql, Object parameterObject) {
        Configuration configuration = ms.getConfiguration();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        List<Object> parameterValues = new ArrayList();
        if (parameterMappings != null && !parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                parameterValues.add(parameterObject);
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);

                Object obj;
                for(Iterator var8 = parameterMappings.iterator(); var8.hasNext(); parameterValues.add(obj)) {
                    ParameterMapping parameterMapping = (ParameterMapping)var8.next();
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        obj = metaObject.getValue(propertyName);
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        obj = boundSql.getAdditionalParameter(propertyName);
                    } else {
                        obj = null;
                    }
                }
            }
        }

        return parameterValues;
    }

    private static Collection<Object> getParameters(Object parameter) {
        Collection<Object> parameters = null;
        if (parameter instanceof Collection) {
            parameters = (Collection)parameter;
        } else if (parameter instanceof Map) {
            Map parameterMap = (Map)parameter;
            if (parameterMap.containsKey("collection")) {
                parameters = (Collection)parameterMap.get("collection");
            } else if (parameterMap.containsKey("list")) {
                parameters = (List)parameterMap.get("list");
            } else if (parameterMap.containsKey("array")) {
                parameters = Arrays.asList((Object[])((Object[])parameterMap.get("array")));
            }
        }

        if (parameters == null) {
            parameters = new ArrayList();
            ((Collection)parameters).add(parameter);
        }

        return (Collection)parameters;
    }

    private static class MysqlContext {
        public Object parameterObject;
        public String database;
        public List<Object> generatedKeys;
        public boolean isInsert;
        public String keyProperty;

        private MysqlContext() {
            this.isInsert = false;
        }
    }




    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        properties.forEach((k,v) -> {
            logger.info("setProperties key:{} value:{}",k,v);
        });

    }
}
