package info.dourok.esactivity.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import info.dourok.esactivity.test.R;

public class CaptureTestActivity extends AppCompatActivity {

  private TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    textView = findViewById(R.id.content);
    if (savedInstanceState == null) {
      prepareFragmentWithTag();
      prepareSupportFragmentWithTag();
    }
  }

  private void prepareFragmentWithTag() {
    OneButtonFragment fragment = OneButtonFragment.newInstance("captureFragmentWithTag");
    getFragmentManager().beginTransaction().add(R.id.parent, fragment, "fragment").commit();
  }

  private void prepareSupportFragmentWithTag() {
    OneButtonSupportFragment fragment =
        OneButtonSupportFragment.newInstance("captureSupportFragmentWithTag");
    getSupportFragmentManager().beginTransaction().add(R.id.parent, fragment, "fragment").commit();
  }

  public void captureActivity(View v) {
    EditorActivityBuilder.create(this).forCancel(intent -> showContent(this.toString())).start();
  }

  public void captureViewWithId(View v) {
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
