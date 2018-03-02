package info.dourok.esactivity.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import info.dourok.esactivity.BuilderUtil;
import info.dourok.esactivity.test.R;

public class CaptureTestActivity extends AppCompatActivity {

  private TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    textView = findViewById(R.id.content);
  }

  public void captureActivity(View v) {
    EditorActivityBuilder.create(this).forCancel(intent -> showContent(this.toString())).start();
  }

  public void captureView(View v) {
    TextView localTextView = findViewById(R.id.content);
    EditorActivityBuilder.create(this).forContent(localTextView::setText).start();
  }

  public void methodRef(View v) {

    EditorActivityBuilder.create(this).forContent(this::showContent).start();
  }

  void showContent(String s) {
    textView.setText(s);
  }
}
