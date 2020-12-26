package com.github.gabrielbb.cutout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static android.view.View.VISIBLE;

class SaveDrawingTask extends AsyncTask<Bitmap, Void, Pair<File, Exception>> {

    private static final String SAVED_IMAGE_FORMAT = "png";
    private static final String SAVED_IMAGE_NAME = "cutout_tmp";

    private final WeakReference<CutOutActivity> activityWeakReference;
    private CutOutSaveTypes type;

    SaveDrawingTask(CutOutActivity activity, CutOutSaveTypes type) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activityWeakReference.get().loadingModal.setVisibility(VISIBLE);
    }

    @Override
    protected Pair<File, Exception> doInBackground(Bitmap... bitmaps) {
        if (type == CutOutSaveTypes.SAVE_TO_CACHE) {
            try {
                File file = File.createTempFile(SAVED_IMAGE_NAME, SAVED_IMAGE_FORMAT, activityWeakReference.get().getApplicationContext().getCacheDir());

                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmaps[0].compress(Bitmap.CompressFormat.PNG, 95, out);
                    return new Pair<>(file, null);
                }
            } catch (IOException e) {
                return new Pair<>(null, e);
            }
        } else {
            SaveToStorageResult result = saveToStorage(bitmaps[0]);
            File file = new File(result.getPath());
            if (file.exists())
                return new Pair<>(file, null);
            else
                return new Pair<>(null, result.getException());
        }
    }

    protected void onPostExecute(Pair<File, Exception> result) {
        super.onPostExecute(result);

        Intent resultIntent = new Intent();

        if (result.first != null) {
            Uri uri = Uri.fromFile(result.first);

            resultIntent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, uri);
            activityWeakReference.get().setResult(Activity.RESULT_OK, resultIntent);
            activityWeakReference.get().finish();

        } else {
            activityWeakReference.get().exitWithError(result.second);
        }
    }

    private SaveToStorageResult saveToStorage(Bitmap capturedBitmap) {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "cutout");

        folder.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH_mm_SS", Locale.US);

        String format = sdf.format(new Date());

        File photoFile = new File(folder, format.concat(".png"));

        if (photoFile.exists()) {
            photoFile.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(photoFile.getPath());

            capturedBitmap.compress(Bitmap.CompressFormat.PNG, 60, fos);

            fos.flush();
            fos.close();
            return new SaveToStorageResult(photoFile.getAbsolutePath(), null);
        } catch (IOException e) {
            Log.e("Cutout", "Exception in photoCallback", e);
            return new SaveToStorageResult("", e);
        }
    }

    public class SaveToStorageResult {
        private String path;
        private IOException exception;

        public SaveToStorageResult(String path, IOException exception) {
            this.path = path;
            this.exception = exception;
        }

        public String getPath() {
            return path;
        }

        public IOException getException() {
            return exception;
        }
    }
}