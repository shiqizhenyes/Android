package me.ghui.v2er.module.drawer.care;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;

import javax.inject.Inject;

import butterknife.BindView;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import me.ghui.v2er.R;
import me.ghui.v2er.adapter.base.MultiItemTypeAdapter;
import me.ghui.v2er.adapter.base.ViewHolder;
import me.ghui.v2er.injector.component.DaggerSpecialCareComponent;
import me.ghui.v2er.injector.module.SpecialCareModule;
import me.ghui.v2er.module.base.BaseActivity;
import me.ghui.v2er.module.topic.TopicActivity;
import me.ghui.v2er.network.bean.CareInfo;
import me.ghui.v2er.network.bean.TopicBasicInfo;
import me.ghui.v2er.util.Utils;
import me.ghui.v2er.widget.BaseToolBar;
import me.ghui.v2er.widget.LoadMoreRecyclerView;

/**
 * Created by ghui on 16/05/2017.
 */

public class SpecialCareActivity extends BaseActivity<SpecialCareContract.IPresenter> implements SpecialCareContract.IView,
        LoadMoreRecyclerView.OnLoadMoreListener, MultiItemTypeAdapter.OnItemClickListener {

    @BindView(R.id.base_recyclerview)
    LoadMoreRecyclerView mLoadMoreRecyclerView;
    @Inject
    LoadMoreRecyclerView.Adapter<CareInfo.Item> mAdapter;

    @Override
    protected int attachLayoutRes() {
        return R.layout.common_load_more_recyclerview;
    }

    @Override
    protected void startInject() {
        DaggerSpecialCareComponent.builder()
                .appComponent(getAppComponent())
                .specialCareModule(new SpecialCareModule(this))
                .build().inject(this);
    }

    @Override
    protected void configSystemBars(Window window) {
        Utils.transparentBars(window, Color.TRANSPARENT, getResources().getColor(R.color.transparent_navbar_color));
    }

    @Override
    protected void configToolBar(BaseToolBar toolBar) {
        super.configToolBar(toolBar);
        Utils.setPaddingForStatusBar(toolBar);
    }

    @Override
    protected void init() {
        mLoadMoreRecyclerView.addDivider();
        mLoadMoreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLoadMoreRecyclerView.setAdapter(mAdapter);
        mLoadMoreRecyclerView.setOnLoadMoreListener(this);
        mAdapter.setOnItemClickListener(this);
    }

    @Override
    protected PtrHandler attachPtrHandler() {
        return new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mLoadMoreRecyclerView.resetWillLoadPage();
                mPresenter.start();
            }
        };
    }

    @Override
    public void onLoadMore(int willLoadPage) {
        mPresenter.loadMore(willLoadPage);
    }

    @Override
    public void fillView(CareInfo careInfo, boolean isLoadMore) {
        if (careInfo == null) {
            mAdapter.setData(null);
            return;
        }
        mAdapter.setData(careInfo.getItems(), isLoadMore);
        mLoadMoreRecyclerView.setHasMore(mAdapter.getContentItemCount() < careInfo.getTotal());
    }


    @Override
    public void onItemClick(View view, ViewHolder holder, int position) {
        CareInfo.Item item = mAdapter.getItem(position);
        TopicBasicInfo basicInfo = new TopicBasicInfo.Builder(item.getTitle(), item.getAvatar())
                .author(item.getUserName())
                .tag(item.getTagName())
                .commentNum(item.getComentNum())
                .build();
        TopicActivity.open(mAdapter.getDatas().get(position).getLink(), this, holder.getView(R.id.title_tv), basicInfo);
    }
}
