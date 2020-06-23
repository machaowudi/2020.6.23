package com.ztian.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer player;
    private Timer timer;
    public MusicService() throws IOException {}

    Field[]fields=R.raw.class.getDeclaredFields();   //获取raw底下的文件


    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();//创建音乐播放器对象
    }
    public void addTimer() {        //添加计时器用于设置音乐播放器中的播放进度条
        if (timer == null) {
            timer = new Timer();     //创建计时器对象
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (player == null) return;
                    int duration = player.getDuration();                //获取歌曲总时长
                    int currentPosition = player.getCurrentPosition();//获取播放进度
                    Message msg = ServiceActivity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至消息对象中
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    ServiceActivity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
            timer.schedule(task, 5, 500);
        }
    }
    class MusicControl extends Binder {

        Random r=new Random();
        int index=r.nextInt(fields.length)+0;
        String name="";
        String save;
        String[] list;
        String nextname="";
        public void play() {
            try {
                player.reset();//重置音乐播放器
                //加载多媒体文件
                /*for (int i=0;i<fields.length;i++){//点击收藏中的内容
                    if (fields[i].getName().equals(save)){index=i;}

                }*/
                /*player = MediaPlayer.create(getApplicationContext(), fields[index].getInt(R.raw.class));
                name=fields[index].getName();
                player.start();//播放音乐
                */

                //从高翔端传来两个参数，一个为空，一个不为空，用不为空的那一个Emotion
                AssetManager am = getAssets();
                list=am.list("happy/");
                AssetFileDescriptor afd=am.openFd("happy/"+list[index]);
                name=list[index].substring(0,list[index].indexOf("."));
                if (index==list.length-1) {
                    nextname = list[0].substring(0, list[0].indexOf("."));
                }
                else {nextname=list[index+1].substring(0,list[index+1].indexOf("."));}
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                player.prepare();
                player.start();
                addTimer();     //添加计时器
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            next();

                        }
                    });
                    //Toast.makeText(MusicService.this,"有文件",Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void pausePlay() {

            player.pause();           //暂停播放音乐
        }
        public void continuePlay() {

            player.start();           //继续播放音乐
        }
        public void seekTo(int progress) {
            player.seekTo(progress);//设置音乐的播放位置
        }
        public void next(){
            index++;
             if(index==fields.length){
                 index=0;
             }
             if(player.isPlaying()){
                 player.stop();
             }
             play();

        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player == null) return;
        if (player.isPlaying()) player.stop();//停止播放音乐
        player.release();                         //释放占用的资源
        player = null;                            //将player置为空
    }
}
