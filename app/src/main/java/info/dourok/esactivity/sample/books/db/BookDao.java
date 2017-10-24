package info.dourok.esactivity.sample.books.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Query;
import info.dourok.esactivity.sample.books.Book;
import java.util.List;

/**
 * Created by tiaolins on 2017/10/23.
 */
@Dao
public interface BookDao {
  @Query("SELECT * FROM book")
  List<Book> getAll();

  @Query("SELECT * FROM book WHERE id = :id")
  Book findById(long id);

  @Delete
  void delete(Book book);
}
