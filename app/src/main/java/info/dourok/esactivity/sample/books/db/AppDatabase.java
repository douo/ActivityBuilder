package info.dourok.esactivity.sample.books.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import info.dourok.esactivity.sample.books.Book;

@Database(entities = { Book.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public abstract BookDao bookDao();

  @Override protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public static AppDatabase build(Context application) {

    return Room.databaseBuilder(application, AppDatabase.class, "app").build();
  }
}