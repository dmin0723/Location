package com.dengmin.location.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.dengmin.location.R;
import com.dengmin.location.fragment.CompassFragment;
import com.dengmin.location.fragment.ContentFragment;
import com.dengmin.location.fragment.GalleryFragment;
import com.dengmin.location.fragment.MapFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;

public class MainActivity extends ActionBarActivity implements ViewAnimator.ViewAnimatorListener {

    @Bind(R.id.left_drawer)
    LinearLayout linearLayout; //这是侧滑菜单
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout; //这是整体的
    @Bind(R.id.toolbar)
    Toolbar toolbar;    //这是标题
    @Bind(R.id.content_frame)
    LinearLayout view; //这是fragment容器
    @Bind(R.id.content_overlay)
    LinearLayout contentOverlay; //上一个fragment视图的截屏？不明白作用

    private ContentFragment contentFragment;
    private ViewAnimator viewAnimator;
    //侧滑开关
    private ActionBarDrawerToggle drawerToggle;
    //这是侧滑的菜单栏列表
    private List<SlideMenuItem> list = new ArrayList<>();

    private int res = R.drawable.first; //这个图片是初始化的图片

    public static boolean isMap = false;
    MapFragment mapFragment = new MapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //黄油刀的绑定
        ButterKnife.bind(this);

        //设置相应的图片
        contentFragment = ContentFragment.newInstance(R.drawable.first);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, contentFragment)
                .commit();

        drawerLayout.setScrimColor(Color.TRANSPARENT);
        //设置标题栏
        setActionBar();
        //创建侧滑菜单，添加相应的图片
        createMenuList();

        viewAnimator = new ViewAnimator<SlideMenuItem>(this, list, contentFragment, drawerLayout, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Journey");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.left_drawer)
    void left_drawer() {
        Log.d("tag", "execute left_drawer");
        drawerLayout.closeDrawers();
    }

    //创建了相应的侧滑菜单，添加相应的图片
    private void createMenuList() {
        //SlideMenuItem这是导入的第三方库
        //向对应的菜单添加相应的图标

        //这是点击返回
        SlideMenuItem menuItem0 = new SlideMenuItem(ContentFragment.CLOSE, R.drawable.icn_close);
        list.add(menuItem0);
        //这是地图
        SlideMenuItem menuItem = new SlideMenuItem(ContentFragment.MAP, R.drawable.map);
        list.add(menuItem);
        //这是指南针
        SlideMenuItem menuItem2 = new SlideMenuItem(ContentFragment.COMPASS, R.drawable.compass_one);
        list.add(menuItem2);
        //这是拍照
        SlideMenuItem menuItem3 = new SlideMenuItem(ContentFragment.CAMERA, R.drawable.camera);
        list.add(menuItem3);
        //这是相册
        SlideMenuItem menuItem4 = new SlideMenuItem(ContentFragment.PHOTO, R.drawable.photo);
        list.add(menuItem4);
        //这是蓝牙
        SlideMenuItem menuItem5 = new SlideMenuItem(ContentFragment.BLUETOOTH, R.drawable.bluetooth);
        list.add(menuItem5);
        //这是紧急号码
        SlideMenuItem menuItem6 = new SlideMenuItem(ContentFragment.CALL, R.drawable.contact);
        list.add(menuItem6);
        //这是关于
        SlideMenuItem menuItem7 = new SlideMenuItem(ContentFragment.ABOUT, R.drawable.about);
        list.add(menuItem7);
    }

    //添加标题栏
    private void setActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //这是侧滑开关
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //linearLayout是侧滑菜单
                linearLayout.removeAllViews();
                linearLayout.invalidate(); //使无效
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    //onPostCreate( )——Activity彻底运行起来之后的回调方法。
    // 这样的话，我们就可以重写onPostCreate（），来获取某个view的宽高。
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    //讲述设置Android onConfigurationChanged的意义
    //不设置时，横屏导致当前Activity生命周期重新执行一次，竖屏两次
    //只设置为orientation时，横竖屏则只执行一次
    //而设置为orientation|keyboardHidden，则生命周期不再执行，只是执行onConfigurationChanged方法，这样就减少了我们代码的复杂性
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    //这是替代相应的Fragment
    private ScreenShotable replaceFragment(ScreenShotable screenShotable, int topPosition) {
        //更换图片
//        this.res = this.res == R.drawable.content_music ? R.drawable.content_films : R.drawable.content_music;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
        //插补器 加速
        animator.setInterpolator(new AccelerateInterpolator());
        //时间为0.5S
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);
        contentOverlay.setBackgroundDrawable(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
        animator.start();

        ContentFragment contentFragment = ContentFragment.newInstance(this.res);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, contentFragment).commit();
        return contentFragment;
    }

    //选择对应的侧滑menu
    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {
        switch (slideMenuItem.getName()) {
            //点击关闭
            case ContentFragment.CLOSE:
                return screenShotable;
            //点击Book 启动指南针
            case ContentFragment.COMPASS:
                startAnimator(position);
                CompassFragment compassFragment = new CompassFragment();
                //替换相应的Fragment页面
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,compassFragment).commit();
                return compassFragment;
            //点击 启动地图的页面
            case ContentFragment.MAP:
               // startActivity(new Intent(this, MapActivity.class));
                //isMap=true;
                startAnimator(position);
                MapFragment mapFragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,mapFragment).commit();
                /*if (isMap){
                    addMenuBtn();
                }*/
                return mapFragment;

            //点击 启动照相功能
            case ContentFragment.CAMERA:
                startActivity(new Intent(this,ActivityCamera.class));
                break;
            //点击 这是拍好的相片
            case ContentFragment.PHOTO:
                startAnimator(position);
                GalleryFragment galleryFragment = new GalleryFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,galleryFragment).commit();
                return galleryFragment;

            //点击 蓝牙
            case ContentFragment.BLUETOOTH:
                startActivity(new Intent(this,BluetoothActivity.class));
                break;

            //点击 紧急联系人
            case ContentFragment.CALL:
                startActivity(new Intent(this, DialActivity.class));
                break;
            //点击 关于
            case ContentFragment.ABOUT:
                startActivity(new Intent(this,AboutActivity.class));
                break;
            default:
                isMap = false;
        }
        return replaceFragment(screenShotable, position);
    }

    //开始动画
    public void startAnimator(int position) {
        //取长宽两者中的最大值
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, 0, position, 0, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        //时间0.5S
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);
        animator.start();
    }

    @Override
    public void disableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(false);

    }

    @Override
    public void enableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();

    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }
}
