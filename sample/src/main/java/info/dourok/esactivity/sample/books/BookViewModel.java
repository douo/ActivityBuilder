package info.dourok.esactivity.sample.books;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.view.View;
import info.dourok.esactivity.BuilderUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tiaolins
 * @date 2017/10/23
 */
public class BookViewModel extends AndroidViewModel {
  ObservableField<List<Book>> mBooks;

  public BookViewModel(@NonNull Application application) {
    super(application);
    mBooks = new ObservableField<>();
    fillBook();
  }

  private void fillBook() {
    List<Book> books = new ArrayList<>();
    Book a = new Book();
    a.id = 1;
    a.title = "Code Complete";
    a.author = "Steve McConnell";
    books.add(a);
    a = new Book();
    a.id = 2;
    a.title = "The Pragmatic Programmer";
    a.author = "Andrew Hunt & David Thomas";
    books.add(a);
    a = new Book();
    a.id = 3;
    a.title = "Alpha, Bravo, Charlie";
    a.author = "Sara Gillingham";
    books.add(a);
    mBooks.set(books);
  }

  public ObservableField<List<Book>> books() {
    return mBooks;
  }

  public Book getBookById(long id) {
    for (Book book : mBooks.get()) {
      if (book.id == id) {
        return book;
      }
    }
    return null;
  }

  public void showBookDetail(Book book, Context context) {
    BookDetailActivityBuilder.create((Activity) context)
        .bookId(book.id)
        .forDelete(
            () -> {
              mBooks.get().remove(book);
              mBooks.notifyChange();
            })
        .forUpdate(
            (_b) -> {
              int idx = mBooks.get().indexOf(getBookById(_b.id));
              mBooks.get().remove(idx);
              mBooks.get().add(idx, _b);
              mBooks.notifyChange();
            })
        .start();
  }
}
