package com.DefaultCompany.MyHome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;


import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


public class MainActivity extends Activity {
    private UnityPlayer m_UnityPlayer;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the UnityPlayer
        m_UnityPlayer = new UnityPlayer(this);
        int glesMode = m_UnityPlayer.getSettings().getInt("gles_mode", 1);
        boolean trueColor8888 = false;
        m_UnityPlayer.init(glesMode, trueColor8888);

        setContentView(R.layout.activity_main);

        // Add the Unity view
        FrameLayout layout = (FrameLayout) findViewById(R.id.abc);
        LayoutParams lp = new LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layout.addView(m_UnityPlayer.getView(), 0, lp);
















        //UnityPlayer.UnitySendMessage("게임 오브젝트 이름","함수 이름","String 인자");
        //m_UnityPlayer.UnitySendMessage("Player", "front", null);
    }


    public void buttonDo(){
        final Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camIntent,1);
            Log.e("TAB_PRESS", "1");

    }


    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

            Log.e("TAB","Camera return");
            //if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

//            iocustom.sendImage(imageBitmap,getAppContext());
            Log.d("IMAGE","SENT IMAGE");

    }





}
