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
  MessengerFragment fragment;
  private IntentWrapper<T> intentWrapper;
  BaseResultConsumer<A> consumer;
  private Map<String, Object> refMap;

  public BaseActivityBuilder(A activity) {
    context = activity;
    fragment = MessengerFragment.addIfNeed(activity);
    consumer = new BaseResultConsumer<>();
  }

  public void setIntent(Intent intent) {
    intentWrapper = new IntentWrapper<>(self(), intent);
  }

  public Intent getIntent() {
    if (intentWrapper == null) {
      setIntent(new Intent());
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
    consumer.biConsumer = resultConsumer;
    return self();
  }

  public T forCancel(BiConsumer<A, Intent> cancelConsumer) {
    consumer.cancelConsumer = cancelConsumer;
    return self();
  }

  public T forOk(BiConsumer<A, Intent> okConsumer) {
    consumer.okConsumer = okConsumer;
    return self();
  }

  public T result(BiConsumer<Integer, Intent> resultConsumer) {
    consumer.biConsumer = (context, i, intent) -> resultConsumer.accept(i, intent);
    return self();
  }

  public T forCancel(Consumer<Intent> cancelConsumer) {
    consumer.cancelConsumer = (context, intent) -> cancelConsumer.accept(intent);
    return self();
  }

  public T forOk(Consumer<Intent> okConsumer) {
    consumer.okConsumer = (context, intent) -> okConsumer.accept(intent);
    return self();
  }

  public void start() {
    if (consumer.hasConsumer()) {
      fragment.startActivityForResult(getIntent(), consumer);
    } else {
      fragment.startActivity(getIntent());
    }
  }

  public Map<String, Object> getRefMap() {
    if (refMap == null) {
      refMap = RefManager.getInstance().getRefMap(this);
    }
    return refMap;
  }
}
