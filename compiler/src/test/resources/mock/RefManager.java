package info.dourok.esactivity;

import android.app.Activity;
import android.content.Intent;

public class RefManager {
  public static RefManager getInstance() {
    return null;
  }

  public <T> T get(Activity context, String key) {
    return null;
  }

  public <T> T get(Intent intent, String key) {
    return null;
  }

  /**
   * 用于 setResult 的 Intent
   */
  public <T> void put(Intent intent, String key, T value) {

  }

  public <T> void put(BaseBuilder builder, String key, T value) {

  }
}
