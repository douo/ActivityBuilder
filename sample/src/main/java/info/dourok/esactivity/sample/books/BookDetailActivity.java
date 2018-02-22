package info.dourok.esactivity.sample.books;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import info.dourok.esactivity.Builder;
import info.dourok.esactivity.BuilderParameter;
import info.dourok.esactivity.BuilderUtil;
import info.dourok.esactivity.Result;
import info.dourok.esactivity.ResultParameter;
import info.dourok.esactivity.ResultSet;
import info.dourok.esactivity.sample.R;
import info.dourok.esactivity.sample.databinding.ActivityBookDetailBinding;

/** @author tiaolins */
@Builder
@ResultSet(
  results = {
    @Result(name = "delete"),
    @Result(
      name = "update",
      parameters = {@ResultParameter(name = "book", type = Book.class)}
    )
  }
)
public class BookDetailActivity extends AppCompatActivity {
  @BuilderParameter long mBookId;
  Book mBook;
  BookViewModel mViewModel;
  ActivityBookDetailBinding mBinding;
  BookDetailActivityHelper mHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHelper = BuilderUtil.createHelper(this);
    mViewModel = ViewModelProviders.of(this).get(BookViewModel.class);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail);
    mBook = mViewModel.getBookById(mBookId);
    mBinding.setBook(mBook);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_book_detail, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_save:
        mHelper.finishUpdate(mBook);
        return true;
      case R.id.action_delete:
        mHelper.finishDelete();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
