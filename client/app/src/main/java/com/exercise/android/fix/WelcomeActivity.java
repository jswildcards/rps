package com.exercise.android.fix;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.*;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class WelcomeActivity extends MyActivity {
    private final int VID = R.id.welcomeActivity;
    private final int MID = R.raw.welcome;

    private EditText etSettingsUsername;
    private EditText etSettingsDob;
    private EditText etSettingsPhone;
    private EditText etSettingsEmail;
    private Button btnSettingsBack;
    private View settingsPage;
    private DatePickerDialog datePicker;
    private Calendar myCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    private Integer closingVid = null;
    private float weight;
    private boolean inSettingsPage = false;

    private final Runnable switchPage = new Runnable() {
        public void run() {
            boolean isNew = !sp.contains(getString(R.string.username));
            weight = setWeight(closingVid, 0);

            if(isNew) {
                writeSharedPreferences("isNew", "true");
                toSettingsPage.run();
                createDatabase();
                btnSettingsBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exit(v);
                    }
                });
            } else {
                inSettingsPage = false;
                setWeight(R.id.menuPage, weight);
                LinearLayout menuBtns = findViewById(R.id.menuBtns);
                for (int i = 0; i < menuBtns.getChildCount(); i++)
                    anim(menuBtns.getChildAt(i).getId(), WELCOME_FADE_IN, i);
                btnSettingsBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inSettingsPage = false;
                        closingVid = R.id.settingsPage;
                        switchPage.run();
                    }
                });

                etSettingsUsername.setText(sp.getString(getString(R.string.username), ""));
                etSettingsDob.setText(sp.getString(getString(R.string.dob), ""));
                etSettingsPhone.setText(sp.getString(getString(R.string.phone), ""));
                etSettingsEmail.setText(sp.getString(getString(R.string.email), ""));
            }
        }
    };
    private final Runnable toSettingsPage = new Runnable() {
        @Override
        public void run() {
            inSettingsPage = true;
            setWeight(R.id.settingsPage, weight);
            anim(R.id.settingsPage, WELCOME_FADE_IN, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        super.settle(VID, MID);
        anim(R.id.tapToStart, WELCOME_FADE_IN, 0);
        alertInternetMsgIfNecessary();

        btnSettingsBack = findViewById(R.id.btnSettingsBack);
        etSettingsUsername = findViewById(R.id.etSettingsUsername);
        etSettingsPhone = findViewById(R.id.etSettingsPhone);
        etSettingsEmail = findViewById(R.id.etSettingsEmail);
        etSettingsDob = findViewById(R.id.etSettingsDob);
        settingsPage = findViewById(R.id.settingsPage);
        etSettingsDob.setKeyListener(null);

        datePicker = new DatePickerDialog(this);
        datePicker.getDatePicker().setMaxDate(new Date().getTime());
        datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etSettingsDob.setText(dateFormat.format(myCalendar.getTime()));
            }
        });

        Intent intent = getIntent();
        String isEntered = intent.getStringExtra("isEntered");
        if(isEntered != null && isEntered.equals("true"))
            welcomePageOnClick(null);
    }

    @Override
    public void onBackPressed() {
        if(inSettingsPage)
            btnSettingsBack.callOnClick();
        else
            super.onBackPressed();
    }

    public void etSettingsDobOnClick(View v) {
        datePicker.show();
    }

    public void welcomePageOnClick(View v) {
        click();

        if(closingVid == null) {
            closingVid = R.id.welcomePage;
            long time = anim(R.id.welcomePage, WELCOME_FADE_OUT, 0);
            handler.postDelayed(switchPage, time);
        }
    }

    public void btnSettingsConfirmOnClick(View v) {
        click();
        performFullScreen();
        HashMap<String, String> keyValues = new HashMap<>();

        for(View v_inner : settingsPage.getTouchables()) {
            if(v_inner instanceof EditText) {
                String key = ((EditText) v_inner).getHint().toString();
                String value = ((EditText) v_inner).getText().toString().trim();

                if(!checkValid(key, value))
                    return;

                keyValues.put(key, value);
            }
        }

        for (Map.Entry<String, String> entry : keyValues.entrySet())
            writeSharedPreferences(entry.getKey(), entry.getValue());

        closingVid = R.id.settingsPage;
        long time = anim(R.id.settingsPage, WELCOME_FADE_OUT, 0);
        handler.postDelayed(switchPage, time);
    }

    public boolean checkValid(String key, String value) {
        Resources res = getResources();

        String msg = "";
        if(value.length() <= 0)
            msg = "You have not entered:\n" + key;
        else if(key.equals(res.getString(R.string.phone)) && value.length() != 8)
            msg = "You should enter 8 digits for your phone";
        else if(key.equals(res.getString(R.string.email)) && (!value.contains("@") || value.contains(" ")))
            msg ="You have invalid format for email";

        if(msg.length() > 0) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void btnBattleOnClick(View v) {
        if(isInternetAvailabled())
            openActivity(BattleActivity.class, true, false);
        alertInternetMsgIfNecessary();
    }

    public void btnStatisticsOnClick(View v) {
        openActivity(StatisticsActivity.class, false, false);
    }

    public void btnSettingsClearOnClick(View v) {
        for(View v_inner : settingsPage.getTouchables())
            if(v_inner instanceof EditText)
                ((EditText)v_inner).setText("");
    }

    public void btnSettingsOnClick(View v) {
        long time = anim(R.id.menuPage, WELCOME_FADE_OUT, 0);
        weight = setWeight(R.id.menuPage, 0);
        handler.postDelayed(toSettingsPage, time);
    }

    public void btnTutorialOnClick(View view) {
        openActivity(TutorialActivity.class, false, false);
    }
}
