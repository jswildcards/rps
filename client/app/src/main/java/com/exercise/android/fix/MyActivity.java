package com.exercise.android.fix;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class MyActivity extends AppCompatActivity {
    // Full Screen Coding (Self-Generated)
    private final Handler mHideHandler = new Handler();
    private View myView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            myView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mHideHandler.postDelayed(mHidePart2Runnable, 30);
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        performFullScreen();
    }

    public void performFullScreen() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 10);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myView.getWindowToken(), 0);
    }

    // Override Coding
    @Override
    protected void onPause() {
        super.onPause();
        bgm.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        performFullScreen();
        bgm.seekTo(0);
        bgm.start();
    }

    private MediaPlayer bgm;

    protected final int WELCOME_FADE_IN = R.anim.anim_welcome_fade_in;
    protected final int WELCOME_FADE_OUT = R.anim.anim_welcome_fade_out;
    protected final int BATTLE_OPPO_FADE_OUT = R.anim.anim_battle_start_oppo_fade_out;
    protected final int BATTLE_PLAYER_FADE_OUT = R.anim.anim_battle_start_player_fade_out;
    protected final int BATTLE_GONE = R.anim.anim_battle_alpha_gone;
    protected final int BATTLE_VISIBLE = R.anim.anim_battle_alpha_visible;
    protected final int BATTLE_END_UP = R.anim.anim_battle_end_up_fade_in;
    protected final int BATTLE_END_DOWN = R.anim.anim_battle_end_down_fade_in;
    protected final int BUTTON_PRESS = R.anim.anim_button_press;
    protected final int BATTLE_LEFT = R.anim.anim_battle_start_left_oppo_fade_out;
    protected final int BATTLE_RIGHT = R.anim.anim_battle_start_right_player_fade_out;
    protected final int BATTLE_END_LEFT = R.anim.anim_battle_end_left_fade_in;
    protected final int BATTLE_END_RIGHT = R.anim.anim_battle_end_right_fade_in;
    protected final int BATTLE_POP_UP = R.anim.anim_battle_pop_up;
    protected final String DB_PATH = "/data/data/com.exercise.android.fix/gameDB";
    protected final Handler handler = new Handler();
    protected SharedPreferences sp;
    protected SharedPreferences.Editor editor;
    protected SQLiteDatabase db;
    protected Cursor cursor;

    public void settle(int vid, int mid) {
        myView = findViewById(vid);

        bgm = MediaPlayer.create(this, mid);
        bgm.setVolume(0.5f, 0.5f);
        bgm.setLooping(true);
        bgm.start();

        sp = getSharedPreferences("MyPrefCode", Context.MODE_PRIVATE);
    }

    public void click() {
        MediaPlayer click = MediaPlayer.create(this, R.raw.click);
        click.start();
    }

    public long anim(int vid, int aid, int delay) {
        View v = findViewById(vid);
        if(v == null)
            return 0;
        Animation anim = AnimationUtils.loadAnimation(this, aid);
        anim.setStartOffset(delay * 200);
        v.startAnimation(anim);

        return anim.getDuration();
    }

    public float setWeight(int vid, float targetWeight) {
        View v = findViewById(vid);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        float weight = params.weight;
        params.weight = targetWeight;
        v.setLayoutParams(params);

        return weight;
    }

    public void writeSharedPreferences(String key, String value) {
        editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void exit(View view) {
        finish();
    }

    public String getAge()
    {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        String[] stringDob = sp.getString(getString(R.string.dob), "").split("-");
        int[] intDob = new int[stringDob.length];
        for(int i = 0; i < stringDob.length; i ++)
            intDob[i] = Integer.parseInt(stringDob[i]);

        dob.set(intDob[2], intDob[1], intDob[0]);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))
                age--;

        return String.valueOf(age);
    }

    public void createDatabase() {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        db.execSQL("DROP TABLE IF EXISTS gameslog;");
        db.execSQL("CREATE TABLE gameslog (gameNo integer primary key autoincrement, gamedate text, " +
                "gametime text, result text, opponentName text, opponentAge text, yourHand text, opponentHand text);");
        db.close();
    }

    public void addPlayedRecord(String oppoName, String oppoAge, String hand, String oppoHand, String result) {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        String date = dateTime.split(" ")[0];
        String time = dateTime.split(" ")[1];

        ContentValues cv = new ContentValues();
        cv.put("gamedate", date);
        cv.put("gametime", time);
        cv.put("result", result);
        cv.put("opponentName", oppoName);
        cv.put("opponentAge", oppoAge);
        cv.put("yourHand", hand);
        cv.put("opponentHand", oppoHand);

        db.insert("gameslog", null, cv);
        db.close();
    }

    public int getResult(String[] conditions) {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
        cursor = db.rawQuery("SELECT * FROM gameslog where yourHand = ? and result = ?;", conditions);
        int count = cursor.getCount();
        db.close();

        return count;
    }

    public String[] getInfo(String[] conditions) {
        String[] columns = { "opponentName", "opponentAge", "opponentHand", "yourHand", "result", "gamedate", "gametime" };
        String[] info = new String[columns.length];

        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
        cursor = db.rawQuery("SELECT * FROM gameslog where gameNo = ?;", conditions);

        cursor.moveToNext();
        for (int i = 0; i < columns.length; i++)
            info[i] = cursor.getString(cursor.getColumnIndex(columns[i]));

        db.close();

        return info;
    }

    public void openActivity(Class<?> cls, boolean isClear, boolean isEntered) {
        Intent intent = new Intent(getBaseContext(), cls);
        if(isClear)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("isEntered", isEntered ? "true" : "false");
        startActivity(intent);
        overridePendingTransition(WELCOME_FADE_IN, WELCOME_FADE_OUT);
    }

    public boolean isInternetAvailabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // Internet can be accessed
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }

    public void alertInternetMsgIfNecessary() {
        // alert internet not connected message
        if(!isInternetAvailabled()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.needInternetAccessHeader));
            alertDialog.setMessage(getString(R.string.needInternetAccessMsg));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
