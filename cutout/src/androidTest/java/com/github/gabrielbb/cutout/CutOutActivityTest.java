package com.github.gabrielbb.cutout;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CutOutActivityTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule(CutOutActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();

            Intent result = new Intent(targetContext, CutOutActivity.class);
            result.putExtra(CutOut.CUTOUT_EXTRA_SOURCE, getUriToResource(InstrumentationRegistry.getTargetContext(), R.drawable.done));
            return result;
        }
    };

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.github.gabrielbb.cutout.test", appContext.getPackageName());
    }

    private static Uri getUriToResource(@NonNull Context context,
                                        @AnyRes int resId)
            throws Resources.NotFoundException {

        Resources res = context.getResources();

        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
    }
}
