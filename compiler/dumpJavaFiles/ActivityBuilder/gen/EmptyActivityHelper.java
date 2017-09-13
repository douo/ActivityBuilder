package test;

import android.content.Intent;
import android.os.Bundle;

class EmptyActivityHelper {
  private final EmptyActivity activity;

  EmptyActivityHelper(EmptyActivity activity) {
    this.activity = activity;
  }

  void inject() {
    Intent intent = activity.getIntent();
  }

  void restore(Bundle savedInstanceState) {
  }

  void save(Bundle savedInstanceState) {
  }
}