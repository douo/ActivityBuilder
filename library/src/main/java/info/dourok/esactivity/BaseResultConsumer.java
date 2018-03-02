package info.dourok.esactivity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;
import info.dourok.esactivity.function.BiConsumer;
import info.dourok.esactivity.function.Consumer;
import info.dourok.esactivity.function.TriConsumer;
import java.lang.reflect.Field;

/**
 * @author tiaolins
 * @param <A> the starter activity
 * @date 2017/8/6
 */
public class BaseResultConsumer<A extends Activity>
    implements TriConsumer<Activity, Integer, Intent> {
  private BiConsumer<Integer, Intent> biConsumer;
  private Consumer<Intent> okConsumer;
  private Consumer<Intent> cancelConsumer;

  @Override
  public final void accept(Activity context, Integer result, @Nullable Intent intent) {
    A starter = (A) context;
    if (!handleResult(starter, result, intent)) {
      if (result == Activity.RESULT_OK && okConsumer != null) {
        doCheck(context, result);
        okConsumer.accept(intent);
      }
      if (result == Activity.RESULT_CANCELED && cancelConsumer != null) {
        doCheck(context, cancelConsumer);
        cancelConsumer.accept(intent);
      }
      if (biConsumer != null) {
        doCheck(context, cancelConsumer);
        biConsumer.accept(result, intent);
      }
    }
    RefManager.getInstance().clearRefs(intent);
  }

  void setBiConsumer(BiConsumer<Integer, Intent> biConsumer) {
    this.biConsumer = biConsumer;
  }

  void setOkConsumer(Consumer<Intent> okConsumer) {
    this.okConsumer = okConsumer;
  }

  void setCancelConsumer(Consumer<Intent> cancelConsumer) {
    this.cancelConsumer = cancelConsumer;
  }

  protected void doCheck(Activity activity, Object lambda) {
    try {
      // 可以先标记为是否有 Activity 引用
      checkActivityReference(activity, lambda);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }
  /** 确保捕获了 Activity 实例的 lambda 对象引用的是最新的 Activity 实例，如果不是会尝试更新 Activity 引用 */
  private void checkActivityReference(Activity activity, Object lambda)
      throws IllegalAccessException, NoSuchFieldException {
    Class zlass = lambda.getClass();
    for (Field field : zlass.getDeclaredFields()) {
      field.setAccessible(true);
      // 对四种类型做特殊处理 1. Activity 2. Fragment 3. View 4. support fragment
      if (Activity.class.isAssignableFrom(field.getType())) {
        // Activity 直接替换
        Activity originActivity = (Activity) field.get(lambda);
        if (originActivity != activity) {
          // Activity 被重建了需要更新引用
          reassignFinalField(field, lambda, activity);
        }
      } else if (Fragment.class.isAssignableFrom(field.getType())) {
        // Fragment
        Fragment fragment = (Fragment) field.get(lambda);
        // fragment detach 之后 getActivity 应返回为空
        if (fragment.getActivity() == null || fragment.getActivity() != activity) {
          // 尝试通过 id 或者 tag 来查找
          Fragment newFragment = activity.getFragmentManager().findFragmentById(fragment.getId());
          if (newFragment == null) {
            newFragment = activity.getFragmentManager().findFragmentByTag(fragment.getTag());
          }
          if (newFragment != null) {
            reassignFinalField(field, lambda, newFragment);
          } else {
            // TODO
            throw new RuntimeException("Activity 重建后找不到 fragment:" + fragment);
          }
        }
      } else if (View.class.isAssignableFrom(field.getType())) {
        // View
        View view = (View) field.get(lambda);
        if (view.getContext() != activity) {
          View newView = null;
          if (view.getId() != View.NO_ID) {
            newView = activity.findViewById(view.getId());
          }
          if (newView != null) {
            reassignFinalField(field, lambda, newView);
          } else {
            throw new RuntimeException("Activity 重建后找不到 View:" + view);
          }
        }
      }
      // TODO support fragment
    }
  }

  private void reassignFinalField(Field field, Object object, Object value)
      throws IllegalAccessException, NoSuchFieldException {
    field.setAccessible(true);
    // Field modifiersField = Field.class.getDeclaredField("modifiers");
    // modifiersField.setAccessible(true);
    // modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    field.set(object, value);
  }

  protected boolean handleResult(A context, int result, Intent intent) {
    return false;
  }

  public boolean hasConsumer() {
    return biConsumer != null || okConsumer != null || cancelConsumer != null;
  }

  void detach() {}
}
