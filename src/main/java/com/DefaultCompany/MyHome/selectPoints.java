package com.DefaultCompany.MyHome;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
    Button confirm;

    ImageView pointer1;
    ImageView pointer2;
    ImageView pointer3;
    ImageView pointer4;

    float[] point1pos;
    float[] point2pos;
    float[] point3pos;
    float[] point4pos;

    String iswall = "true";



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

        point1pos = new float[2];
        point2pos = new float[2];
        point3pos = new float[2];
        point4pos = new float[2];


        confirm = findViewById(R.id.confirmButton);

        rl = (RelativeLayout) findViewById(R.id.myrelout);






















        point1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer1.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer1.setLayoutParams(params);
                point1pos = relcoords;
                Log.d("POINT 1",point1pos[0] + " -- " + point1pos[1]);
            }
        });
        point2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer2.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer2.setLayoutParams(params);
                point2pos = relcoords;
                Log.d("POINT 2",point2pos[0] + " -- " + point2pos[1]);
            }
        });
        point3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer3.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                pointer3.setLayoutParams(params);
                point3pos = relcoords;
                Log.d("POINT 3",point3pos[0] + " -- " + point3pos[1]);
            }
        });
        point4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pointer4.getLayoutParams());
                params.setMargins(round(coords[0]),round(coords[1]),0,0);
                //pointer4.getLayoutParams().
                pointer4.setLayoutParams(params);
                point4pos = relcoords;
                Log.d("POINT 4",point4pos[0] + " -- " + point4pos[1]);
            }
        });

        imageView = (ImageView) findViewById(R.id.selectIV);

        try {
            final Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().openFileInput("myImage"));
            if(imageView == null) Log.e("IMG","ImageView is NULL!!!!!!");
            if (bitmap != null && imageView != null)imageView.setImageBitmap(bitmap);

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //4개의 point 를 이용해 이미지를 조작하자.
                    List<float[]> square = new ArrayList<>();
                    square.add(point1pos);
                    square.add(point2pos);
                    square.add(point3pos);
                    square.add(point4pos);

                    //0: x0 y0
                    //1: xM y0
                    //2: xM yM
                    //3: x0 yM

                    // Center of Mass 기준으로 정렬하자!
                    float mx = 0;
                    float my = 0;
                    List<float[]> sortSquare = new ArrayList<>();
                    sortSquare.add(point1pos);
                    sortSquare.add(point2pos);
                    sortSquare.add(point3pos);
                    sortSquare.add(point4pos);
                    for(float[] pts:square){
                        mx += pts[0];
                        my += pts[1];
                    }
                    mx = mx/4;
                    my = my/4;
                    for(float[] pts : square){
                        Log.d("SQUARE",pts[0] + " - " + pts[1]);
                        if(pts[0] <= mx && pts[1] <= my){ // 00
                            sortSquare.set(0,pts);
                        }else if(pts[0] > mx && pts[1] > my){ //MM
                            sortSquare.set(2,pts);
                        }else if(pts[0] <= mx && pts[1] > my){ //0M
                            sortSquare.set(3,pts);
                        }else if(pts[0] > mx && pts[1] <= my){ //M0
                            sortSquare.set(1,pts);
                        }else{
                            Log.e("SORT ERROR","What happened?");
                        }
                    }
                    for(float[] pt : sortSquare){
                        Log.d("SRT SQUARE",pt[0] + " - " + pt[1]);
                    }


                    //이미지를 Mat 로 다시 변환
                    Mat img = new Mat();
                    Utils.bitmapToMat(bitmap,img);
                    Mat dest = new Mat(400, 400, img.type());

                    Mat src = new MatOfPoint2f(new Point(sortSquare.get(0)[0],sortSquare.get(0)[1]),new Point(sortSquare.get(1)[0],sortSquare.get(1)[1]),new Point(sortSquare.get(2)[0],sortSquare.get(2)[1]),new Point(sortSquare.get(3)[0],sortSquare.get(3)[1]));
                    Mat dst = new MatOfPoint2f(new Point(0,0),new Point(dest.width()-1,0),new Point(dest.width()-1,dest.height()-1),new Point(0,dest.height()-1));
                    Mat transform = Imgproc.getPerspectiveTransform(src,dst);
                    Imgproc.warpPerspective(img,dest,transform,dest.size());

                    //dest 가 우리가 원하는 것!
                    Bitmap res = convertMatToBitMap(dest);
                    imageView.setImageBitmap(res);


                    AlertDialog.Builder builder = new AlertDialog.Builder(selectPoints.this);

                    builder.setTitle("바닥인지 벽인지 선택하세요").setMessage("(바닥/벽)");

                    builder.setPositiveButton("바닥", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Bitmap result = Bitmap.createScaledBitmap(res, 36, 36, false);

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            result.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            //intent
                            iswall = "false";
                            Intent intent = new Intent(getApplicationContext(), com.DefaultCompany.MyHome.UnityPlayerActivity.class);
                            intent.putExtra("room",encoded);
                            intent.putExtra("iswall", iswall);

                            if(intent == null) {
                                Log.d("intent", "null");
                            }
                            startActivity(intent);

                        }
                    });

                    builder.setNegativeButton("벽", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Bitmap result = Bitmap.createScaledBitmap(res, 36, 25, false);

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            result.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            //intent
                            Intent intent = new Intent(getApplicationContext(), com.DefaultCompany.MyHome.UnityPlayerActivity.class);
                            intent.putExtra("room",encoded);
                            intent.putExtra("iswall", iswall);
                            if(intent == null) {
                                Log.d("intent", "null");
                            }
                            startActivity(intent);


                        }
                    });

                    builder.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(getApplicationContext(), "cancel Click", Toast.LENGTH_SHORT).show();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });
        } catch (FileNotFoundException e) {

        }
    }


    private static Bitmap convertMatToBitMap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        int[] pos = new int[2];
        int[] rloffset = new int[2];
        imageView.getLocationOnScreen(pos);
        rl.getLocationOnScreen(rloffset);
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            relcoords = new float[]{event.getX()-pos[0], event.getY()-pos[1]};
            coords = new float[]{event.getX()-rloffset[0]-15, event.getY()-rloffset[1]-15};
            String text = "You click at x = " + (relcoords[0]) + " and y = " + (relcoords[1]);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
        return super.onTouchEvent(event);
    }
}
