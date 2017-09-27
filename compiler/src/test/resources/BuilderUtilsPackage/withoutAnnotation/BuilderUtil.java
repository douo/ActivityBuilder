package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import info.dourok.esactivity.BaseActivityBuilder;
import java.lang.Class;
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BuilderUtil {
  private static final HashMap<Class<? extends Activity>, Class<? extends BaseActivityBuilder>> sBuilderMap = new HashMap<>();

  static {
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
}