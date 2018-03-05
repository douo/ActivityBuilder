package info.dourok.esactivity.reference;

import android.app.Activity;
import android.view.View;
import java.lang.reflect.Field;

/**
 * @author tiaolins
 * @date 2018/3/3.
 */
public class ViewReferenceUpdater extends AbstractActivityReferenceUpdater {
  @Override
  protected Object findNewObject(Activity activity, Field field, Object lambda, Object oldObject) {
    View view = (View) oldObject;
    if (view.getContext() != activity) {
      View newView = null;
      if (view.getId() != View.NO_ID) {
        newView = activity.findViewById(view.getId());
      }
      return newView;
    }
    return null;
  }

  @Override
  protected boolean isMatch(Field field) {
    return View.class.isAssignableFrom(field.getType());
  }
}
