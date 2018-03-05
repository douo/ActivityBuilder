package info.dourok.esactivity.reference;

import android.app.Activity;
import info.dourok.esactivity.LambdaStateCallback;
import java.lang.reflect.Field;

/**
 * 用于更新 lambda 表达式捕获的与 Activity 相关的对象的引用。 一些 UI 相关的组件，比如 View、Fragment 或 Activity 本身，在 Activity
 * 重建后会被重新创建。 而 Closure 捕获的还是旧的对象引用，需要在执行前进行更新。
 *
 * @author tiaolins
 * @date 2018/3/2.
 */
public abstract class AbstractActivityReferenceUpdater implements LambdaStateCallback {

  @Override
  public void beforeAdd(Object lambda) {}

  @Override
  public void beforeExecute(Activity activity, Object lambda) {
    Class zlass = lambda.getClass();
    for (Field field : zlass.getDeclaredFields()) {
      field.setAccessible(true);
      if (isMatch(field)) {
        try {
          Object oldObject = field.get(lambda);
          Object object = findNewObject(activity, field, lambda, oldObject);
          if (object != null) {
            reassignFinalField(field, lambda, object);
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
   * TODO 需区分找不到和没必要更新两种情况
   *
   * @param activity 新的 Activity 对象
   * @param oldObject 旧对象
   * @return 找不到返回 null
   */
  protected abstract Object findNewObject(Activity activity, Field field, Object lambda,
      Object oldObject);


  /**
   * 确定当前捕获的类型该更新器可以出来的类型。
   *
   * @param field 用检查是否匹配的字段
   * @return 是该更新器可以处理的类型
   */
  protected abstract boolean isMatch(Field field);

  @Override
  public void afterExecute(Activity activity, Object lambda) {}

  /** 更新 final 引用的值 */
  private void reassignFinalField(Field field, Object object, Object value)
      throws IllegalAccessException, NoSuchFieldException {
    field.setAccessible(true);
    // ART 虚拟机不需要更新 modifier 就能直接修改
    // Field modifiersField = Field.class.getDeclaredField("modifiers");
    // modifiersField.setAccessible(true);
    // modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    field.set(object, value);
  }
}
