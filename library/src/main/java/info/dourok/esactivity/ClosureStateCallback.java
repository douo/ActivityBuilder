package info.dourok.esactivity;

import android.app.Activity;
import info.dourok.esactivity.reference.AbstractActivityReferenceUpdater;

/**
 * 参考 {@link AbstractActivityReferenceUpdater}
 * @author tiaolins
 * @date 2018/3/2.
 */
public interface ClosureStateCallback {
  /**
   * 在 closure 被添加前调用
   */
  void beforeAdd(Object closure);

  /**
   * 在 closure 被调用前调用。onActivityResult 将会执行这个 lambda 表达式
   * @param activity starter activity
   */
  void beforeExecute(Activity activity, Object closure);

  /**
   * 在 closure 被调用后调用
   */
  void afterExecute(Activity activity, Object closure);
}
