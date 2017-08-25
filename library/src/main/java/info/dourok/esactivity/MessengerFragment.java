package info.dourok.esactivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

/**
 * Created by tiaolins on 2017/8/5.
 */

public class MessengerFragment extends Fragment {
  public static final String FRAGMENT_TAG = "info.dourok.esactivity:MessengerFragment";
  private static final String TAG = "MessengerFragment";
  private BiConsumer<Integer, Intent> consumer;
  private SparseArray<InnerConsumer> consumerMap;

  public MessengerFragment() {
    consumerMap = new SparseArray<>();
    setRetainInstance(true);
  }

  private int addConsumer(InnerConsumer consumer) {
    int requestCode = generateRequestCode();
    consumerMap.put(requestCode, consumer);
    return requestCode;
  }

  private int generateRequestCode() {
    int key;
    do {
      key = (int) (Math.random() * 0xEFFF + 0x1000);
    } while (consumerMap.get(key) != null);
    return key;
  }

  @TargetApi(Build.VERSION_CODES.N) @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult:" + requestCode + " " + resultCode);
    InnerConsumer consumer = consumerMap.get(requestCode);
    try {
      Field f = consumer.okConsumer.getClass().getField("arg$1");
      Log.d(TAG, f.toGenericString());

      //MethodHandles.Lookup lookup = MethodHandles.lookup();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    if (consumer != null) {
      consumer.accept(resultCode, data);
    }
  }

  public static MessengerFragment addIfNeed(Activity activity) {
    FragmentManager fm = activity.getFragmentManager();
    MessengerFragment fragment =
        (MessengerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    if (fragment == null) {
      fragment = new MessengerFragment();
      fm.beginTransaction().add(fragment, FRAGMENT_TAG)
          .commitAllowingStateLoss();
      fm.executePendingTransactions();
    }
    return fragment;
  }

  public void startActivityForResult(Intent intent, InnerConsumer consumer) {
    int requestCode = addConsumer(consumer);
    startActivityForResult(intent, requestCode);
  }
}
