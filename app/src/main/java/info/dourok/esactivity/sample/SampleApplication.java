package info.dourok.esactivity.sample;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by tiaolins on 2017/8/6.
 */

public class SampleApplication extends Application {
  private static final String TAG = "SampleApplication";

  @Override public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.d(TAG, "onActivityCreated:" + activity + "\n" + bundle);
      }

      @Override public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted:" + activity);
      }

      @Override public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed:" + activity);
      }

      @Override public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused:" + activity);
      }

      @Override public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped:" + activity);
      }

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.d(TAG, "onActivitySaveInstanceState:" + activity + "\n" + bundle);
      }

      @Override public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed:" + activity);
      }
    });
  }
}
