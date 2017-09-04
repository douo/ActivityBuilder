package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import info.dourok.esactivity.function.BiConsumer;
import info.dourok.esactivity.function.TriConsumer;

/**
 * @param <A> the starter activity
 * Created by tiaolins on 2017/8/6.
 */

public class BaseResultConsumer<A extends Activity>
    implements TriConsumer<Activity, Integer, Intent> {
  TriConsumer<A, Integer, Intent> biConsumer;
  BiConsumer<A, Intent> okConsumer;
  BiConsumer<A,Intent> cancelConsumer;

  @Override public final void accept(Activity context, Integer result, Intent intent) {
    A starter = (A) context;
    if (!handleResult(starter, result, intent)) {
      if (result == Activity.RESULT_OK && okConsumer != null) {
        okConsumer.accept(starter, intent);
      }
      if (result == Activity.RESULT_CANCELED && cancelConsumer != null) {
        cancelConsumer.accept(starter, intent);
      }
      if (biConsumer != null) {
        biConsumer.accept(starter, result, intent);
      }
    }
    RefManager.getInstance().clearRefs(context);
  }

  protected boolean handleResult(A context, Integer result, Intent intent) {
    return false;
  }

  public boolean hasConsumer() {
    return biConsumer != null || okConsumer != null || cancelConsumer != null;
  }
}