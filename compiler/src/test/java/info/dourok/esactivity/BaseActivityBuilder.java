package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;

public abstract class BaseActivityBuilder<T extends BaseActivityBuilder<T, A>, A extends Activity> {

  public BaseActivityBuilder(A context) {

  }

  public void setIntent(Intent intent) {
  }

  protected BaseResultConsumer<A> getConsumer() {
    return null;
  }

  protected abstract T self();

  public static <A extends Activity> BaseActivityBuilder<?, A> create(A activity,
      Class<? extends Activity> clazz) {
    return null;
  }

  public static <A extends Activity> BaseActivityBuilder<?, A> create(A activity,
      Intent intent) {
    return null;
  }
}
