package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import info.dourok.esactivity.function.BiConsumer;
import info.dourok.esactivity.function.Consumer;
import info.dourok.esactivity.function.TriConsumer;
import java.util.Map;

/**
 * Created by tiaolins on 2017/8/15.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class BaseActivityBuilder<T extends BaseActivityBuilder<T, A>, A extends Activity>
    implements BaseBuilder {
  A context;
  protected MessengerFragment fragment;
  private IntentWrapper<T> intentWrapper;
  protected BaseResultConsumer<A> consumer;
  private Map<String, Object> refMap;

  public BaseActivityBuilder(A activity) {
    context = activity;
    fragment = MessengerFragment.addIfNeed(activity);
  }

  public BaseActivityBuilder(A activity, Intent intent) {
    this(activity);
    setIntent(intent);
  }

  protected BaseResultConsumer<A> getConsumer() {
    if (consumer == null) {
      consumer = new BaseResultConsumer<>();
    }
    return consumer;
  }

  /**
   * intent 应该在构造函数的时候就初始化
   * 因为 Intent 可能会和 RefMap 绑定在一起
   */
  public void setIntent(Intent intent) {
    if (intentWrapper != null) {
      RefManager.getInstance().rebindRefMap(intentWrapper.getIntent(), intent);
    }
    intentWrapper = new IntentWrapper<>(self(), intent);
  }

  public Intent getIntent() {
    if (intentWrapper == null) {
      intentWrapper = new IntentWrapper<>(self(), new Intent());
    }
    return intentWrapper.getIntent();
  }

  @Override public IntentWrapper<T> asIntent() {
    if (intentWrapper == null) {
      setIntent(new Intent());
    }
    return intentWrapper;
  }

  protected abstract T self();

  public T result(TriConsumer<A, Integer, Intent> resultConsumer) {
    getConsumer().biConsumer = resultConsumer;
    return self();
  }

  public T forCancel(BiConsumer<A, Intent> cancelConsumer) {
    getConsumer().cancelConsumer = cancelConsumer;
    return self();
  }

  public T forOk(BiConsumer<A, Intent> okConsumer) {
    getConsumer().okConsumer = okConsumer;
    return self();
  }

  public T result(BiConsumer<Integer, Intent> resultConsumer) {
    getConsumer().biConsumer = (context, i, intent) -> resultConsumer.accept(i, intent);
    return self();
  }

  public T forCancel(Consumer<Intent> cancelConsumer) {
    getConsumer().cancelConsumer = (context, intent) -> cancelConsumer.accept(intent);
    return self();
  }

  public T forOk(Consumer<Intent> okConsumer) {
    getConsumer().okConsumer = (context, intent) -> okConsumer.accept(intent);
    return self();
  }

  public void start() {
    if (getConsumer().hasConsumer()) {
      fragment.startActivityForResult(getIntent(), getConsumer());
    } else {
      fragment.startActivity(getIntent());
    }
    if (hasRefMap()) {
      fragment.registerUselessReMapKey(RefManager.getKeyOfMap(getIntent()));
    }
  }

  public boolean hasRefMap() {
    return refMap != null;
  }

  public Map<String, Object> getRefMap() {
    if (refMap == null) {
      refMap = RefManager.getInstance().getOrCreateRefMap(this);
    }
    return refMap;
  }
}
