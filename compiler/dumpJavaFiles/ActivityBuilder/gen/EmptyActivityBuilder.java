package test;
import android.app.Activity;
import android.content.Intent;
import info.dourok.esactivity.BaseActivityBuilder;
import java.lang.Override;

public class EmptyActivityBuilder<A extends Activity> extends BaseActivityBuilder<EmptyActivityBuilder<A>, A> {
  private EmptyActivityBuilder(A activity) {
    super(activity);
    setIntent(new Intent(activity, EmptyActivity.class));
  }

  public static <A extends Activity> EmptyActivityBuilder<A> create(A activity) {
    return new EmptyActivityBuilder<A>(activity);
  }

  @Override
  protected EmptyActivityBuilder<A> self() {
    return this;
  }
}