package info.dourok.esactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示返回给请求者 Activity 的结果
 * Created by tiaolins on 2017/9/1.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Result {

  /**
   * @return Result 的名称，Builder 会生成 for#{name} 的方法
   */
  String name() default "";

  /**
   * 每个结果可以有零或多个参数
   */
  ResultParameter[] parameters() default {};
}
