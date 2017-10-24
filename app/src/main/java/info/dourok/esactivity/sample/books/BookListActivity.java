package info.dourok.esactivity.sample.books;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import info.dourok.esactivity.sample.R;
import info.dourok.esactivity.sample.databinding.ActivityBookDetailBinding;
import info.dourok.esactivity.sample.databinding.ActivityBookListBinding;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

  RecyclerView mList;
  BookViewModel mBookViewModel;
  BookListAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBookViewModel = ViewModelProviders.of(this).get(BookViewModel.class);
    ActivityBookListBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_book_list);
    binding.setViewModel(mBookViewModel);

    mList = findViewById(R.id.list);
    mList.setHasFixedSize(true);
    mList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mAdapter = new BookListAdapter(mBookViewModel);
    mList.setAdapter(mAdapter);
  }

  @BindingAdapter("content")
  public static <T> void setContent(RecyclerView recyclerView, List<T> content) {
    RecyclerView.Adapter adapter = recyclerView.getAdapter();
    if (adapter == null) {
      return;
    }
    if (adapter instanceof BookListAdapter) {
      ((BookListAdapter) adapter).replace((List<Book>) content);
    }
  }
}
