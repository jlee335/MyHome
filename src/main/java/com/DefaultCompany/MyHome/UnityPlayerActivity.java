package com.DefaultCompany.MyHome;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;


import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.unity3d.player.UnityPlayer;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;

public class UnityPlayerActivity extends AppCompatActivity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private Button l_move;
    private Button r_move;
    private Button t_move;
    private Button b_move;
    private Button fab;

    // Override this in your custom UnityPlayerActivity to tweak the command line arguments passed to the Unity Android Player
    // The command line arguments are passed as a string, separated by spaces
    // UnityPlayerActivity calls this from 'onCreate'
    // Supported: -force-gles20, -force-gles30, -force-gles31, -force-gles31aep, -force-gles32, -force-gles, -force-vulkan
    // See https://docs.unity3d.com/Manual/CommandLineArguments.html
    // @param cmdLine the current command line arguments, may be null
    // @return the modified command line string or null
    protected String updateUnityCommandLineArguments(String cmdLine)
    {
        return cmdLine;
    }

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        //OpenCVLoader.initDebug();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("@@@@", "권한 설정 완료");
            } else {
                Log.d("@@@@", "권한 설정 요청");
                ActivityCompat.requestPermissions(UnityPlayerActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);

        mUnityPlayer = new UnityPlayer(this);
        //setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        setContentView(R.layout.activity_main);

        Button rsv = findViewById(R.id.roomScanButton);
        rsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RoomScanActivity.class);
                startActivity(intent);
            }
        });

        FrameLayout layout = (FrameLayout) findViewById(R.id.framelayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        layout.addView(mUnityPlayer.getView(), 0, lp);

        l_move = findViewById(R.id.left_move);
        r_move = findViewById(R.id.right_move);
        t_move = findViewById(R.id.top_move);
        b_move = findViewById(R.id.bottom_move);
        fab = findViewById(R.id.fab);
        Button goCV = findViewById(R.id.button);
        goCV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonDo2();
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonDo();
            }
        });
    }



    public void buttonDo(){
        final Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camIntent,1);
        Log.e("TAB_PRESS", "1");
    }

    public void buttonDo2(){
        final Intent camIntent = new Intent(this,CamActivity.class);
        startActivity(camIntent);
        Log.e("TAB_PRESS", "2");
    }


    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("TAB","Camera return");
        //if (resultCode == RESULT_OK) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");



        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("바닥인지 벽인지 선택하세요").setMessage("(바닥/벽)");

        builder.setPositiveButton("바닥", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Bitmap result = Bitmap.createScaledBitmap(imageBitmap, 36, 36, false);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                mUnityPlayer.UnitySendMessage("Player", "makeFloor", encoded);
            }
        });

        builder.setNegativeButton("벽", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Bitmap result = Bitmap.createScaledBitmap(imageBitmap, 36, 50, false);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                mUnityPlayer.UnitySendMessage("Player", "makeWall", encoded);
            }
        });

        builder.setNeutralButton("cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getApplicationContext(), "cancel Click", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


//            iocustom.sendImage(imageBitmap,getAppContext());
        Log.d("IMAGE","SENT IMAGE");

    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
        mUnityPlayer.newIntent(intent);
        super.onNewIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();

        l_move.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Toast.makeText(getApplicationContext(), "왼쪽으로 이동", Toast.LENGTH_SHORT).show();
               mUnityPlayer.UnitySendMessage("Player", "left", "");
           }
        });

        r_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "오른쪽으로 이동", Toast.LENGTH_SHORT).show();
                mUnityPlayer.UnitySendMessage("Player", "right", "");
            }
        });
        t_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "위쪽으로 이동", Toast.LENGTH_SHORT).show();
                mUnityPlayer.UnitySendMessage("Player", "front", "");
            }
        });

        b_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "아래쪽으로 이동", Toast.LENGTH_SHORT).show();
                mUnityPlayer.UnitySendMessage("Player", "back", "");
            }
        });
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
