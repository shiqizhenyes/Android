package me.ghui.v2er.module.drawer.star;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Window;

import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import me.ghui.v2er.R;
import me.ghui.v2er.module.base.BaseActivity;
import me.ghui.v2er.util.Utils;

/**
 * Created by ghui on 17/05/2017.
 */

public class StarActivity extends BaseActivity {
    private ArrayList<Fragment> mFragments = new ArrayList<>(2);

    @BindView(R.id.tablayout_main)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.viewpager_main)
    ViewPager mViewPager;

    @Override
    protected int attachLayoutRes() {
        return R.layout.act_star;
    }

    @Override
    protected void configToolBar(Toolbar toolBar) {
        super.configToolBar(toolBar);
        toolBar.setElevation(0);
        Utils.setPaddingForStatusBar(toolBar);
    }

    @Override
    protected void configSystemBars(Window window) {
        Utils.transparentBars(window, Color.TRANSPARENT, getResources().getColor(R.color.transparent_navbar_color));
    }


    @Override
    protected void init() {
        mFragments.add(TopicStarFragment.newInstance());
        mFragments.add(NodeStarFragment.newInstance());
        mSlidingTabLayout.setViewPager(mViewPager, new String[]{"主题", "节点"}, getActivity(), mFragments);
    }
}
