package com.example.a89749.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.a89749.myapplication.conversation.Player;
import com.example.a89749.myapplication.conversation.Recorder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private Button communityButton;
    private Recorder r;
    private Player p ;
    private PipedInputStream in;
    private boolean flag = false;
    private AudioManager audioManager;
    private BluetoothAdapter adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        communityButton = (Button)findViewById(R.id.communityButton);
//        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        flag = false;

        adapter = BluetoothAdapter.getDefaultAdapter();
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        if(!adapter.isEnabled()){//如果蓝牙没有打开，

            Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);

        }


//        audioManager.setBluetoothScoOn(true);
//        audioManager.setSpeakerphoneOn(true);

        audioManager.startBluetoothSco();


    }

    public void communityClick(View v){

        if (!audioManager.isBluetoothScoAvailableOffCall()) {
            Log.v(TAG, "系统不支持蓝牙录音");
            return;
        }
        else if(audioManager.isBluetoothScoOn()){
            Log.v(TAG,"SCO is opened");
        }

        if(flag){
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(false);
            audioManager.stopBluetoothSco();
            audioManager.setMode(AudioManager.MODE_NORMAL);

            Log.v(TAG,audioManager.getRingerMode()+"");
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.v(TAG,audioManager.getRingerMode()+"");

            flag = false;
            r.stopRecord();
            p.stopPlay();
        }
        else{
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.stopBluetoothSco();
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(true);
//            Log.v(TAG,"SCO is opened,recording now"+audioManager.isBluetoothScoOn());

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                    if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                        audioManager.setBluetoothScoOn(true);  //打开SCO
                        if(audioManager.isBluetoothScoOn()){
                            Log.v(TAG,"SCO is opened,recording now");
//                            Log.v(TAG,"SCO is opened,recording now"+audioManager.isSpeakerphoneOn());
//                            audioManager.setRouting();
                        }
                        flag = true;
                        startRecord();
                        unregisterReceiver(this);  //别遗漏
                    }else{//等待一秒后再尝试启动SCO
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        audioManager.startBluetoothSco();
                    }
                }
            }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
        }




    }

    private void startRecord(){
        in = new PipedInputStream();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                    r = new Recorder(MainActivity.this, in,audioManager);
                    r.StartAudioData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                PipedOutputStream pout = new PipedOutputStream();
                p = new Player(audioManager);
                try {
                    p.setOutputStream(pout);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                            p.startPlayAudio();
                        }
                    }).start();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                int size = 0 ;
                try {
                    while (true){
                        while (in.available()>0){
                            size = in.read(buffer);
                            pout.write(buffer, 0, size);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
