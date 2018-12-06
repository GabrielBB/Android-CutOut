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
                    .start(this);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE) {

            switch (resultCode) {
                case Activity.RESULT_OK:
                    Uri imageUri = CutOut.getUri(data);
                    // Save the image using the returned Uri here
                    imageView.setImageURI(imageUri);
                    imageView.setTag(imageUri);
                    break;
                case CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE:
                    Exception ex = CutOut.getError(data);
                    break;
                default:
                    System.out.print("User cancelled the CutOut screen");
            }
        }
    }

    public Uri getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/drawable/" + getApplicationContext().getResources().getResourceEntryName(drawableId));
    }
}
