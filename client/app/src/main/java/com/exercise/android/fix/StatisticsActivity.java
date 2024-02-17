package com.exercise.android.fix;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatisticsActivity extends MyActivity {
    private final int VID = R.id.statisticsActivity;
    private final int MID = R.raw.main;
    private final String[] dbHands = { "paper", "scissors", "stone" };
    private final int[] hands = { R.string.paper, R.string.scissors, R.string.stone };
    private final String[] dbStates = { "win", "lose", "draw"};
    private final int[] states = { R.string.win, R.string.lose, R.string.draw };
    private final Runnable exitResult = new Runnable() {
        @Override
        public void run() {
            setWeight(R.id.statisticsOverviewPage, 1);
            setWeight(R.id.statisticsResultPage, 0);
        }
    };

    private TextView oppoName;
    private TextView oppoAge;
    private ImageView oppoHand;
    private ImageView playerHand;
    private TextView result;
    private TextView date;
    private TextView time;

    private boolean inResultPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        super.settle(VID, MID);
        watchHistory();

        oppoName = findViewById(R.id.statisticsOppoName);
        oppoAge = findViewById(R.id.statisticsOppoAge);
        oppoHand = findViewById(R.id.statisticsOppoHand);
        playerHand = findViewById(R.id.statisticsPlayerHand);
        result = findViewById(R.id.statisticsPlayerResult);
        date = findViewById(R.id.statisticsDate);
        time = findViewById(R.id.statisticsTime);
    }

    public void watchHistory() {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
        cursor = db.rawQuery("SELECT gameNo, gameDate, gameTime, result FROM gameslog " +
                "order by gameNo desc;", null);

        while (cursor.moveToNext())
            createRowData();

        if(cursor.getCount() > 0)
            createBarChart(cursor.getCount());

        db.close();
    }

    public void createRowData() {
        int match_parent = LinearLayout.LayoutParams.MATCH_PARENT;
        int wrap_content = LinearLayout.LayoutParams.WRAP_CONTENT;

        LinearLayout history = findViewById(R.id.history);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(match_parent, match_parent));

        for(int i = 0; i < cursor.getColumnCount(); i ++) {
            String insert = cursor.getString(i);
            if(i == cursor.getColumnCount() - 1) {
                for (int j = 0; j < dbStates.length; j++) {
                    if (insert.equals(dbStates[j])) {
                        insert = getString(states[j]);
                        break;
                    }
                }
            }

            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, wrap_content, 1f));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tv.setPadding(0, 0, 0, 10);
            tv.setText(insert);
            tv.setTag(cursor.getInt(0));
            tv.setGravity(Gravity.CENTER);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] imagePlayerHands = { R.drawable.battle_player_paper,
                            R.drawable.battle_player_scissors,
                            R.drawable.battle_player_stone };

                    int[] imageOppoHands = { R.drawable.battle_oppo_paper,
                            R.drawable.battle_oppo_scissors,
                            R.drawable.battle_oppo_stone };

                    String[] info = getInfo(new String[]{v.getTag().toString()});
                    oppoName.setText(getString(R.string.opponentName) + " " + info[0]);
                    oppoAge.setText(getString(R.string.opponentAge) + " " + info[1]);
                    date.setText(getString(R.string.gamedate) + " " + info[5]);
                    time.setText(getString(R.string.gametime) + " " + info[6]);
                    for(int j = 0; j < dbStates.length; j ++) {
                        if(info[4].equals(dbStates[j])) {
                            result.setText(getString(states[j]));
                            break;
                        }
                    }

                    int oppoImage = 0;
                    int playerImage = 0;
                    for(int i = 0; i < hands.length; i ++) {
                        if(dbHands[i].equals(info[2]))
                            oppoImage = imageOppoHands[i];
                        if(dbHands[i].equals(info[3]))
                            playerImage = imagePlayerHands[i];
                    }
                    oppoHand.setImageResource(oppoImage);
                    playerHand.setImageResource(playerImage);

                    inResultPage = true;
                    setWeight(R.id.statisticsOverviewPage, 0);
                    setWeight(R.id.statisticsResultPage, 1);
                    int anim_1 = BATTLE_END_LEFT;
                    int anim_2 = BATTLE_END_RIGHT;
                    if(isPortrait()) {
                        anim_1 = BATTLE_END_UP;
                        anim_2 = BATTLE_END_DOWN;
                    }
                    anim(R.id.statisticsResultUp, anim_1, 0);
                    anim(R.id.statisticsResultDown, anim_2, 0);
                    anim(R.id.statisticsData, BATTLE_VISIBLE, 0);
                }
            });

            layout.addView(tv);
        }

        history.addView(layout);
    }

    public void createBarChart(int totalPlayed) {
        int[] colors = { R.color.paper, R.color.scissors, R.color.stone };

        Paint paint = new Paint();
        Bitmap bg = Bitmap.createBitmap(360, 210, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        float lengthOfOneGame = 200 / totalPlayed;

        float x = 50, y = 20;
        for(int j = 0; j < hands.length; j ++) {
            String stringHand = getString(hands[j]);
            paint.setColor(getColor(colors[j]));
            paint.setShadowLayer(6, 0, 0, getColor(R.color.rect_shadow));
            canvas.drawRect(x, y, x + 6, y + 6, paint);

            paint.setColor(getColor(R.color.text));
            canvas.drawText(stringHand, x + 12, y + 9, paint);

            x += 70;
        }

        x = 50;
        y = 40;
        for(int j = 0; j < dbStates.length; j ++) {
            float percent = 0;
            for(int i = 0; i < dbHands.length; i ++) {
                paint.setColor(getColor(colors[i]));
                paint.setShadowLayer(12, 0, 0, getColor(R.color.rect_shadow));
                int value = getResult(new String[] {dbHands[i], dbStates[j]});

                if(value > 0) {
                    percent += value * 100.0 / totalPlayed;
                    float length = value * lengthOfOneGame;
                    canvas.drawRect(x, y, x + length, y + 30, paint);

                    paint.setColor(getColor(R.color.text));
                    canvas.drawText(String.valueOf(value), x + 3, y + 15, paint);

                    x += length;
                }
            }

            x += 10;
            paint.setColor(getColor(R.color.text));
            String percentFinal = String.format("%.2f", percent);
            canvas.drawText(getString(states[j]) + " " + percentFinal + "%", x, y + 15, paint);

            x = 50;
            y += 50;
        }

        LinearLayout barChart = findViewById(R.id.barChart);
        barChart.setBackground(new BitmapDrawable(getResources(), bg));
    }

    public void backOnClick(View view) {
        onBackPressed();
    }

    public void btnStatisticsBackOnClick(View view) {
        if(inResultPage) {
            inResultPage = false;
            int anim_1 = BATTLE_LEFT;
            int anim_2 = BATTLE_RIGHT;
            if(isPortrait()) {
                anim_1 = BATTLE_OPPO_FADE_OUT;
                anim_2 = BATTLE_PLAYER_FADE_OUT;
            }
            anim(R.id.statisticsData, BATTLE_GONE, 0);
            anim(R.id.statisticsResultUp, anim_1, 0);
            long time = anim(R.id.statisticsResultDown, anim_2, 0);
            handler.postDelayed(exitResult, time);
        }
    }

    @Override
    public void onBackPressed() {
        if(inResultPage)
            btnStatisticsBackOnClick(null);
        else
            super.onBackPressed();
    }
}
