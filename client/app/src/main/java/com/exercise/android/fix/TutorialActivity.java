package com.exercise.android.fix;

import android.os.Bundle;
import android.view.View;

public class TutorialActivity extends MyActivity {
    private final int VID = R.id.tutorialActivity;
    private final int MID = R.raw.main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        settle(VID, MID);
        writeSharedPreferences("isNew", "false");
    }

    public void btnUserGuideQuitOnClick(View view) {
        super.onBackPressed();
    }
}
