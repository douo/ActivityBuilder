package info.dourok.esactivity.sample;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {
  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

  @Test
  public void everythingIsOk() throws Exception {
    final String text = "text";
    onView(withText("Editor")).perform(click());
    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void cancelIsOk() throws Exception {
    final String text = "cancel";
    onView(withText("Editor")).perform(click());
    pressBack();
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void cancelIsOkAfterActivityRecreate() throws Exception {
    final String text = "cancel";
    onView(withText("Editor")).perform(click());
    //onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(() -> mActivityRule.getActivity().recreate());
    pressBack();
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void shouldWorkAfterActivityRecreate() throws Exception {
    final String text = "text";
    onView(withText("Editor")).perform(click());
    // onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
    onView(withId(R.id.edit_text)).perform(replaceText(text));
    //InstrumentationRegistry.getInstrumentation()
    //    .runOnMainSync(() -> mActivityRule.getActivity().recreate());
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }
}
