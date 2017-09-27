package info.dourok.esactivity;

import javax.lang.model.SourceVersion;

/**
 * 结果的参数
 * Created by tiaolins on 2017/9/1.
 */
public @interface ResultParameter {

  /**
   * 必须是合法的变量名，见{@link SourceVersion#isName(CharSequence)}
   *
   * @return 参数名称
   */
  String name();

  /**
   * @return 传递参数的类型
   */
  Class<?> type();

  TransmitType transmit() default TransmitType.AUTO;
}
