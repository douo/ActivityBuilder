package info.dourok.esactivity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import info.dourok.esactivity.activity.CaptureTestActivity;
import info.dourok.esactivity.test.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static info.dourok.esactivity.util.EspressoHelper.getActivityInstance;
import static org.hamcrest.Matchers.not;
/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CaptureInstrumentedTest {
  @Rule
  public ActivityTestRule<CaptureTestActivity> mActivityRule =
      new ActivityTestRule<>(CaptureTestActivity.class);

  @Test
  public void activity__ref_should_be_updated_after_activity_recreate() {
    String id = getActivityInstance().toString();
    onView(withText("captureActivity")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    pressBack();
    String idNew = getActivityInstance().toString();
    onView(withId(R.id.content)).check(matches(not(withText(id))));
    onView(withId(R.id.content)).check(matches(withText(idNew)));
  }

  @Test
  public void view_with_id__ref_should_be_updated_after_activity_recreate() {
    final String text = "text";
    onView(withText("captureViewWithId")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void fragment_with_id__ref_should_be_updated_after_activity_recreate() {
    final String text = "text";
    onView(withText("captureFragmentWithId")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void fragment_with_tag__ref_should_be_updated_after_activity_recreate() {
    final String text = "text";
    onView(withText("captureFragmentWithTag")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void support_fragment_with_id__ref_should_be_updated_after_activity_recreate() {
    final String text = "text";
    onView(withText("captureSupportFragmentWithId")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void support_fragment_with_tag__ref_should_be_updated_after_activity_recreate() {
    final String text = "text";
    onView(withText("captureSupportFragmentWithTag")).perform(click());

    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void method__ref_should_be_updated_after_activity_recreate() throws Exception {
    final String text = "text";
    onView(withText("methodRef")).perform(click());
    getInstrumentation().runOnMainSync(() -> mActivityRule.getActivity().recreate());
    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void view_with_id__ref_should_work() {
    final String text = "text";
    onView(withText("captureViewWithId")).perform(click());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void fragment_with_id__ref_should_work() {
    final String text = "text";
    onView(withText("captureFragmentWithId")).perform(click());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void fragment_with_tag__ref_should_work() {
    final String text = "text";
    onView(withText("captureFragmentWithTag")).perform(click());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void support_fragment_with_id__ref_should_work() {
    final String text = "text";
    onView(withText("captureSupportFragmentWithId")).perform(click());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void support_fragment_with_tag__ref_should_work() {
    final String text = "text";
    onView(withText("captureSupportFragmentWithTag")).perform(click());

    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }

  @Test
  public void method__ref_should_work() throws Exception {
    final String text = "text";
    onView(withText("methodRef")).perform(click());
    onView(withId(R.id.edit_text)).perform(replaceText(text));
    onView(withId(R.id.action_ok)).perform(click());
    onView(withId(R.id.content)).check(matches(withText(text)));
  }
}
