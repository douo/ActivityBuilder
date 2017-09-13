package info.dourok.esactivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tiaolins on 2017/8/22.
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD })
@Inherited
public @interface BuilderParameter {

  String USE_VARIABLE_NAME = "";

  /**
   * 存入 Bundle 的 key
   */
  String key() default USE_VARIABLE_NAME;

  TransmitType transmit() default TransmitType.Auto;
  /**
   * 参数声明为 Keep 会在 restore 和 helper 中生成代码，用于 Activity#onSaveInstanceState
   */
  boolean keep() default false;
}
