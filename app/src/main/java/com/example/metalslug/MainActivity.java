package com.example.metalslug;

import android.app.Activity;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import static android.view.ViewGroup.LayoutParams.*;

public class MainActivity extends Activity {
    // 定义住布局内的容器：FrameLayout
    public static FrameLayout mainLayout = null;
    // 主布局的布局参数
    public static FrameLayout.LayoutParams mainLP = null;
    // 定义资源管理的核心类
    public static Resources res = null;
    public static MainActivity mainActivity = null;
    // 定义成员变量记录游戏窗口的宽度、高度
    public static int windowWidth;
    public static int windowHeight;
    // 游戏窗口的主程序界面
    public static GameView mainView = null;
    // 播放背景音乐的 MediaPlayer
    private MediaPlayer player;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics metrics = new DisplayMetrics();
        // 获取屏幕高度、宽度
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        windowHeight = metrics.heightPixels; // 屏幕高度
        windowWidth = metrics.widthPixels; // 屏幕宽度
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        res = getResources();
        // 加载 activity_main.xml 界面设计文件
        setContentView(R.layout.activity_main);
        // 获取 activity_main.xml 界面设计文件 中 id 为 mainLayout 的组件
        mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
        // 创建 GameView 组件
        mainView = new GameView(this.getApplicationContext(), GameView.STAGE_INIT);
        mainLP = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        mainLayout.addView(mainView, mainLP);
        // 播放背景音乐
        player = MediaPlayer.create(this, R.raw.background);
        player.setLooping(true);
        player.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 当游戏恢复时，如果没有播放背景音乐，开始播放背景音乐
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 当游戏暂停时，暂停播放背景音乐
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }
}
