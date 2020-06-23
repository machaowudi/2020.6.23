package com.ztian.service;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ServiceActivity extends AppCompatActivity implements View.OnClickListener
{
    int x=0;
    int y=1;
    int resID;
    ArrayList List=new ArrayList();
    private static SeekBar sb;
    private static TextView tv_progress, tv_total;
    public ObjectAnimator animator;  //动画
    private MusicService.MusicControl musicControl;//音乐控制
    MyServiceConn conn;
    Intent intent;
    private boolean isUnbind = false;//记录服务是否被解绑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        init();

    }
    private void init() {
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_total = (TextView) findViewById(R.id.tv_total);
        sb = (SeekBar) findViewById(R.id.sb);
        ImageView iv_music = (ImageView) findViewById(R.id.iv_music);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        intent = new Intent(this, MusicService.class);//创建意图对象
        conn = new MyServiceConn();//创建服务连接对象
        bindService(intent, conn, BIND_AUTO_CREATE);  //绑定服务
        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean
                    fromUser) {                          //滑动条进度改变时，会调用此方法
                if (progress == seekBar.getMax()) { //当滑动条滑到末端时，结束动画
                    //animator.pause();                   //停止播放动画
                    ImageView imageView=(ImageView)findViewById(R.id.iv_music);//更改图片
                    resID = getResources().getIdentifier(musicControl.nextname, "drawable","com.ztian.service");
                    imageView.setImageResource(resID);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//滑动条开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { //滑动条停止滑动时调用
                //根据拖动的进度改变音乐播放进度
                int progress = seekBar.getProgress();//获取seekBar的进度
                musicControl.seekTo(progress);         //改变播放进度
            }
        });


        animator = ObjectAnimator.ofFloat(iv_music, "rotation", 0f, 360.0f);
        animator.setDuration(10000);  //动画旋转一周的时间为10秒
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);  //-1表示设置动画无限循环
    }
    public static Handler handler = new Handler() {//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg) {            //设置进度条的进度问题
            Bundle bundle = msg.getData(); //获取从子线程发送过来的音乐播放进度
            int duration = bundle.getInt("duration");                  //歌曲的总时长
            int currentPostition = bundle.getInt("currentPosition");//歌曲当前进度
            sb.setMax(duration);                //设置SeekBar的最大值为歌曲总时长
            sb.setProgress(currentPostition);//设置SeekBar当前的进度位置
            //歌曲的总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            String strMinute = null;
            String strSecond = null;
            if (minute < 10) {              //如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute; //在分钟的前面加一个0
            } else {
                strMinute = minute + "";
            }
            if (second < 10) {             //如果歌曲的时间中的秒钟小于10
                strSecond = "0" + second;//在秒钟前面加一个0
            } else {
                strSecond = second + "";
            }
            tv_total.setText(strMinute + ":" + strSecond);
            //歌曲当前播放时长
            minute = currentPostition / 1000 / 60;
            second = currentPostition / 1000 % 60;
            if (minute < 10) {             //如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute;//在分钟的前面加一个0
            } else {
                strMinute = minute + "";
            }
            if (second < 10) {               //如果歌曲的时间中的秒钟小于10
                strSecond = "0" + second;  //在秒钟前面加一个0
            } else {
                strSecond = second + "";
            }
            tv_progress.setText(strMinute + ":" + strSecond);

        }
    };
    class MyServiceConn implements ServiceConnection { //用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MusicService.MusicControl) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
    private void unbind(boolean isUnbind){
        if(!isUnbind){                  //判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);      //解绑服务
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:               //暂停按钮点击事件

                if(y==0){
                    findViewById(R.id.btn_pause).setBackgroundResource(R.drawable.start);
                musicControl.pausePlay();     //暂停播放音乐
                animator.pause();
                y=1;
                }              //暂停播放动画
                else {
                    findViewById(R.id.btn_pause).setBackgroundResource(R.drawable.stop);
                    if(x==0){
                        musicControl.play();//播放
                       ImageView imageView=(ImageView)findViewById(R.id.iv_music);//更改图片
                         resID = getResources().getIdentifier(musicControl.name, "drawable","com.ztian.service");
                        imageView.setImageResource(resID);
                        animator.start();
                        x=1;

                    }
                        musicControl.continuePlay();//继续播放
                        animator.start();
                        y = 0;
                }
                Toast.makeText(ServiceActivity.this," "+musicControl.index+musicControl.list.length,Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_next: //下一曲
                musicControl.next();
               findViewById(R.id.btn_pause).setBackgroundResource(R.drawable.stop);
                y=0;

                //换图片
                ImageView imageView=(ImageView)findViewById(R.id.iv_music);
                resID = getResources().getIdentifier(musicControl.name, "drawable","com.ztian.service");
                //Toast.makeText(ServiceActivity.this," "+musicControl.index+"和"+resID,Toast.LENGTH_SHORT).show();
                imageView.setImageResource(resID);
                animator.start();

                //是否收藏了
                if (List.indexOf(musicControl.name) == -1)//如果收藏了，那图标就改变
                {findViewById(R.id.btn_save).setBackgroundResource(R.drawable.save1);}
                else findViewById(R.id.btn_save).setBackgroundResource(R.drawable.save2);//反之，就是没收藏的图标
                break;

            case R.id.btn_save:
                if(musicControl.name!=null) {
                    if (List.indexOf(musicControl.name) == -1) {//收藏，图标变化，加到List中
                        findViewById(R.id.btn_save).setBackgroundResource(R.drawable.save2);
                        List.add(musicControl.name);
                        Toast.makeText(ServiceActivity.this, " " + musicControl.name + "和" + List.size(), Toast.LENGTH_SHORT).show();
                    } else {//取消收藏，图标变化，从List中删除
                        List.remove(musicControl.name);
                        findViewById(R.id.btn_save).setBackgroundResource(R.drawable.save1);
                        Toast.makeText(ServiceActivity.this, " " + musicControl.name + "和" + List.size(), Toast.LENGTH_SHORT).show();
                    }
                }
                else break;

                break;
            case R.id.btn_exit:                //退出按钮点击事件
                unbind(isUnbind);               //解绑服务绑定
                isUnbind = true;                //完成解绑服务
                finish();                         //关闭音乐播放界面
                break;

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbind(isUnbind); //解绑服务
    }
}
