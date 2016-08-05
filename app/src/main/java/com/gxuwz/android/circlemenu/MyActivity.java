package com.gxuwz.android.circlemenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gxuwz.android.menu.listener.OnMenuItemClickListener;
import com.gxuwz.android.menu.widget.SemicircleMenu;

public class MyActivity extends AppCompatActivity {

    private SemicircleMenu mSemicircleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mSemicircleMenu= (SemicircleMenu) findViewById(R.id.sm_activity_my);

        final String[] strings = {
                "场景设置",
                "图片查看",
                "视频查看",
                "通讯录",
                "直播查看",
                "系统设置"
        };

        mSemicircleMenu.setData(new int[]{
             R.mipmap.ic_item_scene_setting ,
             R.mipmap.ic_item_picture,
             R.mipmap.ic_item_video ,
             R.mipmap.ic_item_contacts,
             R.mipmap.ic_item_live,
             R.mipmap.ic_item_system_setting
        }, strings,new int[]{
                R.mipmap.ic_item_bg_scene_setting ,
                R.mipmap.ic_item_bg_picture,
                R.mipmap.ic_item_bg_video ,
                R.mipmap.ic_item_bg_contacts,
                R.mipmap.ic_item_bg_live,
                R.mipmap.ic_item_bg_system_setting
        });

        mSemicircleMenu.addItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void onItemClick(View view, int index) {
                Toast.makeText(MyActivity.this,"当前选择:"+strings[index],Toast.LENGTH_LONG).show();
            }
        });

    }
}
