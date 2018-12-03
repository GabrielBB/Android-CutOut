package com.github.gabrielbb.cutout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import com.github.gabrielbb.cutout.test.MainActivity;
import com.github.gabrielbb.cutout.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private ImageView imageView;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        this.imageView = activityRule.getActivity().findViewById(R.id.imageView);
        assertEquals(imageView.getTag(), activityRule.getActivity().getUriFromDrawable(R.drawable.image_icon));

        onView(withId(R.id.fab)).perform(click());

        assertThat(getCurrentActivity(), instanceOf(CutOutActivity.class));
    }

    @Test
    public void testSavingWithNoChange() {
        final Uri initialUri = (Uri) imageView.getTag();

        onView(withId(R.id.done)).perform(click());

        assertNotEquals(imageView.getTag(), initialUri);
    }

    public void testDefaultActivatedActionButton() {
        onView(withId(R.id.manual_clear_button)).check(matches(isEnabled()));
    }

    @Test
    public void testMagicToolButtonEnabled() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        onView(withId(R.id.auto_clear_button)).perform(click());

    }

    @Test
    public void testMagicToolEffect() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        Bitmap initialBitmap = cutOutActivity.getCurrentBitmap();

        onView(withId(R.id.drawView)).perform(click());

        Bitmap postClickBitmap = cutOutActivity.getCurrentBitmap();


    }

    private Activity getCurrentActivity() {
        final Activity[] activity = new Activity[1];
        onView(isRoot()).check((view, noViewFoundException) -> activity[0] = (Activity) view.getContext());
        return activity[0];
    }
}
