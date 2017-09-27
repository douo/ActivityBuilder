package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import java.util.Map;

public abstract class BaseActivityBuilder<T extends BaseActivityBuilder<T, A>, A extends Activity>
    implements BaseBuilder {
  protected BaseResultConsumer<A> consumer;

  public BaseActivityBuilder(A context) {

  }

  public void setIntent(Intent intent) {
  }

  public Intent getIntent() {
    return null;
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

  public Map<String, Object> getRefMap() {
    return null;
  }
}
