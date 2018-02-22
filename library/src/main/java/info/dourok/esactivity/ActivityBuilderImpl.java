package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import java.lang.Override;

class ActivityBuilderImpl<A extends Activity>
    extends BaseActivityBuilder<ActivityBuilderImpl<A>, A> {
  ActivityBuilderImpl(A activity, Class<? extends Activity> clazz) {
    super(activity);
    setIntent(new Intent(activity, clazz));
  }

  ActivityBuilderImpl(A activity, Intent intent) {
    super(activity);
    setIntent(intent);
  }

  @Override
  protected ActivityBuilderImpl<A> self() {
    return this;
  }
}
