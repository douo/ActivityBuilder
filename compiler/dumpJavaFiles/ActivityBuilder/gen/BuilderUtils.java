package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import test.EmptyActivityBuilder;
import test.EmptyActivityHelper;
import test.EmptyActivity;
import java.lang.Class;
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BuilderUtils {
  private static final HashMap<Class<? extends Activity>, Class<? extends BaseActivityBuilder>> sBuilderMap = new HashMap<>();

  static {
    sBuilderMap.put(EmptyActivity.class,EmptyActivityBuilder.class);
  }

  public static <A extends Activity> BaseActivityBuilder<? extends BaseActivityBuilder, A> createBuilder(A activity,
      Class<? extends Activity> clazz) {
    return BaseActivityBuilder.create(activity, clazz);
  }

  public static <A extends Activity> BaseActivityBuilder<? extends BaseActivityBuilder, A> createBuilder(A activity,
      Intent intent) {
    return BaseActivityBuilder.create(activity, intent);
  }

  public static <A extends Activity, T extends BaseActivityBuilder<T, A>> T smallCreate(A activity,
      Class<? extends Activity> clazz) {
    if (sBuilderMap.containsKey(clazz)) {
      try {
        return (T) sBuilderMap.get(clazz).getMethod("create", Activity.class).invoke(null,activity);
      }
      catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return (T) createBuilder(activity,clazz);
  }

  public static EmptyActivityHelper createHelper(EmptyActivity activity) {
    return new EmptyActivityHelper(activity);
  }
}
