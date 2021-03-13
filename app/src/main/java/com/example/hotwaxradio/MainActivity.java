package com.example.hotwaxradio;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    final String url320 = "https://streamingv2.shoutcast.com/hotwaxradiocom";
    final String url128 = "http://162.248.92.167:5950/stream?type=http&nocache=104";
    final String urlInfo = "http://162.248.92.167:5950/currentsong?sid=1";//for song name & info
    final String urlWebsite  = "https://www.hotwaxradio.com/";//website to visit in web browser
    String url = "" + url320;//the url to stream audio from. made to hotswap between the 2 qualities
    boolean playing = false;//bool to know if we are already streaming audio
    MediaPlayer player;//the mediaplayer is what plays the audio stream
    ImageView image;//the spinning record image xml element
    TextView songInfo;//song information text xml element
    String songInfoString;//because android throws hissyfit I have to keep this here
    BarVisualizer mVisualizer;//the audio visualizer (the bars on the bottom)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic load the main activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set objects equal to the xml elements we want to manipulate
        image = findViewById(R.id.imageView3);
        songInfo = findViewById(R.id.songInfo);
        mVisualizer = findViewById(R.id.bar);
    }

    //Called when the user taps the play button. this method toggles the audio stream
    public void sendMessage(View view) {
        try {
            if(playing){//if already playing it pauses
                player.pause();
                playing = false;
                image.clearAnimation();
            }
            else{//if not already playing then start playing
                playing = true;
                if(player == null) {
                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                }
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(url);
                player.prepareAsync(); //this is long part
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        player.start();//start radio service
                    }
                });
                //rotate album image
                RotateAnimation rotate = new RotateAnimation(
                        0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotate.setInterpolator(new LinearInterpolator());//smooth rotation
                rotate.setDuration(9500);
                rotate.setRepeatCount(Animation.INFINITE);
                rotate.setFillAfter(true);
                image.startAnimation(rotate);
                //start audio visualizer
                mVisualizer.setAudioSessionId(player.getAudioSessionId());
            }
            //display song info
            updateSongInfo(view);
        }catch (Exception e){//if error happens
            System.out.println("music fucked up and made exception");
            e.printStackTrace();
        }
    }

    public void changeQuality(View view) {//changes url from 320 to 128 and vice reversa
        if(url.equals(url320)){
            url = "" + url128;//yes ik this is hackish and probably inefficient
        }
        else{
            url = "" + url320;
        }
        if(playing){//if url changes while music was playing, send message to stop it, then replay
            sendMessage(view);
            sendMessage(view);
        }
    }

    public void visitWebsite(View view) {//if click on globe it goes to radio website
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlWebsite)));
    }

    //takes html from a webpage and sets textview to display it
    public void updateSongInfo(View view) throws IOException {
        Thread songInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL U = new URL(urlInfo);
                    URLConnection con = U.openConnection();
                    InputStream is = con.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    songInfoString = br.readLine();
                }catch (Exception e){//if error happens
                    System.out.println("song info fucked up and made an exception");
                    e.printStackTrace();
                }
            }
        });
        songInfoThread.start();
        songInfo.setText(songInfoString);//sets text to first line from buffered reader
        //TODO make the text immediately show
    }
}