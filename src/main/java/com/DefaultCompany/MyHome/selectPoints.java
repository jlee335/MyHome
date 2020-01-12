package com.DefaultCompany.MyHome;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;

import static java.lang.Math.round;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class selectPoints extends AppCompatActivity {
    Button point1;
    Button point2;
    Button point3;
    Button point4;

    ImageView pointer1;
    ImageView pointer2;
    ImageView pointer3;
    ImageView pointer4;

    ImageView imageView;
    float[] coords;
    float[] relcoords;
    RelativeLayout rl;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_points);

        point1 = findViewById(R.id.point1);
        point2 = findViewById(R.id.point2);
        point3 = findViewById(R.id.point3);
        point4 = findViewById(R.id.point4);

        pointer1 = findViewById(R.id.pointer1);
        pointer2 = findViewById(R.id.pointer2);
        pointer3 = findViewById(R.id.pointer3);
        pointer4 = findViewById(R.id.pointer4);

        rl = (RelativeLayout) findViewById(R.id.myrelout);

        point1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer1.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer1.setLayoutParams(params);
            }
        });
        point2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer2.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer2.setLayoutParams(params);
            }
        });
        point3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer3.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer3.setLayoutParams(params);
            }
        });
        point4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer4.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                //pointer4.getLayoutParams().
                pointer4.setLayoutParams(params);
            }
        });

        imageView = (ImageView) findViewById(R.id.selectIV);

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput("myImage"));


        } catch (FileNotFoundException e) {

        }
        if(imageView == null) Log.e("IMG","ImageView is NULL!!!!!!");
        if (bitmap != null && imageView != null)imageView.setImageBitmap(bitmap);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        int[] pos = new int[2];
        int[] rloffset = new int[2];
        imageView.getLocationOnScreen(pos);
        rl.getLocationOnScreen(rloffset);
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            String text = "You click at x = " + (event.getX()-pos[0]) + " and y = " + (event.getY()-pos[1]);
            relcoords = new float[]{event.getX()-pos[0], event.getY()-pos[1]};
            coords = new float[]{event.getX()-rloffset[0], event.getY()-rloffset[1]};
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        return super.onTouchEvent(event);
    }
}
