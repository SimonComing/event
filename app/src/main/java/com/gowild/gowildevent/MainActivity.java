package com.gowild.gowildevent;

import cn.gowild.annotation.GowildSubscribe;
import cn.gowild.api.GowildEvent;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = new View(this);
        view.setBackgroundColor(Color.YELLOW);
        setContentView(view);
        try {
            // 尽量在application里初始化，避免反复调用
            GowildEvent.getInstance().init(this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 注册
        GowildEvent.getInstance().register(this);
        // 发送
        GowildEvent.getInstance().post("test","something");
    }

    @GowildSubscribe(eventPath = "test")
    public void rev(String params){
        System.out.println("test "+params);
    }



}
