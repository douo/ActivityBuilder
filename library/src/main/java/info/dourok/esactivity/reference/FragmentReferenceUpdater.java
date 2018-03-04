package info.dourok.esactivity.reference;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import info.dourok.esactivity.function.Function;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.NO_ID;

/**
 * @author tiaolins
 * @date 2018/3/3.
 */
public class FragmentReferenceUpdater extends AbstractActivityReferenceUpdater {
  private Map<Field, Function<Activity, Object>> map = new HashMap<>(4);

  /** 缓存 Fragment Finder. fragment makeInactive 之后状态会被清空，所以得提前对 Fragment 的 id 或 tag 进行保存。 */
  @Override
  public void beforeAdd(Object closure) {
    Class zlass = closure.getClass();
    for (Field field : zlass.getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Function<Activity, Object> fragmentFinder = null;
        if (Fragment.class.isAssignableFrom(field.getType())) {
          Fragment fragment = (Fragment) field.get(closure);
          if (fragment.getId() != NO_ID) {
            int id = fragment.getId();
            fragmentFinder =
                (Activity activity) -> activity.getFragmentManager().findFragmentById(id);
          } else if (fragment.getTag() != null) {
            String tag = fragment.getTag();
            fragmentFinder =
                (Activity activity) -> activity.getFragmentManager().findFragmentByTag(tag);
          }
          // Support Fragment
        } else if (android.support.v4.app.Fragment.class.isAssignableFrom(field.getType())) {
          android.support.v4.app.Fragment fragment =
              (android.support.v4.app.Fragment) field.get(closure);
          if (fragment.getId() != NO_ID) {
            int id = fragment.getId();
            fragmentFinder =
                (Activity activity) ->
                    ((FragmentActivity) activity).getSupportFragmentManager().findFragmentById(id);
          } else if (fragment.getTag() != null) {
            String tag = fragment.getTag();
            fragmentFinder =
                (Activity activity) ->
                    ((FragmentActivity) activity)
                        .getSupportFragmentManager()
                        .findFragmentByTag(tag);
          }
        }
        if (fragmentFinder != null) {
          map.put(field, fragmentFinder);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected Object findNewObject(Activity activity, Field field, Object closure, Object oldObject) {
    final boolean fragmentNeedUpdate =
        oldObject instanceof Fragment
            // Fragment detach 之后 Activity 应该为空
            && (((Fragment) oldObject).getActivity() == null
                || ((Fragment) oldObject).getActivity() != activity);
    final boolean supportFragmentNeedUpdate =
        oldObject instanceof android.support.v4.app.Fragment
            && (((android.support.v4.app.Fragment) oldObject).getActivity() == null
                || ((android.support.v4.app.Fragment) oldObject).getActivity() != activity);
    if (fragmentNeedUpdate || supportFragmentNeedUpdate) {
      return map.get(field).apply(activity);
    } else {
      return null;
    }
  }

  @Override
  protected boolean isMatch(Field field) {
    // XXX 一个 Lambda 表达式只有一个实例（断言）
    // 所以用 field 就能够确保唯一性
    return map.containsKey(field);
  }
}
