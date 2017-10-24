package info.dourok.esactivity.sample.books;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import info.dourok.esactivity.sample.BR;
import info.dourok.esactivity.sample.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by tiaolins on 2017/10/24.
 */

class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {
  List<Book> mItems;
  // each time data is set, we update this variable so that if DiffUtil calculation returns
  // after repetitive updates, we can ignore the old calculation
  private int dataVersion = 0;
  private BookViewModel mViewModel;

  public BookListAdapter(BookViewModel viewModel) {
    mViewModel = viewModel;
  }

  @Override public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater =
        LayoutInflater.from(parent.getContext());
    ViewDataBinding binding = DataBindingUtil.inflate(
        layoutInflater, R.layout.item_book, parent, false);
    return new BookViewHolder(binding, BR.item);
  }

  @Override public void onBindViewHolder(BookViewHolder holder, int position) {
    holder.bind(mItems.get(position), mViewModel);
  }

  @Override public int getItemCount() {
    return mItems == null ? 0 : mItems.size();
  }

  private List<Book> cloneList(List<Book> books) {
    List<Book> clone = new ArrayList<>(books.size());
    for (Book book : books) {
      clone.add(book);
    }
    return clone;
  }

  @MainThread
  public void replace(List<Book> update) {
    dataVersion++;
    if (mItems == null) {
      if (update == null) {
        return;
      }
      mItems = cloneList(update);
      notifyDataSetChanged();
    } else if (update == null) {
      int oldSize = mItems.size();
      mItems = null;
      notifyItemRangeRemoved(0, oldSize);
    } else {
      final int startVersion = dataVersion;
      final List<Book> oldItems = mItems;
      new AsyncTask<Void, Void, DiffUtil.DiffResult>() {
        @Override
        protected DiffUtil.DiffResult doInBackground(Void... voids) {
          return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
              return oldItems.size();
            }

            @Override
            public int getNewListSize() {
              return update.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
              Book oldItem = oldItems.get(oldItemPosition);
              Book newItem = update.get(newItemPosition);
              return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
              Book oldItem = oldItems.get(oldItemPosition);
              Book newItem = update.get(newItemPosition);
              return Objects.equals(oldItem.title, newItem.title) && Objects.equals(oldItem.author,
                  newItem.author);
            }
          });
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
          if (startVersion != dataVersion) {
            // ignore update
            return;
          }
          mItems = cloneList(update);
          diffResult.dispatchUpdatesTo(BookListAdapter.this);
        }
      }.execute();
    }
  }

  static class BookViewHolder extends RecyclerView.ViewHolder {
    ViewDataBinding mBinding;
    int mVariableId;

    public BookViewHolder(ViewDataBinding binding, int variableId) {
      super(binding.getRoot());
      mBinding = binding;
      mVariableId = variableId;
    }

    void bind(Book book, BookViewModel mViewModel) {
      mBinding.setVariable(BR.item, book);
      mBinding.setVariable(BR.viewModel, mViewModel);
    }
  }
}
