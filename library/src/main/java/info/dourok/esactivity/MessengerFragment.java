package info.dourok.esactivity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/**
 * @author tiaolins
 * @date 2017/8/5
 */
public class MessengerFragment extends Fragment {
  public static final String FRAGMENT_TAG = "info.dourok.esactivity:MessengerFragment";
  private static final String TAG = "MessengerFragment";
  private BiConsumer<Integer, Intent> consumer;
  private SparseArray<BaseResultConsumer<?>> consumerMap;
  private ArrayList<Integer> uselessRefMapKeys = new ArrayList<>();

  public MessengerFragment() {
    consumerMap = new SparseArray<>();
    setRetainInstance(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    // 清空无用的 RefMap
    // Builder 启动 Activity 后，再回到当前 Activity。RefMap 就已经没用了
    for (Integer key : uselessRefMapKeys) {
      RefManager.getInstance().clearRefs(key);
    }
    uselessRefMapKeys.clear();
  }

  /** 注册无用的 RefMapKey，{@link MessengerFragment} 需要对无用 RefMap 进行清理 */
  void registerUselessReMapKey(int keyOfMap) {
    uselessRefMapKeys.add(keyOfMap);
  }

  private int addConsumer(BaseResultConsumer<?> consumer) {
    int requestCode = generateRequestCode();
    consumerMap.put(requestCode, consumer);
    return requestCode;
  }

  private int generateRequestCode() {
    int key;
    do {
      // request code 取值 [0x1000,0xFFFF)
      key = (int) (Math.random() * 0xEFFF + 0x1000);
    } while (consumerMap.get(key) != null);
    return key;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (consumerMap.get(requestCode) != null) {
      consumerMap.get(requestCode).accept(getActivity(), resultCode, data);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  public void startActivityForResult(
      Intent intent, BaseResultConsumer<?> consumer, Bundle options) {
    int requestCode = addConsumer(consumer);
    startActivityForResult(intent, requestCode, options);
  }

  public void startActivityForResult(Intent intent, BaseResultConsumer<?> consumer) {
    int requestCode = addConsumer(consumer);
    startActivityForResult(intent, requestCode);
  }

  public static MessengerFragment fragmentFor(Activity activity) {
    FragmentManager fm = activity.getFragmentManager();
    if (fm.isDestroyed()) {
      throw new IllegalStateException("Activity already destroyed");
    }
    Fragment fragmentByTag = fm.findFragmentByTag(FRAGMENT_TAG);
    if (fragmentByTag != null && !(fragmentByTag instanceof MessengerFragment)) {
      throw new IllegalStateException(
          "Unexpected " + "fragment instance was returned by FRAGMENT_TAG");
    }
    MessengerFragment fragment = (MessengerFragment) fragmentByTag;
    if (fragment == null) {
      fragment = new MessengerFragment();
      fm.beginTransaction().add(fragment, FRAGMENT_TAG).commitAllowingStateLoss();
      // 接下来需要立刻通过这个 Fragment 来启动 Activity，所以需要立刻添加到 Activity 中
      fm.executePendingTransactions();
    }
    return fragment;
  }
}
