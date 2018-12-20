package com.example.a89749.myapplication.conversation;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static android.content.ContentValues.TAG;

public class Recorder{
    private AudioRecord audioRecord;
    private Context context;
    private boolean isRecording = false ;
    private PipedOutputStream outstream ;//利用管道传输数据
    private AudioManager audioManager;

    public Recorder(Context context , PipedInputStream instream,AudioManager a) throws IOException {
        this.context  = context;
        //初始化管道流 用于向外传输数据
        outstream = new PipedOutputStream();
        outstream.connect(instream);
        this.audioManager = a;
    }
    public void StartAudioData(){//得到录音数据
        int frequency = 16000;
        @SuppressWarnings("deprecation")
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                frequency, channelConfiguration, audioEncoding, bufferSize);

        byte[]buffer;



        audioRecord.startRecording();//开始录音
        isRecording = true;

        int bufferReadSize = 1024;
        while (isRecording){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            buffer = new byte[bufferSize];
            audioRecord.read(buffer, 0, bufferReadSize);
//            Log.v(TAG,""+bufferSize);
            try {
                this.audioManager.setBluetoothScoOn(true);
                outstream.write(buffer, 0, bufferReadSize);
//                Log.v(TAG,"123");
//                Log.v(TAG,buffer.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void stopRecord(){//停止录音
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
//        audioRecord = null;
        Log.v(TAG,"stopR");
        try {
            outstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
