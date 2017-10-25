package info.dourok.esactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * 表示返回给请求者 Activity 的结果
 * 可用于 Activity 类或者方法
 * 用于 Activity 时，name 不能为空
 * 用于方法时 name 会被忽略
 * 方法命名需满足 result(?&lt;name&gt;[A-Z][\w]*) 的正则表达式
 *
 * 对于参数类型没有泛型类的话，可直接注解于 Activity
 * 如果需要支持泛型只能通过注解方法来实现
 * Created by tiaolins on 2017/9/1.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Result {

  /**
   * @return Result 的名称，Builder 会生成 for{name} 的方法
   */
  String name() default "";

  /**
   * 每个结果可以有零或多个参数
   */
  ResultParameter[] parameters() default {};
}
