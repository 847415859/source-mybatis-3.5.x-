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
package org.apache.ibatis.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 参数解析器
 */
public class ParamNameResolver {

  private static final String GENERIC_NAME_PREFIX = "param";

  /**
   * 特殊参数（即RowBounds或ResultHandler）时，此索引可能与实际索引不同。
   * aMethod（@Param（“M”）int a，@Param（“N”）int b）->｛｛0，“M”｝，｛1，“N”｝｝
   * aMethod（int a，int b）->｛｛0，“0”｝，｛1，“1”｝｝
   * aMethod（int a，RowBounds rb，int b）->｛｛0，“0”｝，｛2，“1”｝｝
   *
   */
  private final SortedMap<Integer, String> names;

  private boolean hasParamAnnotation;

  public ParamNameResolver(Configuration config, Method method) {
    /**
     * 解析我们的参数的类型
     */
    final Class<?>[] paramTypes = method.getParameterTypes();
    /**
     * 解析我们方法上的@Param注解
     */
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<>();
    int paramCount = paramAnnotations.length;
    /**
     * 解析我们标注了@Param注解
     */
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      // 判断是否为特殊参数
      if (isSpecialParameter(paramTypes[paramIndex])) {
        // skip special parameters
        continue;
      }
      String name = null;
      // 获取参数上的 @Param 注解配置的值
      for (Annotation annotation : paramAnnotations[paramIndex]) {
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          name = ((Param) annotation).value();
          break;
        }
      }
      // 如果属性上没有配置@Param注解，则使用默认的命名规则
      if (name == null) {
        if (config.isUseActualParamName()) {  // 默认是true
          name = getActualParamName(method, paramIndex);  // 则是参数列表 arg0,arg1  数字为参数索引的位置
        }
        if (name == null) {
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          name = String.valueOf(map.size());
        }
      }
      map.put(paramIndex, name);
    }
    /**
     *Dept findDeptByIdAndName(@Param("id") Integer id,@Param("name") String name);
     * 变为Map   {0,id},{1,name}
     */
    names = Collections.unmodifiableSortedMap(map);
  }

  private String getActualParamName(Method method, int paramIndex) {
    return ParamNameUtil.getParamNames(method).get(paramIndex);
  }

  /**
   * 判断是否为特殊参数
   * @param clazz
   * @return
   */
  private static boolean isSpecialParameter(Class<?> clazz) {
    return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
  }

  /**
   * Returns parameter names referenced by SQL providers.
   */
  public String[] getNames() {
    return names.values().toArray(new String[0]);
  }

  /**
   * <p>
   * A single non-special parameter is returned without a name.
   * Multiple parameters are named using the naming rule.
   * In addition to the default names, this method also adds the generic names (param1, param2,
   * ...).
   * </p>
   */
  public Object getNamedParams(Object[] args) {
    //获取参数的个数
    /**
     * names的数据结构为map
     * ({key="0",value="id"},{key="1",value="name"})
     */
    final int paramCount = names.size();
    //若参数的个数为空或者个数为0直接返回
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
      /**
       * 若有且只有一个参数 而且没有标注了@Param指定方法方法名称
       *
       */
      return args[names.firstKey()];
    } else {
      final Map<String, Object> param = new ParamMap<>();
      int i = 0;
      /**
       * 循坏我们所有的参数的个数
       */
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
        //把key为id,value为1加入到param中
        param.put(entry.getValue(), args[entry.getKey()]);
        // add generic param names (param1, param2, ...)
        //加入通用的参数：名称为param+0,1,2,3......
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
        // ensure not to overwrite parameter named with @Param
        if (!names.containsValue(genericParamName)) {
          //把key为param+0,1,2,3.....,value值加入到param中
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
}
