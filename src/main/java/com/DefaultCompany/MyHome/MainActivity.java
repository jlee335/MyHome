package com.DefaultCompany.MyHome;

import android.os.Bundle;


import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import com.unity3d.player.UnityPlayer;


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
    }
}
