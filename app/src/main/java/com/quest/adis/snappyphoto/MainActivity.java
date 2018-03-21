package com.quest.adis.snappyphoto;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView bunnyEars;
    ImageView sunGlasses;
    ImageView cap;

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private int mode = NONE;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float scalediff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        bunnyEars = findViewById(R.id.bunnyEars);
        sunGlasses = findViewById(R.id.sunGlasses);
        cap = findViewById(R.id.cap);

        FrameLayout.LayoutParams bunnyParams = new FrameLayout.LayoutParams(250, 250);
        bunnyParams.leftMargin = 20;
        bunnyParams.topMargin = 50;
        bunnyParams.bottomMargin = -250;
        bunnyParams.rightMargin = -250;

        FrameLayout.LayoutParams sunGlassesParams = new FrameLayout.LayoutParams(250, 250);
        sunGlassesParams.leftMargin = 300;
        sunGlassesParams.topMargin = 50;
        sunGlassesParams.bottomMargin = -250;
        sunGlassesParams.rightMargin = -250;

        FrameLayout.LayoutParams capParams = new FrameLayout.LayoutParams(250, 250);
        capParams.leftMargin = 600;
        capParams.topMargin = 50;
        capParams.bottomMargin = -250;
        capParams.rightMargin = -250;

        bunnyEars.setLayoutParams(bunnyParams);
        bunnyEars.setOnTouchListener(touchListener);

        sunGlasses.setLayoutParams(sunGlassesParams);
        sunGlasses.setOnTouchListener(touchListener);

        cap.setLayoutParams(capParams);
        cap.setOnTouchListener(touchListener);

    }

    public void openImage(View view) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imagePreview = findViewById(R.id.imagePreview);
            imagePreview.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {

        FrameLayout.LayoutParams params;
        int startWidth;
        int startHeight;
        float dx = 0, dy = 0, x = 0, y = 0;
        float angle = 0;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            final ImageView v = (ImageView) view;

            ((BitmapDrawable) v.getDrawable()).setAntiAlias(true);

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    startWidth = params.width;
                    startHeight = params.height;
                    dx = motionEvent.getRawX() - params.leftMargin;
                    dy = motionEvent.getRawY() - params.topMargin;
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(motionEvent);
                    if(oldDist > 10f) {

                        mode = ZOOM;
                    }
                    d = rotation(motionEvent);
                    break;
                case MotionEvent.ACTION_UP:
                    break;

                case  MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {

                        x = motionEvent.getRawX();
                        y = motionEvent.getRawY();

                        params.leftMargin = (int) (x - dx);
                        params.topMargin = (int) (y - dy);

                        params.rightMargin = 0;
                        params.bottomMargin = 0;
                        params.rightMargin = params.leftMargin + (1 * params.width);
                        params.bottomMargin = params.topMargin + (1 * params.height);

                        v.setLayoutParams(params);


                    } else if (mode == ZOOM)  {
                        if (motionEvent.getPointerCount() == 2) {
                            newRot = rotation(motionEvent);
                            float r = newRot - d;
                            angle = r;

                            x = motionEvent.getRawX();
                            y = motionEvent.getRawY();

                            float newDist = spacing(motionEvent);
                            if (newDist > 10f) {
                                float scale = newDist / oldDist * v.getScaleX();
                                if (scale > 0.6) {
                                    scalediff = scale;
                                    v.setScaleX(scale);
                                    v.setScaleY(scale);
                                }
                            }
                            v.animate().rotationBy(angle).setDuration(0).setInterpolator(
                                    new LinearInterpolator()
                            ).start();
                            x = motionEvent.getRawX();
                            y = motionEvent.getRawY();
                            params.leftMargin = (int) ((x - dx) + scalediff);
                            params.topMargin = (int) ((y - dy) + scalediff);
                            params.rightMargin = 0;
                            params.bottomMargin = 0;
                            params.rightMargin = params.leftMargin + (5 * params.width);
                            params.bottomMargin = params.topMargin + (10 * params.height);
                            v.setLayoutParams(params);
                        }
                    }
                    break;
            }
            return true;
        }
    };

    private float spacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    private float rotation(MotionEvent event) {

        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);

    }

}
