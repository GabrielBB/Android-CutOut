package com.github.gabrielbb.cutout.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.gabrielbb.cutout.CutOut;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private static int GALLERY_REQUEST_FLAG = 891;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        final Uri imageIconUri = getUriFromDrawable(R.drawable.image_icon);
        imageView.setImageURI(imageIconUri);
        imageView.setTag(imageIconUri);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_FLAG);
        });

    }

    public Uri getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/drawable/" + getApplicationContext().getResources().getResourceEntryName(drawableId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == GALLERY_REQUEST_FLAG) {

            if (resultCode == RESULT_OK) {
                parseGallery(data);
            }else{
                Toast.makeText(this, "Image not picked from gallery.", Toast.LENGTH_LONG).show();
            }

        }else if(requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE){

            if (resultCode == RESULT_OK) {
                Uri imageUri = CutOut.getUri(data);
                imageView.setImageURI(imageUri);
                imageView.setTag(imageUri);
            }else{
                Toast.makeText(this, "User cancelled the CutOut screen.", Toast.LENGTH_LONG).show();
            }

        }

    }

    public void parseGallery(Intent data) {

        Uri selectedImage = data.getData();

        if (selectedImage != null) {

            Uri uri = data.getData();
            String picturePath = uri.getPath();

            if (picturePath == null) {
                Toast.makeText(this, "Image not received", Toast.LENGTH_LONG).show();
                return;
            }

            CutOut.activity()
                    .src(selectedImage)
                    .bordered()
                    .noCrop()
                    .start(this);

        }
    }

}
