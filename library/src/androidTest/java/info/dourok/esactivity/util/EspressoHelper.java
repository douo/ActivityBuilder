package info.dourok.esactivity.util;

import android.app.Activity;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import info.dourok.esactivity.function.Consumer;
import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

/**
 * @author tiaolins
 * @date 2018/3/2.
 */
public class EspressoHelper {
  private EspressoHelper() {}

  private static Activity currentActivity;

  public static synchronized Activity getActivityInstance() {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              Collection<Activity> resumedActivities =
                  ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
              if (resumedActivities.iterator().hasNext()) {
                currentActivity = resumedActivities.iterator().next();
              }
            });

    return currentActivity;
  }
}
