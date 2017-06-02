package me.ghui.v2ex.module.drawer.care;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import me.ghui.v2ex.R;
import me.ghui.v2ex.adapter.base.MultiItemTypeAdapter;
import me.ghui.v2ex.general.Navigator;
import me.ghui.v2ex.injector.component.DaggerSpecialCareComponent;
import me.ghui.v2ex.injector.module.SpecialCareModule;
import me.ghui.v2ex.module.base.BaseActivity;
import me.ghui.v2ex.module.topic.TopicActivity;
import me.ghui.v2ex.network.bean.CareInfo;
import me.ghui.v2ex.util.UriUtils;
import me.ghui.v2ex.widget.LoadMoreRecyclerView;

/**
 * Created by ghui on 16/05/2017.
 */

public class SpecialCareActivity extends BaseActivity<SpecialCareContract.IPresenter> implements SpecialCareContract.IView,
        LoadMoreRecyclerView.OnLoadMoreListener, MultiItemTypeAdapter.OnItemClickListener {

    @BindView(R.id.common_recyclerview)
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
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        String id = UriUtils.getLastSegment(mAdapter.getDatas().get(position).getLink());
        Navigator.from(getContext())
                .to(TopicActivity.class)
                .putExtra(TopicActivity.TOPIC_ID_KEY, id)
                .start();
    }
}
