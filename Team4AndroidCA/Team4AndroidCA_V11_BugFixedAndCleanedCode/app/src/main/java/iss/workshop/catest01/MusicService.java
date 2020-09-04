package iss.workshop.catest01;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
    MediaPlayer player1;
    MediaPlayer player2;
    MediaPlayer end;
    MediaPlayer correct;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    public void onCreate() {
        player1 =MediaPlayer.create(this, R.raw.main_music); //select music file
        player1.setLooping(true); //set looping

        player2 = MediaPlayer.create(this, R.raw.gamemusic); //select music file
        player2.setLooping(true); //set looping

        end= MediaPlayer.create(this, R.raw.victory); //select music file
        end.setLooping(false); //set looping

        correct= MediaPlayer.create(this, R.raw.correct); //select music file
        correct.setLooping(false); //set looping
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getStringExtra("class").equals("main")){
            player1.start();
            return Service.START_NOT_STICKY;
        }
        else if (intent.getStringExtra("class").equals("game")) {
            player2.start();
            return Service.START_NOT_STICKY;
        }
        else if (intent.getStringExtra("class").equals("victory")) {
            end.start();
            return Service.START_NOT_STICKY;
        }

        else if (intent.getStringExtra("class").equals("correct")) {
            correct.start();
            return Service.START_NOT_STICKY;
        }

        return Service.START_NOT_STICKY;
    }


    public void onDestroy() {

        player1.stop();
        player1.release();

        player2.stop();
        player2.release();

        end.stop();
        end.release();

        correct.stop();
        correct.release();

        stopSelf();
        super.onDestroy();
    }
}
