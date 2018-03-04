package info.dourok.esactivity.reference;

import android.app.Activity;
import info.dourok.esactivity.ClosureStateCallback;
import java.lang.reflect.Field;

/**
 * 用于更新 clousure 捕获的与 Activity 相关的对象的引用。 一些 UI 相关的组件，比如 View、Fragment 或 Activity 本身，在 Activity
 * 重建后会被重新创建。 而 Closure 捕获的还是旧的对象引用，
 *
 * @author tiaolins
 * @date 2018/3/2.
 */
public abstract class AbstractActivityReferenceUpdater implements ClosureStateCallback {

  @Override
  public void beforeAdd(Object closure) {}

  @Override
  public void beforeExecute(Activity activity, Object closure) {
    Class zlass = closure.getClass();
    for (Field field : zlass.getDeclaredFields()) {
      field.setAccessible(true);
      if (isMatch(field)) {
        try {
          Object oldObject = field.get(closure);
          Object object = findNewObject(activity, field, closure, oldObject);
          if (object != null) {
            reassignFinalField(field, closure, object);
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (NoSuchFieldException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 尝试找回新对象
   *
   * @param activity 新的 Activity 对象
   * @param oldObject 旧对象
   * @return 找不到返回 null
   */
  protected abstract Object findNewObject(Activity activity, Field field, Object closure,
      Object oldObject);


  /**
   * 确定当前捕获的类型该更新器可以出来的类型。
   *
   * @param field 用检查是否匹配的字段
   * @return 是该更新器可以处理的类型
   */
  protected abstract boolean isMatch(Field field);

  @Override
  public void afterExecute(Activity activity, Object closure) {}

  /** 更新 final 引用的值 */
  void reassignFinalField(Field field, Object object, Object value)
      throws IllegalAccessException, NoSuchFieldException {
    field.setAccessible(true);
    // ART 虚拟机不需要更新 modifier 就能直接修改
    // Field modifiersField = Field.class.getDeclaredField("modifiers");
    // modifiersField.setAccessible(true);
    // modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    field.set(object, value);
  }
}
