package info.dourok.esactivity.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import info.dourok.esactivity.ActivityParameter;
import info.dourok.esactivity.BaseActivityBuilder;
import info.dourok.esactivity.EasyActivity;
import java.util.function.Consumer;

@RequiresApi(api = Build.VERSION_CODES.N)
@EasyActivity
public class SampleActivity extends AppCompatActivity {

  @ActivityParameter(key = "wtf")
  String text;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      Helper.inject(this);
    }
    setContentView(R.layout.activity_sample);
    TextView tv = findViewById(R.id.text);
    tv.setText(text);
    tv.setOnClickListener(view -> {
      setResult(RESULT_OK);
      finish();
    });
  }

  public static class Helper {
    public static void inject(SampleActivity activity) {
      Intent intent = activity.getIntent();

      activity.text = intent.getStringExtra("text");
    }

    public static void restore(SampleActivity activity, Bundle savedInstanceState) {
      activity.text = savedInstanceState.getString("text");
    }

    public static void save(SampleActivity activity, Bundle savedInstanceState) {
      savedInstanceState.putString("text", activity.text);
    }
  }

  public static class Builder extends BaseActivityBuilder<Builder> {
    public Builder(Activity activity) {
      super(activity);
      setIntent(new Intent(activity, SampleActivity.class));
    }

    public Builder text(String text) {
      getIntent().putExtra("text", text);
      return this;
    }

    public void forText(Consumer<String> consumer) {
    }

    @Override public Builder self() {
      return this;
    }
  }
}
