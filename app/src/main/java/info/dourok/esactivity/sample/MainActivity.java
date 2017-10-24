package info.dourok.esactivity.sample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import info.dourok.esactivity.BuilderUtil;
import info.dourok.esactivity.sample.books.BookListActivity;
import info.dourok.esactivity.sample.editor.EditorActivity;
import info.dourok.esactivity.sample.editor.EditorActivityBuilder;
import info.dourok.esactivity.sample.editor.EditorActivityHelper;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @RequiresApi(api = Build.VERSION_CODES.N) @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    openBook();
    openEditor();
  }

  private void openBook() {
    Button btn = findViewById(R.id.book_list);
    btn.setOnClickListener(
        view ->
            BuilderUtil.createBuilder(this, BookListActivity.class)
                .start());
  }

  private void openEditor() {
    findViewById(R.id.editor).setOnClickListener(
        view ->
            EditorActivityBuilder.create(this)
                .hint("say something!")
                .forContent(System.out::println)
                .start()
    );
  }

  private void badPractice() {
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(
        view ->
            SampleActivityBuilder.create(this)
                .forCancel(data -> Snackbar.make(view, "You're Cancel",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show())
                .forText(s -> Toast.makeText(this, "" + s, Toast.LENGTH_SHORT).show())
                .start());
  }

  private void goodPractice() {
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(
        view ->
            SampleActivityBuilder.create(this)
                .forCancel((context, data) -> Snackbar.make(context.findViewById(R.id.fab),
                    "You're Cancel",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show())
                .forText((context, s) -> Toast.makeText(context, "" + s, Toast.LENGTH_SHORT).show())
                .start());
  }

  private static final int REQUEST_SOME_TEXT = 0x2;

  private void requestSomeTextNormalWay() {
    findViewById(R.id.fab).setOnClickListener(
        view -> {
          Intent intent = new Intent(this, EditorActivity.class);
          intent.putExtra("hint", "say something");
          startActivityForResult(intent, REQUEST_SOME_TEXT);
        }
    );
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_SOME_TEXT:
        if (resultCode == EditorActivityHelper.RESULT_CONTENT) {
          String text = data.getStringExtra("content");
          showToast(text);
        }
    }
  }

  private void showToast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  public void handleOk(Intent data) {
    Log.d(TAG, toString());
    Snackbar.make(findViewById(R.id.fab), "You're Ok", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
