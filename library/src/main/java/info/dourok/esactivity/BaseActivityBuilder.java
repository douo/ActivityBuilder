package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by tiaolins on 2017/8/15.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class BaseActivityBuilder<T extends BaseActivityBuilder<T>> implements BaseBuilder {
  Activity context;
  MessengerFragment fragment;
  IntentWrapper<T> intentWrapper;
  InnerConsumer consumer;

  public BaseActivityBuilder(Activity activity) {
    context = activity;
    fragment = MessengerFragment.addIfNeed(activity);
    consumer = new InnerConsumer();
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

  public T result(BiConsumer<Integer, Intent> resultConsumer) {
    consumer.biConsumer = resultConsumer;
    return self();
  }

  protected abstract T self();

  public T forCancel(Consumer<Intent> cancelConsumer) {
    consumer.cancelConsumer = cancelConsumer;
    return self();
  }

  public T forOk(Consumer<Intent> okConsumer) {
    consumer.okConsumer = okConsumer;
    return self();
  }

  public void start() {
    if (consumer.hasConsumer()) {
      fragment.startActivityForResult(getIntent(), consumer);
    } else {
      fragment.startActivity(getIntent());
    }
  }
}
