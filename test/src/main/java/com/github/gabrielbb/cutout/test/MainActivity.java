package com.github.gabrielbb.cutout.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.github.gabrielbb.cutout.CutOut;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);

        final Uri imageIconUri = getUriFromDrawable(R.drawable.image_icon);
        imageView.setImageURI(imageIconUri);
        imageView.setTag(imageIconUri);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Uri testImageUri = getUriFromDrawable(R.drawable.test_image);

            CutOut.activity()
                    .src(testImageUri)
                    .bordered()
                    .noCrop()
                    .ad()
                    .start(this);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                Uri uri = CutOut.getUri(data);

                imageView.setImageURI(uri);
                imageView.setTag(uri);

            } else if (resultCode == CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE) {
                Exception ex = CutOut.getError(data);
                throw new RuntimeException(ex);
            } else {
                // CutOut Activity was cancelled by the user
            }
        }
    }

    public Uri getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/drawable/" + getApplicationContext().getResources().getResourceEntryName(drawableId));
    }
}
