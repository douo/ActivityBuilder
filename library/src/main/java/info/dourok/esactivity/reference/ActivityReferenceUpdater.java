package info.dourok.esactivity.reference;

import android.app.Activity;
import java.lang.reflect.Field;

/**
 * @author tiaolins
 * @date 2018/3/3.
 */
public class ActivityReferenceUpdater extends AbstractActivityReferenceUpdater {
  @Override
  protected Object findNewObject(Activity activity, Field field, Object closure, Object oldObject) {
    return activity;
  }

  @Override
  protected boolean isMatch(Field field) {
    return Activity.class.isAssignableFrom(field.getType());
  }
}
