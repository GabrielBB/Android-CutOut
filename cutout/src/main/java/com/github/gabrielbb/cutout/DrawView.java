package com.github.gabrielbb.cutout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;
import java.util.Stack;

import static com.github.gabrielbb.cutout.DrawView.DrawViewAction.AUTO_CLEAR;
import static com.github.gabrielbb.cutout.DrawView.DrawViewAction.MANUAL_CLEAR;
import static com.github.gabrielbb.cutout.DrawView.DrawViewAction.ZOOM;

class DrawView extends View {

    private Path livePath;
    private Paint pathPaint;

    private Bitmap imageBitmap;
    private final Stack<Pair<Pair<Path, Paint>, Bitmap>> cuts = new Stack<>();
    private final Stack<Pair<Pair<Path, Paint>, Bitmap>> undoneCuts = new Stack<>();

    private float pathX, pathY;

    private static final float TOUCH_TOLERANCE = 4;
    private static final float COLOR_TOLERANCE = 20;

    private Button undoButton;
    private Button redoButton;

    private View loadingModal;

    private DrawViewAction currentAction;

    public enum DrawViewAction {
        AUTO_CLEAR,
        MANUAL_CLEAR,
        ZOOM
    }

    public DrawView(Context c, AttributeSet attrs) {

        super(c, attrs);

        livePath = new Path();

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.TRANSPARENT);
        pathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setButtons(Button undoButton, Button redoButton) {
        this.undoButton = undoButton;
        this.redoButton = redoButton;
    }


    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);

        resizeBitmap(newWidth, newHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        if (imageBitmap != null) {

            canvas.drawBitmap(this.imageBitmap, 0, 0, null);

            for (Pair<Pair<Path, Paint>, Bitmap> action : cuts) {
                if (action.first != null) {
                    canvas.drawPath(action.first.first, action.first.second);
                }
            }

            if (currentAction == MANUAL_CLEAR) {
                canvas.drawPath(livePath, pathPaint);
            }
        }

        canvas.restore();
    }

    private void touchStart(float x, float y) {
        pathX = x;
        pathY = y;

        undoneCuts.clear();
        redoButton.setEnabled(false);

        if (currentAction == AUTO_CLEAR) {
            new AutomaticPixelClearingTask(this).execute((int) x, (int) y);
        } else {
            livePath.moveTo(x, y);
        }

        invalidate();
    }

    private void touchMove(float x, float y) {
        if (currentAction == MANUAL_CLEAR) {
            float dx = Math.abs(x - pathX);
            float dy = Math.abs(y - pathY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                livePath.quadTo(pathX, pathY, (x + pathX) / 2, (y + pathY) / 2);
                pathX = x;
                pathY = y;
            }
        }
    }


    private void touchUp() {
        if (currentAction == MANUAL_CLEAR) {
            livePath.lineTo(pathX, pathY);
            cuts.push(new Pair<>(new Pair<>(livePath, pathPaint), null));
            livePath = new Path();
            undoButton.setEnabled(true);
        }
    }

    public void undo() {
        if (cuts.size() > 0) {

            Pair<Pair<Path, Paint>, Bitmap> cut = cuts.pop();

            if (cut.second != null) {
                undoneCuts.push(new Pair<>(null, imageBitmap));
                this.imageBitmap = cut.second;
            } else {
                undoneCuts.push(cut);
            }

            if (cuts.isEmpty()) {
                undoButton.setEnabled(false);
            }

            redoButton.setEnabled(true);

            invalidate();
        }
        //toast the user
    }

    public void redo() {
        if (undoneCuts.size() > 0) {

            Pair<Pair<Path, Paint>, Bitmap> cut = undoneCuts.pop();

            if (cut.second != null) {
                cuts.push(new Pair<>(null, imageBitmap));
                this.imageBitmap = cut.second;
            } else {
                cuts.push(cut);
            }

            if (undoneCuts.isEmpty()) {
                redoButton.setEnabled(false);
            }

            undoButton.setEnabled(true);

            invalidate();
        }
        //toast the user
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (imageBitmap != null && currentAction != ZOOM) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(ev.getX(), ev.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    touchMove(ev.getX(), ev.getY());
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    invalidate();
                    return true;
            }
        }

        return super.onTouchEvent(ev);
    }

    private void resizeBitmap(int width, int height) {
        if (width > 0 && height > 0 && imageBitmap != null) {
            imageBitmap = BitmapUtility.getResizedBitmap(this.imageBitmap, width, height);
            imageBitmap.setHasAlpha(true);
            invalidate();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
        resizeBitmap(getWidth(), getHeight());
    }

    public Bitmap getCurrentBitmap() {
        return this.imageBitmap;
    }

    public void setAction(DrawViewAction newAction) {
        this.currentAction = newAction;
    }

    public void setStrokeWidth(int strokeWidth) {
        pathPaint = new Paint(pathPaint);
        pathPaint.setStrokeWidth(strokeWidth);
    }

    public void setLoadingModal(View loadingModal) {
        this.loadingModal = loadingModal;
    }

    private static class AutomaticPixelClearingTask extends AsyncTask<Integer, Void, Bitmap> {

        private WeakReference<DrawView> drawViewWeakReference;

        AutomaticPixelClearingTask(DrawView drawView) {
            this.drawViewWeakReference = new WeakReference<>(drawView);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            drawViewWeakReference.get().loadingModal.setVisibility(VISIBLE);
            drawViewWeakReference.get().cuts.push(new Pair<>(null, drawViewWeakReference.get().imageBitmap));
        }

        @Override
        protected Bitmap doInBackground(Integer... points) {
            Bitmap oldBitmap = drawViewWeakReference.get().imageBitmap;

            int colorToReplace = oldBitmap.getPixel(points[0], points[1]);

            int width = oldBitmap.getWidth();
            int height = oldBitmap.getHeight();
            int[] pixels = new int[width * height];
            oldBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            int rA = Color.alpha(colorToReplace);
            int rR = Color.red(colorToReplace);
            int rG = Color.green(colorToReplace);
            int rB = Color.blue(colorToReplace);

            int pixel;

            // iteration through pixels
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    // get current index in 2D-matrix
                    int index = y * width + x;
                    pixel = pixels[index];
                    int rrA = Color.alpha(pixel);
                    int rrR = Color.red(pixel);
                    int rrG = Color.green(pixel);
                    int rrB = Color.blue(pixel);

                    if (rA - COLOR_TOLERANCE < rrA && rrA < rA + COLOR_TOLERANCE && rR - COLOR_TOLERANCE < rrR && rrR < rR + COLOR_TOLERANCE &&
                            rG - COLOR_TOLERANCE < rrG && rrG < rG + COLOR_TOLERANCE && rB - COLOR_TOLERANCE < rrB && rrB < rB + COLOR_TOLERANCE) {
                        pixels[index] = Color.TRANSPARENT;
                    }
                }
            }

            Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return newBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            drawViewWeakReference.get().imageBitmap = result;
            drawViewWeakReference.get().undoButton.setEnabled(true);
            drawViewWeakReference.get().loadingModal.setVisibility(INVISIBLE);
            drawViewWeakReference.get().invalidate();
        }
    }
}