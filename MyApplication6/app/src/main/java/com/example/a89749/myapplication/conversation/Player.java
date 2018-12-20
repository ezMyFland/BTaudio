package com.example.a89749.myapplication.conversation;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static android.content.ContentValues.TAG;

public class Player {
    private PipedInputStream instream;
    private boolean isPlaying ;
    private AudioTrack audioplayer;
    private byte[] buffer;
    private int buffSize;
    private AudioManager audioManager;

    public Player(AudioManager a) {
        isPlaying = false;
        instream = null;
        //初始化播音类
        buffSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffSize*4, AudioTrack.MODE_STREAM);
        audioManager = a;

    }
    //设置管道流，用于接受音频数据
    public void setOutputStream(PipedOutputStream out) throws IOException {
        instream = new PipedInputStream(out);

    }
    public void startPlayAudio(){ //调用之前先调用setOutputStream 函数
        isPlaying = true;

//        audioManager.setWiredHeadsetOn(true);
        audioplayer.play();//开始接受数据流播放
        buffer = new byte[1024];
        while (instream!=null&&isPlaying){
//            audioManager.setMode(AudioManager.MODE_NORMAL);
            try {
                while (instream.available()>0){
                    int size = instream.read(buffer);
                    audioplayer.write(buffer, 0
                            , size);//不断播放数据
//                    Log.v(TAG,"321");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
    }
    public void stopPlay(){//停止播放
        isPlaying = false ;

        Log.v(TAG,"stopP");
        try {
            instream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioplayer.stop();
        audioplayer.release();
//        audioplayer = null;
    }

}
