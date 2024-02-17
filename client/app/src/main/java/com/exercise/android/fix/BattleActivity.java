package com.exercise.android.fix;

import android.media.MediaPlayer;
import android.os.*;
import android.view.View;
import android.widget.*;

import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class BattleActivity extends MyActivity {
    private final int VID = R.id.battleActivity;
    private final int MID = R.raw.battle;
    private final int[] btnHands = { R.id.btnPaper, R.id.btnScissors, R.id.btnStone };

    private final int[] imagePlayerHands = { R.drawable.battle_player_paper,
            R.drawable.battle_player_scissors,
            R.drawable.battle_player_stone };

    private final int[] imageOppoHands = { R.drawable.battle_oppo_paper,
            R.drawable.battle_oppo_scissors,
            R.drawable.battle_oppo_stone };

    private final String[] hands = { "paper", "scissors", "stone" };
    private final String[] results = { "win", "lose", "draw" };
    private final int[] intResults = { R.string.win, R.string.lose, R.string.draw };

    private final Runnable delayBattleAnim = new Runnable () {
        @Override
        public void run() {
            int anim_1 = BATTLE_LEFT;
            int anim_2 = BATTLE_RIGHT;
            if(isPortrait()) {
                anim_1 = BATTLE_OPPO_FADE_OUT;
                anim_2 = BATTLE_PLAYER_FADE_OUT;
            }
            anim(R.id.battleStartOppo, anim_1, 0);
            anim(R.id.battleStartPlayer, anim_2, 0);
            long time = anim(R.id.battleStartBattle, BATTLE_GONE, 0);
            handler.postDelayed(battleStart, time);
        }
    };
    private final Runnable battleStart = new Runnable() {
        @Override
        public void run() {
            float weight = setWeight(R.id.battleStartPage, 0);
            setWeight(R.id.battleMainPage, weight);
            battleTimer.setText(String.valueOf(5));
            handler.postDelayed(timerTick, 1000);
            handler.postDelayed(oppoTimerTick, 1000);
        }
    };
    private final Runnable timerTick = new Runnable() {
        @Override
        public void run() {
            if (!finished) {
                if(Integer.parseInt(battleTimer.getText().toString()) >= 0) {
                    String time = String.valueOf(Integer.parseInt(battleTimer.getText().toString()) - 1);
                    battleTimer.setText(time);
                    handler.postDelayed(timerTick, 1000);
                } else {
                    if (playerHand == null) {
                        int anim_1 = BATTLE_END_LEFT;
                        int anim_2 = BATTLE_END_RIGHT;
                        if (isPortrait()) {
                            anim_1 = BATTLE_END_UP;
                            anim_2 = BATTLE_END_DOWN;
                        }
                        setWeight(R.id.battleMainPage, 0);
                        setWeight(R.id.battleLoseTurnPage, 1);
                        anim(R.id.battleLoseTurnUp, anim_1, 0);
                        anim(R.id.battleLoseTurnDown, anim_2, 0);
                    }
                }
            } else
                battleTimer.setText(getString(R.string.end));
        }
    };
    private final Runnable oppoTimerTick = new Runnable() {
        @Override
        public void run() {
            if (oppoDecideTime -- > 0) {
                oppoState.setImageResource(R.drawable.battle_oppo_thinking);
                handler.postDelayed(oppoTimerTick, 1000);
            } else {
                oppoHand = hands[intOppoHand];
                oppoState.setImageResource(R.drawable.battle_oppo_waiting);
                long time = anim(R.id.oppoState, BATTLE_POP_UP, 0);
                handler.postDelayed(testResult, time + 500);
            }
        }
    };
    private final Runnable showPlayerState = new Runnable() {
        @Override
        public void run() {
            playerState.setVisibility(View.VISIBLE);
            long time = anim(R.id.playerState, BATTLE_VISIBLE, 0);
            handler.postDelayed(testResult, time);
        }
    };
    private final Runnable testResult = new Runnable() {
        @Override
        public void run() {
            if(oppoHand != null && playerHand != null)
                battleResult();
        }
    };
    protected final Runnable beep = new Runnable() {
        @Override
        public void run() {
            if(beepTime -- > 0) {
                sound.seekTo(0);
                sound.start();
                handler.postDelayed(beep, 150);
            }
        }
    };
    protected final Runnable buttonGone = new Runnable() {
        @Override
        public void run() {
            for(int i = 0; i < btnHands.length; i ++) {
                if(btnClicked.getId() == btnHands[i]) {
                    playerState.setImageResource(imagePlayerHands[i]);
                }

                findViewById(btnHands[i]).setVisibility(View.GONE);
                long time = anim(btnHands[i], BATTLE_GONE, 0);
                handler.postDelayed(showPlayerState, time);
            }
        }
    };

    private int intOppoHand;
    private String oppoHand;
    private int intPlayerHand;
    private String playerHand;
    private int oppoDecideTime;
    private boolean finished;
    private String result;
    private int beepTime;
    private String oppoName;
    private String oppoAge;

    private TextView battleTimer;
    private ImageView oppoState;
    private ImageView playerState;
    private TextView playerResult;
    private ImageView playerFinalHand;
    private ImageView oppoFinalHand;
    private MediaPlayer sound;
    private ImageButton btnClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        super.settle(VID, MID);
        startNewGame(null);

        battleTimer = findViewById(R.id.battleTimer);
        oppoState = findViewById(R.id.oppoState);
        playerState = findViewById(R.id.playerState);
        playerResult = findViewById(R.id.playerResult);
        playerFinalHand = findViewById(R.id.playerHand);
        oppoFinalHand = findViewById(R.id.oppoHand);
        sound = MediaPlayer.create(getBaseContext(), R.raw.beep);
    }

    public void startNewGame(View v) {
        if(sp.getString("isNew", "false").equals("true")) {
            writeSharedPreferences("isNew", "false");
            return;
        }

        if(!isInternetAvailabled())
            btnBattleQuitOnClick(v);
        else {
            setWeight(R.id.userGuide, 0);
            setWeight(R.id.battleStartPage, 1);
            new OppoPlayer().execute("Server URL");
        }
    }

    public void beep() {
        beepTime = 3;
        beep.run();
    }

    public void battleReady(View v) {

        btnClicked = (ImageButton)v;
        for(int i = 0; i < btnHands.length; i ++) {
            if (btnClicked.getId() == btnHands[i]) {
                intPlayerHand = i;
                playerHand = hands[i];
                break;
            }
        }
        click();
        findViewById(R.id.battleMidQuit).setEnabled(false);
        long time = anim(v.getId(), BUTTON_PRESS, 0);
        handler.postDelayed(buttonGone, time * 2);
    }

    public void battleResult() {
        int cal = intPlayerHand - intOppoHand;
        int intResult;
        if(cal == 0)
            intResult = 2;
        else if(cal == 1 || cal == -2)
            intResult = 0;
        else
            intResult = 1;

        if(!finished) {
            finished = true;
            result = results[intResult];
            addPlayedRecord(oppoName, oppoAge, playerHand, oppoHand, result);
            setWeight(R.id.battleMainPage, 0);
            setWeight(R.id.battleResultPage, 1);
            playerResult.setText(intResults[intResult]);
            playerFinalHand.setImageResource(imagePlayerHands[intPlayerHand]);
            oppoFinalHand.setImageResource(imageOppoHands[intOppoHand]);
            int anim_1 = BATTLE_END_LEFT;
            int anim_2 = BATTLE_END_RIGHT;
            if (isPortrait()) {
                anim_1 = BATTLE_END_UP;
                anim_2 = BATTLE_END_DOWN;
            }
            anim(R.id.battleResultUp, anim_1, 0);
            anim(R.id.battleResultDown, anim_2, 0);
            anim(R.id.resultData, BATTLE_VISIBLE, 0);
            oppoHand = null;
            playerHand = null;
        }
    }

    public void btnBattleContinueOnClick(View v) {
        click();
        setWeight(R.id.battleResultPage, 0);
        setWeight(R.id.battleStartPage, 1);
        startNewGame(v);
    }

    public void btnBattleQuitOnClick(View view) {
        if(playerHand == null)
            openActivity(WelcomeActivity.class, true, true);
    }

    public void btnUserGuideQuitOnClick(View v) {
        startNewGame(v);
    }

    private class OppoPlayer extends AsyncTask<String, String[], String[]> {
        @Override
        protected String[] doInBackground(String... values) {
            String[] oppoInfo = null;

            try {
                String line, result = "";
                URL url = new URL(values[0]);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.connect();

                InputStream inputStream = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while((line = bufferedReader.readLine()) != null)
                    result += line;
                inputStream.close();

                JSONObject jsonObject = new JSONObject(result);
                String oppoName = jsonObject.getString("name");
                String oppoAge = String.valueOf(jsonObject.getInt("age"));
                String oppoHand = String.valueOf(jsonObject.getInt("hand"));
                oppoInfo = new String[] {oppoName, oppoAge, oppoHand};
            } catch (Exception e) {
                e.printStackTrace();
            }

            return oppoInfo;
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            beep();

            oppoName = s[0];
            oppoAge = s[1];
            // initialize
            ((TextView)findViewById(R.id.oppoName)).setText(getString(R.string.battleName) + oppoName);
            ((TextView)findViewById(R.id.oppoAge)).setText(getString(R.string.battleAge) + oppoAge);
            ((TextView)findViewById(R.id.playerName)).setText(getString(R.string.battleName) +
                    sp.getString(getString(R.string.username), ""));
            ((TextView)findViewById(R.id.playerAge)).setText(getString(R.string.battleAge) + getAge());
            intOppoHand = Integer.parseInt(s[2]);
            for(int i = 0; i < btnHands.length; i ++)
                findViewById(btnHands[i]).setVisibility(View.VISIBLE);
            oppoState.setImageResource(R.drawable.battle_oppo_thinking);
            playerState.setVisibility(View.GONE);
            finished = false;
            oppoDecideTime = (int) (Math.random() * 3);
            findViewById(R.id.battleMidQuit).setEnabled(true);
            handler.postDelayed(delayBattleAnim, 500);
        }
    }
}
