package iss.workshop.catest01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    List<Drawable> images = new ArrayList<>();
    List<ImageView> imgLoc = new ArrayList<>();
    List<Integer> indexLoc = new ArrayList<>();
    Drawable background;
    TextView textGoesHere;
    long startTime;
    long countUp;
    int correct;
    TextView textView;
    List<Bitmap> input;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadImages();
        //duplicate
        for (int i =0 ;i<6;i++) {
            images.add(images.get(i));
        }

        Intent homePage = new Intent(this, MainActivity.class);

        Chronometer timeElapsed = (Chronometer) findViewById(R.id.chronometer);
        startTime = SystemClock.elapsedRealtime();


        timeElapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                cArg.setText(hh+":"+mm+":"+ss);


            }
        });
        timeElapsed.setBase(SystemClock.elapsedRealtime());
        timeElapsed.start();


        textView = findViewById(R.id.matches);
        textView.setText("0/6 matches");

        // match 6 scores, end game and go back Main Activity

        Intent intentm = new Intent(GameActivity.this,MusicService.class);
        intentm.putExtra("class","game");
        startService(intentm);

        background = getResources().getDrawable(R.drawable.hidden);

        imgLoc.add((ImageView) findViewById(R.id.gameImg1));
        imgLoc.add((ImageView) findViewById(R.id.gameImg2)); // imgLoc ={[gameImge1][gameImg2]}
        imgLoc.add((ImageView) findViewById(R.id.gameImg3));
        imgLoc.add((ImageView) findViewById(R.id.gameImg4));
        imgLoc.add((ImageView) findViewById(R.id.gameImg5));
        imgLoc.add((ImageView) findViewById(R.id.gameImg6));
        imgLoc.add((ImageView) findViewById(R.id.gameImg7));
        imgLoc.add((ImageView) findViewById(R.id.gameImg8));
        imgLoc.add((ImageView) findViewById(R.id.gameImg9));
        imgLoc.add((ImageView) findViewById(R.id.gameImg10));
        imgLoc.add((ImageView) findViewById(R.id.gameImg11));
        imgLoc.add((ImageView) findViewById(R.id.gameImg12));

        //randomize
        Collections.shuffle(imgLoc);

        shuffleBackground();

        for(int i = 0 ; i< 12; i++){
            final ImageView img = imgLoc.get(i);
            if(img != null){
                final int finalI = i;
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Handler handler = new Handler();

                        if(indexLoc.size()<2) {
                            ImageView img = findViewById(view.getId());
                            img.setBackground(images.get(finalI));
                            System.out.println(finalI);
                            indexLoc.add(finalI);
                        }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                check(indexLoc);
                            }
                        },500);
                    }
                });
            }
        }


    }

    public void shuffleBackground(){
        for(int i = 0 ; i< 12;i++){
            ImageView img = imgLoc.get(i);
            img.setBackground(background);
        }
    }
    public void check(List<Integer> input){
        if(input.size() == 2){
            int temp = Math.abs((input.get(0) - input.get(1))) ;
            if(temp == 6){
                System.out.println("Same.....");
                animation= AnimationUtils.loadAnimation(GameActivity.this,R.anim.bounce);
                Intent intentcor = new Intent(GameActivity.this,MusicService.class);
                intentcor.putExtra("class","correct");

                ImageView img1 = imgLoc.get(input.get(0));
                img1.startAnimation(animation);

                img1.setClickable(false);
                ImageView img2 = imgLoc.get(input.get(1));
                img2.startAnimation(animation);
                startService(intentcor);
                img2.setClickable(false);
                indexLoc.clear();
                correct ++;
                TextView textView = findViewById(R.id.matches);
                textView.setText(correct + "/ 6 matches");
                if (correct == 6)
                {
                    Intent intentm = new Intent(GameActivity.this,MusicService.class);
                    intentm.putExtra("class","game");
                    stopService(intentm);

                    new CountDownTimer(8000, 2000) {
                        public void onFinish() {
                            // When timer is finished
                            // Execute your code here
                            finish();
                            Intent homePage = new Intent(GameActivity.this, Splash.class);
                            startActivity(homePage);
                        }

                        public void onTick(long millisUntilFinished) {
                            // millisUntilFinished    The amount of time until finished.

                            Intent intentend = new Intent(GameActivity.this,MusicService.class);
                            intentend.putExtra("class","victory");
                            startService(intentend);
                            Toast.makeText(GameActivity.this,"Game Completed", Toast.LENGTH_SHORT).show();
                            Chronometer timeElapsed = (Chronometer) findViewById(R.id.chronometer);
                            timeElapsed.stop();
                        }
                    }.start();

                }


            }
            else {
                System.out.println("Wrong......");
                ImageView img1 = imgLoc.get(input.get(0));
                img1.setBackground(background);
                ImageView img2 = imgLoc.get(input.get(1));
                img2.setBackground(background);
                indexLoc.clear();
            }
            
        }
    }

    protected void onDestroy(){
        stopService(new Intent(GameActivity.this, MusicService.class));
        super.onDestroy();
    }

    private void loadImages() {
        images = new ArrayList<Drawable>();
        int x = 1;
        while( x < 7 ){
            byte[] byteArray = getIntent().getByteArrayExtra("selectedImgs"+x);
            Bitmap b = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Drawable d = new BitmapDrawable(getResources(), b);
            images.add(d);
            x++;
        }
    }

}