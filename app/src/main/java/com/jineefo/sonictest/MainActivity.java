package com.jineefo.sonictest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import org.vinuxproject.audio.sonic.AndroidAudioDevice;
import org.vinuxproject.audio.sonic.Sonic;

import java.io.IOException;
import java.io.InputStream;

/**
 * setChannels(int) 设置声道，1 = mono单声道, 2 = stereo立体声
 * setSampleRate(uint) 设置采样率
 * setRate(double) 指定播放速率，原始值为1.0，大快小慢
 * setTempo(double) 指定节拍，原始值为1.0，大快小慢
 * setRateChange(double)、setTempoChange(double) 在原速1.0基础上，按百分比做增量，取值(-50 .. +100 %)
 * setPitch(double) 指定音调值，原始值为1.0
 * setPitchOctaves(double) 在原音调基础上以八度音为单位进行调整，取值为[-1.00,+1.00]
 * setPitchSemiTones(int) 在原音调基础上以半音为单位进行调整，取值为[-12,+12]
 * PCM处理类接口：
 *
 * putSamples(const SAMPLETYPE *samples, uint nSamples) 输入采样数据
 * receiveSamples(SAMPLETYPE *output, uint maxSamples) 输出处理后的数据，需要循环执行
 * flush() 冲出处理管道中的最后一组“残留”的数据，应在最后执行
 */
public class MainActivity extends Activity {
    EditText speedEdit;
    Button play;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        speedEdit = (EditText) findViewById(R.id.speed);
        play=(Button)findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

    public void play()
    {
//        AudioTrackManager.getInstance().startPlay(getResources().openRawResource(R.raw.xpg));
        new Thread(new Runnable()
        {
            public void run()
            {

                float speed = Float.parseFloat(speedEdit.getText().toString());
//                float pitch = Float.parseFloat(pitchEdit.getText().toString());
//                float rate = Float.parseFloat(rateEdit.getText().toString());
                AndroidAudioDevice device = new AndroidAudioDevice(44100, 1);
                Sonic sonic = new Sonic(44100, 1);
                sonic.setSpeed(speed);
                byte samples[] = new byte[4096];
                byte modifiedSamples[] = new byte[2048];
                InputStream soundFile = getResources().openRawResource(R.raw.xxx);
                int bytesRead;

                if(soundFile != null) {
                    sonic.setSpeed(speed);
                    do {
                        try {
                            bytesRead = soundFile.read(samples, 0, samples.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        if(bytesRead > 0) {
                            sonic.putBytes(samples, bytesRead);
                        } else {
                            sonic.flush();
                        }
                        int available = sonic.availableBytes();
                        if(available > 0) {
                            if(modifiedSamples.length < available) {
                                modifiedSamples = new byte[available*2];
                            }
                            sonic.receiveBytes(modifiedSamples, available);
                            device.writeSamples(modifiedSamples, available);
                        }
                    } while(bytesRead > 0);
                    device.flush();
                }
            }
        } ).start();
    }
}