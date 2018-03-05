package info.dourok.esactivity;

import android.app.Activity;
import info.dourok.esactivity.reference.AbstractActivityReferenceUpdater;

/**
 * 参考 {@link AbstractActivityReferenceUpdater}
 * @author tiaolins
 * @date 2018/3/2.
 */
public interface LambdaStateCallback {
  /**
   * 在 lambda 被添加前调用
   * @param lambda lambda expression
   */
  void beforeAdd(Object lambda);

  /**
   * 在 lambda 被调用前调用。onActivityResult 将会执行这个 lambda 表达式
   * @param activity starter activity
   * @param lambda lambda expression
   */
  void beforeExecute(Activity activity, Object lambda);

  /**
   * 在 lambda 被调用后调用
   * @param activity starter activity
   * @param lambda lambda expression
   */
  void afterExecute(Activity activity, Object lambda);
}
