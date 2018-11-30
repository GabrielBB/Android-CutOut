package com.github.gabrielbb.cutout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.ref.WeakReference;
import java.util.UUID;

import top.defaults.checkerboarddrawable.CheckerboardDrawable;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class CutOutActivity extends AppCompatActivity {

    private static final int INTRO_REQUEST = 4;
    private static final String INTRO_SHOWN = "intro_shown";
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    private FrameLayout loadingModal;
    private GestureView gestureView;
    private DrawView drawView;
    private LinearLayout manualClearSettingsLayout;
    private CropImage.ActivityBuilder cropImageBuilder;

    private static final short MAX_ERASER_SIZE = 150;
    private static final short BORDER_SIZE = 45;
    private static final int BORDER_COLOR = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_edit);

        Toolbar toolbar = findViewById(R.id.photo_edit_toolbar);
        toolbar.setBackgroundColor(Color.BLACK);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        FrameLayout drawViewLayout = findViewById(R.id.drawViewLayout);

        int sdk = android.os.Build.VERSION.SDK_INT;

        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            drawViewLayout.setBackgroundDrawable(CheckerboardDrawable.create());
        } else {
            drawViewLayout.setBackground(CheckerboardDrawable.create());
        }

        SeekBar strokeBar = findViewById(R.id.strokeBar);
        strokeBar.setMax(MAX_ERASER_SIZE);
        strokeBar.setProgress(50);

        gestureView = findViewById(R.id.gestureView);

        drawView = findViewById(R.id.drawView);
        drawView.setDrawingCacheEnabled(true);
        drawView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //drawView.setDrawingCacheEnabled(true);
        drawView.setStrokeWidth(strokeBar.getProgress());

        strokeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawView.setStrokeWidth(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        loadingModal = findViewById(R.id.loadingModal);
        loadingModal.setVisibility(INVISIBLE);

        drawView.setLoadingModal(loadingModal);

        manualClearSettingsLayout = findViewById(R.id.manual_clear_settings_layout);

        setUndoRedo();
        initializeDrawViewActionButtons();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }

        }

        Button doneButton = findViewById(R.id.done);

        doneButton.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startSaveStickerTask();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_CODE);
            }

        });

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);

        if (getIntent().hasExtra(CutOut.CUTOUT_EXTRA_AD_ID)) {
            String adId = getIntent().getStringExtra(CutOut.CUTOUT_EXTRA_AD_ID);

            AdView adView = new AdView(getApplicationContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(adId);

            adViewContainer.addView(adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adViewContainer.setVisibility(View.GONE);
        }


        if (getIntent().hasExtra(CutOut.CUTOUT_EXTRA_SOURCE)) {
            String sourceUri = getIntent().getStringExtra(CutOut.CUTOUT_EXTRA_SOURCE);

            cropImageBuilder = CropImage.activity(Uri.parse(sourceUri));
        } else {
            cropImageBuilder = CropImage.activity();
        }

        cropImageBuilder = cropImageBuilder.setGuidelines(CropImageView.Guidelines.ON);
        startCropImage();
    }

    private void startSaveStickerTask() {
        Bitmap image = BitmapUtility.getBorderedBitmap(this.drawView.getDrawingCache(), BORDER_COLOR, BORDER_SIZE);
        new SaveDrawingTask(this).execute(image);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSaveStickerTask();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "You denied permissions to save the image. Please, grant the permissions to continue", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void startCropImage() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean alreadyShown = sharedPref.getBoolean(INTRO_SHOWN, false);

        if (alreadyShown) {
            cropImageBuilder.start(this);
        } else {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivityForResult(intent, INTRO_REQUEST);
        }

    }

    private void activateGestureView() {
        gestureView.getController().getSettings()
                .setMaxZoom(4f)
                .setDoubleTapZoom(-1f) // Falls back to max zoom level
                .setPanEnabled(true)
                .setZoomEnabled(true)
                .setDoubleTapEnabled(true)
                .setOverscrollDistance(0f, 0f)
                .setOverzoomFactor(2f);
    }

    private void deactivateGestureView() {
        gestureView.getController().getSettings()
                .setPanEnabled(false)
                .setZoomEnabled(false)
                .setDoubleTapEnabled(false);
    }

    private void initializeDrawViewActionButtons() {
        Button autoClearButton = findViewById(R.id.auto_clear_button);
        Button manualClearButton = findViewById(R.id.manual_clear_button);
        Button zoomButton = findViewById(R.id.zoom_button);

        autoClearButton.setActivated(false);
        autoClearButton.setOnClickListener((buttonView) -> {
            if (!autoClearButton.isActivated()) {
                drawView.setAction(DrawView.DrawViewAction.AUTO_CLEAR);
                manualClearSettingsLayout.setVisibility(INVISIBLE);
                autoClearButton.setActivated(true);
                manualClearButton.setActivated(false);
                zoomButton.setActivated(false);
                deactivateGestureView();
            }
        });

        manualClearButton.setActivated(true);
        drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
        manualClearButton.setOnClickListener((buttonView) -> {
            if (!manualClearButton.isActivated()) {
                drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
                manualClearSettingsLayout.setVisibility(VISIBLE);
                manualClearButton.setActivated(true);
                autoClearButton.setActivated(false);
                zoomButton.setActivated(false);
                deactivateGestureView();
            }

        });

        zoomButton.setActivated(false);
        deactivateGestureView();
        zoomButton.setOnClickListener((buttonView) -> {
            if (!zoomButton.isActivated()) {
                drawView.setAction(DrawView.DrawViewAction.ZOOM);
                manualClearSettingsLayout.setVisibility(INVISIBLE);
                zoomButton.setActivated(true);
                manualClearButton.setActivated(false);
                autoClearButton.setActivated(false);
                activateGestureView();
            }

        });
    }

    private void setUndoRedo() {
        Button undoButton = findViewById(R.id.undo);
        undoButton.setEnabled(false);
        undoButton.setOnClickListener(v -> undo());
        Button redoButton = findViewById(R.id.redo);
        redoButton.setEnabled(false);
        redoButton.setOnClickListener(v -> redo());

        drawView.setButtons(undoButton, redoButton);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {

                drawView.setUri(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Intent intent = new Intent();
                intent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, result.getError());
                setResult(CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE, intent);
                finish();
            }
        } else if (requestCode == INTRO_REQUEST) {
            System.out.println("Using for the first time. Introduced application to user");
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(INTRO_SHOWN, true);
            editor.apply();
            cropImageBuilder.start(this);
        }
    }

    public void undo() {
        drawView.undo();
    }

    public void redo() {
        drawView.redo();
    }

    private static class SaveDrawingTask extends AsyncTask<Bitmap, Void, String> {
        private final WeakReference<CutOutActivity> activityWeakReference;

        SaveDrawingTask(CutOutActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityWeakReference.get().loadingModal.setVisibility(VISIBLE);
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            return MediaStore.Images.Media.insertImage(activityWeakReference.get().getApplicationContext().getContentResolver(), bitmaps[0], UUID.randomUUID().toString(), null);
        }

        protected void onPostExecute(String uri) {
            super.onPostExecute(uri);
            Intent resultIntent = new Intent();

            if (uri != null) {
                resultIntent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, uri);
                activityWeakReference.get().setResult(Activity.RESULT_OK, resultIntent);
            } else {
                resultIntent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, new Exception("CutOut could not saved image to gallery"));
                activityWeakReference.get().setResult(CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE, resultIntent);
            }

            activityWeakReference.get().finish();

        }
    }
}