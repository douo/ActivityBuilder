package info.dourok.esactivity.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import info.dourok.esactivity.BuilderParameter;
import info.dourok.esactivity.BaseActivityBuilder;
import info.dourok.esactivity.BaseResultConsumer;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.ResultParameter;
import info.dourok.esactivity.TransmitType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import info.dourok.esactivity.BuilderUtils;

@RequiresApi(api = Build.VERSION_CODES.N)
@Builder

@Result(name = "wtf", parameters = {
    @ResultParameter(name = "ids", type = ArrayList.class)
})
public class SampleActivity extends AppCompatActivity {

  @BuilderParameter(key = "wtf", keep = true, transmit = TransmitType.Ref)
  String text;
  @BuilderParameter float f;
  @BuilderParameter Double dd;
  @BuilderParameter byte[] bytes;
  @BuilderParameter ArrayList<Integer> ids;
  @BuilderParameter(transmit = TransmitType.Ref) HashSet set;
  SampleActivityHelper mHelper = BuilderUtils.createHelper(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHelper.inject();
    if (savedInstanceState != null) {
      mHelper.restore(savedInstanceState);
    }

    setContentView(R.layout.activity_sample);
    TextView tv = findViewById(R.id.text);
    tv.setText(text);
    tv.setOnClickListener(view -> {
      ArrayList<Double> ids = new ArrayList<>();
      ids.add(0.1);
      mHelper.finishText(ids);
    });
  }

  @Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
    super.onSaveInstanceState(outState, outPersistentState);
    mHelper.save(outState);
  }

  @Result
  public void resultText(ArrayList<Double> text) {
    mHelper.resultText(text);
  }

  @Result
  public void resultAbcd(String a, String b, String c, String d) {
    mHelper.resultAbcd(a, b, c, d);
  }

  public void forWtf(Consumer<ArrayList<? super Integer>> consumer) {
    Builder<SampleActivity> builder = new Builder<>(this);
    builder = builder.asIntent().asBuilder();
  }

  public static class MyConsumer<A extends Activity> extends BaseResultConsumer<A> {
  }

  public static class Builder<A extends Activity> extends BaseActivityBuilder<Builder<A>, A> {

    public Builder(A activity) {
      super(activity);
      setIntent(new Intent(activity, SampleActivity.class));
    }

    public Builder text(String text) {
      Builder<A> b = asIntent().asBuilder();

      getIntent().putExtra("text", text);
      return this;
    }

    public void forText(Consumer<String> consumer) {
    }

    @Override public MyConsumer<A> getConsumer() {
      if (consumer != null) {
        consumer = new MyConsumer<A>();
      }
      return (MyConsumer<A>) consumer;
    }

    @Override public Builder self() {
      return this;
    }
  }
}
