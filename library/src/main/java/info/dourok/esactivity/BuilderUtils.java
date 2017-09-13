package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by tiaolins on 2017/9/13.
 */

public class BuilderUtils {
  private static Map<Class<? extends Activity>, Class<? extends BaseActivityBuilder>> sBuilderMap;
  static {
    sBuilderMap.put(Activity.class,BaseActivityBuilder.class);
  }

  public static <A extends Activity, T extends BaseActivityBuilder<T, A>> T smallCreate(A activity,
      Class<? extends Activity> clazz) {
    if (sBuilderMap.containsKey(clazz)) {
      Class<? extends BaseActivityBuilder> c = sBuilderMap.get(clazz);
      try {
        Constructor cc = c.getConstructor(Activity.class);
        return (T) cc.newInstance(activity);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return (T) createBuilder(activity, clazz);
  }

  public static <A extends Activity> BaseActivityBuilder<?, A> createBuilder(A activity,
      Class<? extends Activity> clazz) {
    return BaseActivityBuilder.create(activity, clazz);
  }

  public static <A extends Activity> BaseActivityBuilder<?, A> createBuilder(A activity,
      Intent intent) {
    return BaseActivityBuilder.create(activity, intent);
  }

}
