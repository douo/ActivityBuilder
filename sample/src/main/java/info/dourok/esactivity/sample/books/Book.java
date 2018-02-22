package info.dourok.esactivity.sample.books;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.DrawableRes;

/**
 * @author tiaolins
 * @date 2017/10/23
 */
@Entity
public class Book {
  @PrimaryKey public long id;
  @DrawableRes public int cover;
  public String title;
  public String author;
}
