package info.dourok.esactivity.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderParameter;
import info.dourok.esactivity.BuilderUtil;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.ResultParameter;
import info.dourok.esactivity.test.R;

/** @author tiaolins */
@Builder
@Result(
  name = "content",
  parameters = {@ResultParameter(name = "content", type = String.class)}
)
public class EditorActivity extends AppCompatActivity {
  @BuilderParameter String hint;
  EditorActivityHelper mHelper;
  private EditText editText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);
    mHelper = BuilderUtil.createHelper(this);
    editText = findViewById(R.id.edit_text);
    if (hint != null) {
      editText.setHint(hint);
    }
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_editor, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_ok:
        mHelper.finishContent(editText.getText().toString());
        return true;
    }
    return false;
  }
}
