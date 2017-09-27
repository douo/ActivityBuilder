package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import info.dourok.esactivity.function.BiConsumer;
import info.dourok.esactivity.function.TriConsumer;


public  class BaseResultConsumer<A extends Activity>
    implements TriConsumer<Activity, Integer, Intent> {
  TriConsumer<A, Integer, Intent> biConsumer;
  BiConsumer<A, Intent> okConsumer;
  BiConsumer<A, Intent> cancelConsumer;

  @Override public final void accept(Activity context, Integer result, Intent intent) {
    //
  }

  protected boolean handleResult(A context, int result, Intent intent) {
    return false;
  }

  public boolean hasConsumer() {
    return biConsumer != null || okConsumer != null || cancelConsumer != null;
  }
}
