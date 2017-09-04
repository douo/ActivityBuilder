package info.dourok.esactivity;

import java.lang.reflect.Type;

/**
 * 结果的参数
 * Created by tiaolins on 2017/9/1.
 */
public @interface ResultParameter {

  /**
   *
   * @return 参数名称
   */
  String name();

  /**
   * @return 传递参数的类型
   */
  Class<?> type();

  TransmitType transmit() default TransmitType.Auto;
}
