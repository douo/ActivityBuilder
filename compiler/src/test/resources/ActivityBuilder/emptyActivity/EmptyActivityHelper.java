package test;

import android.content.Intent;
import android.os.Bundle;

public class EmptyActivityHelper {
  private final EmptyActivity activity;

  public EmptyActivityHelper(EmptyActivity activity) {
    this(activity, false)
  }

  public EmptyActivityHelper(EmptyActivity activity, boolean autoInject) {
    this.activity = activity;
    if (autoInject) {
      inject();
    }
  }

  void inject() {
    Intent intent = activity.getIntent();
  }

  void restore(Bundle savedInstanceState) {
  }

  void save(Bundle savedInstanceState) {
  }
}