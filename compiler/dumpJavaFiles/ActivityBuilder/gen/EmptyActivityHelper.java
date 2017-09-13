import android.content.Intent;
import android.os.Bundle;

public class EmptyActivityHelper {
  void inject(EmptyActivity activity) {
    Intent intent = activity.getIntent();
  }

  void restore(EmptyActivity activity, Bundle savedInstanceState) {
  }

  void save(EmptyActivity activity, Bundle savedInstanceState) {
  }
}