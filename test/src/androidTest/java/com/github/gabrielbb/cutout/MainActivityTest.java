package com.github.gabrielbb.cutout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import com.github.gabrielbb.cutout.test.MainActivity;
import com.github.gabrielbb.cutout.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(CAMERA, WRITE_EXTERNAL_STORAGE);

    @Before
    public void init() {
        this.imageView = activityRule.getActivity().findViewById(R.id.imageView);
        assertEquals(imageView.getTag(), activityRule.getActivity().getUriFromDrawable(R.drawable.image_icon));

        onView(withId(R.id.fab)).perform(click());
    }

    @Test
    public void testActivityShowUp() {
        assertThat(getCurrentActivity(), instanceOf(CutOutActivity.class));
    }

    @Test
    public void testSavingWithNoChange() {
        final Uri initialUri = (Uri) imageView.getTag();

        onView(withId(R.id.done)).perform(click());

        assertNotEquals(imageView.getTag(), initialUri);
    }

    public void testDefaultActivatedTool() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        assertTrue(cutOutActivity.findViewById(R.id.manual_clear_button).isActivated());
    }


    @Test
    public void testMagicToolButtonClick() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        onView(withId(R.id.auto_clear_button)).perform(click());
        assertTrue(cutOutActivity.findViewById(R.id.auto_clear_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.manual_clear_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.zoom_button).isActivated());
    }

    @Test
    public void testManualToolButtonClick() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        onView(withId(R.id.manual_clear_button)).perform(click());
        assertTrue(cutOutActivity.findViewById(R.id.manual_clear_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.auto_clear_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.zoom_button).isActivated());
    }

    @Test
    public void testZoomButtonClick() {
        CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        onView(withId(R.id.zoom_button)).perform(click());
        assertTrue(cutOutActivity.findViewById(R.id.zoom_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.manual_clear_button).isActivated());
        assertFalse(cutOutActivity.findViewById(R.id.auto_clear_button).isActivated());
    }

    @Test
    public void testMagicToolEffect() {
        final Uri initialUri = (Uri) imageView.getTag();

        final CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        testMagicToolButtonClick();

        final DrawView drawView = cutOutActivity.findViewById(R.id.drawView);

        final Bitmap initialBitmap = drawView.getCurrentBitmap();

        onView(withId(R.id.drawView)).perform(click());

        final View loadingModal = cutOutActivity.findViewById(R.id.loadingModal);

        do {
            SystemClock.sleep(2000);
        }
        while (loadingModal.getVisibility() == View.VISIBLE);

        assertFalse(initialBitmap.sameAs(drawView.getCurrentBitmap()));

        onView(withId(R.id.done)).perform(click());

        assertNotEquals(imageView.getTag(), initialUri);
    }

    @Test
    public void testUndoButton() {
        final Uri initialUri = (Uri) imageView.getTag();

        final CutOutActivity cutOutActivity = (CutOutActivity) getCurrentActivity();

        testMagicToolButtonClick();

        final DrawView drawView = cutOutActivity.findViewById(R.id.drawView);

        final Bitmap initialBitmap = drawView.getCurrentBitmap();

        onView(withId(R.id.drawView)).perform(click());

        final View loadingModal = cutOutActivity.findViewById(R.id.loadingModal);

        do {
            SystemClock.sleep(2000);
        }
        while (loadingModal.getVisibility() == View.VISIBLE);

        assertFalse(initialBitmap.sameAs(drawView.getCurrentBitmap()));

        onView(withId(R.id.undo)).perform(click());

        assertTrue(initialBitmap.sameAs(drawView.getCurrentBitmap()));

        onView(withId(R.id.redo)).perform(click());

        assertFalse(initialBitmap.sameAs(drawView.getCurrentBitmap()));

        onView(withId(R.id.done)).perform(click());

        assertNotEquals(imageView.getTag(), initialUri);
    }

    private Activity getCurrentActivity() {
        final Activity[] activity = new Activity[1];
        onView(isRoot()).check((view, noViewFoundException) -> activity[0] = (Activity) view.getContext());
        return activity[0];
    }
}