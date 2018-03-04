package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import info.dourok.esactivity.function.BiConsumer;
import info.dourok.esactivity.function.Consumer;
import info.dourok.esactivity.function.TriConsumer;
import info.dourok.esactivity.reference.ActivityReferenceUpdater;
import info.dourok.esactivity.reference.FragmentReferenceUpdater;
import info.dourok.esactivity.reference.ViewReferenceUpdater;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tiaolins
 * @param <A> the starter activity
 * @date 2017/8/6
 */
public class BaseResultConsumer<A extends Activity>
    implements TriConsumer<Activity, Integer, Intent>, ClosureStateCallback {
  private BiConsumer<Integer, Intent> biConsumer;
  private Consumer<Intent> okConsumer;
  private Consumer<Intent> cancelConsumer;
  private List<ClosureStateCallback> callbackList = new ArrayList<>(4);

  {
    callbackList.add(new ActivityReferenceUpdater());
    callbackList.add(new FragmentReferenceUpdater());
    callbackList.add(new ViewReferenceUpdater());
  }
  /**
   * {@link MessengerFragment} 会调用 request code 相同的该方法。
   *
   * @param context 请求者 Activity
   * @param result 同 onActivityResult 方法
   * @param intent 同 onActivityResult 方法
   */
  @Override
  public final void accept(Activity context, Integer result, @Nullable Intent intent) {
    A starter = (A) context;
    if (!handleResult(starter, result, intent)) {
      if (result == Activity.RESULT_OK && okConsumer != null) {
        beforeExecute(context, okConsumer);
        okConsumer.accept(intent);
        afterExecute(context, okConsumer);
      }
      if (result == Activity.RESULT_CANCELED && cancelConsumer != null) {
        beforeExecute(context, cancelConsumer);
        cancelConsumer.accept(intent);
        afterExecute(context, cancelConsumer);
      }
      if (biConsumer != null) {
        beforeExecute(context, biConsumer);
        biConsumer.accept(result, intent);
        afterExecute(context, biConsumer);
      }
    }
    RefManager.getInstance().clearRefs(intent);
  }

  void setBiConsumer(BiConsumer<Integer, Intent> biConsumer) {
    beforeAdd(biConsumer);
    this.biConsumer = biConsumer;
  }

  void setOkConsumer(Consumer<Intent> okConsumer) {
    beforeAdd(okConsumer);
    this.okConsumer = okConsumer;
  }

  void setCancelConsumer(Consumer<Intent> cancelConsumer) {
    beforeAdd(cancelConsumer);
    this.cancelConsumer = cancelConsumer;
  }

  @Override
  public void beforeAdd(Object closure) {
    for (ClosureStateCallback callback : callbackList) {
      callback.beforeAdd(closure);
    }
  }

  @Override
  public void beforeExecute(Activity activity, Object closure) {
    for (ClosureStateCallback callback : callbackList) {
      callback.beforeExecute(activity, closure);
    }
  }

  @Override
  public void afterExecute(Activity activity, Object closure) {
    for (ClosureStateCallback callback : callbackList) {
      callback.afterExecute(activity, closure);
    }
  }

  public void addClosureStateCallback(ClosureStateCallback callback) {
    callbackList.add(callback);
  }

  protected boolean handleResult(A context, int result, Intent intent) {
    return false;
  }

  public boolean hasConsumer() {
    return biConsumer != null || okConsumer != null || cancelConsumer != null;
  }

  void detach() {}
}
