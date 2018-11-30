package com.github.gabrielbb.cutout;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.theartofdev.edmodo.cropper.CropImage;

public class CutOut {

    public static final short CUTOUT_ACTIVITY_REQUEST_CODE = 368;
    static final String CUTOUT_EXTRA_SOURCE = "CUTOUT_EXTRA_SOURCE";
    public static final String CUTOUT_EXTRA_AD_ID = "CUTOUT_EXTRA_AD_ID";

    static final String CUTOUT_EXTRA_RESULT = "CUTOUT_EXTRA_RESULT";
    public static final short CUTOUT_ACTIVITY_RESULT_ERROR_CODE = 3680;

    public static ActivityBuilder activity() {
        return new ActivityBuilder(null);
    }

    public static ActivityBuilder activity(Uri mSource) {
        return new ActivityBuilder(mSource);
    }

    /**
     * Builder used for creating CutOut Activity by user request.
     */
    public static final class ActivityBuilder {

        /**
         * The image to crop source Android uri.
         */
        @Nullable
        private final Uri mSource;

        @Nullable
        private String adId;

        private ActivityBuilder(@Nullable Uri source) {
            mSource = source;
        }


        /**
         * Get {@link CutOutActivity} intent to start the activity.
         */
        private Intent getIntent(@NonNull Context context) {
            Intent intent = new Intent();
            intent.setClass(context, CutOutActivity.class);

            if (mSource != null) {
                intent.putExtra(CUTOUT_EXTRA_SOURCE, mSource);
            }

            if (adId != null) {
                intent.putExtra(CUTOUT_EXTRA_AD_ID, adId);
            }

            return intent;
        }

        public ActivityBuilder setAd(String adId) {
            this.adId = adId;
            return this;
        }

        /**
         * Start {@link CutOutActivity}.
         *
         * @param activity activity to receive result
         */
        public void start(@NonNull Activity activity) {
            activity.startActivityForResult(getIntent(activity), CUTOUT_ACTIVITY_REQUEST_CODE);
        }

        /*
        public ActivityBuilder setSnapRadius(float snapRadius) {
            mOptions.snapRadius = snapRadius;
            return this;
        }*/
    }

    public static String getUri(@Nullable Intent data) {
        return data != null ? data.getStringExtra(CUTOUT_EXTRA_RESULT) : null;
    }

    public static Exception getError(@Nullable Intent data) {
        return data != null ? (Exception) data.getSerializableExtra(CUTOUT_EXTRA_RESULT) : null;
    }
}
