package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by tiaolins on 2017/8/6.
 */

@RequiresApi(api = Build.VERSION_CODES.N) class InnerConsumer
    implements BiConsumer<Integer, Intent> {
  BiConsumer<Integer, Intent> biConsumer;
  Consumer<Intent> okConsumer;
  Consumer<Intent> cancelConsumer;

  @Override public void accept(Integer result, Intent intent) {
    if (result == Activity.RESULT_OK && okConsumer != null) {
      okConsumer.accept(intent);
    }
    if (result == Activity.RESULT_CANCELED && cancelConsumer != null) {
      cancelConsumer.accept(intent);
    }
    if (biConsumer != null) {
      biConsumer.accept(result, intent);
    }
  }

  public boolean hasConsumer() {
    return biConsumer != null || okConsumer != null || cancelConsumer != null;
  }
}
