package com.github.gabrielbb.cutout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CutOut {

    public static final short CUTOUT_ACTIVITY_REQUEST_CODE = 368;
    public static final short CUTOUT_ACTIVITY_RESULT_ERROR_CODE = 3680;

    static final String CUTOUT_EXTRA_SOURCE = "CUTOUT_EXTRA_SOURCE";
    static final String CUTOUT_EXTRA_AD_ID = "CUTOUT_EXTRA_AD_ID";
    static final String CUTOUT_EXTRA_RESULT = "CUTOUT_EXTRA_RESULT";
    static final String CUTOUT_EXTRA_BORDER = "CUTOUT_EXTRA_BORDER";

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

        private boolean bordered;

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

            if (bordered) {
                intent.putExtra(CUTOUT_EXTRA_BORDER, true);
            }

            return intent;
        }

        public ActivityBuilder ad(String adId) {
            this.adId = adId;
            return this;
        }

        public ActivityBuilder bordered() {
            this.bordered = true;
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

    public static Uri getUri(@Nullable Intent data) {
        return data != null ? data.getParcelableExtra(CUTOUT_EXTRA_RESULT) : null;
    }

    public static Exception getError(@Nullable Intent data) {
        return data != null ? (Exception) data.getSerializableExtra(CUTOUT_EXTRA_RESULT) : null;
    }
}
