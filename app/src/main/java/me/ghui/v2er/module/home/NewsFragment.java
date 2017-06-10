package me.ghui.v2er.module.home;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import me.ghui.v2er.R;
import me.ghui.v2er.adapter.base.MultiItemTypeAdapter;
import me.ghui.v2er.adapter.base.ViewHolder;
import me.ghui.v2er.general.Navigator;
import me.ghui.v2er.injector.component.DaggerNewsComponent;
import me.ghui.v2er.injector.module.NewsModule;
import me.ghui.v2er.module.base.BaseFragment;
import me.ghui.v2er.module.create.CreateTopicActivity;
import me.ghui.v2er.module.topic.TopicActivity;
import me.ghui.v2er.network.bean.NewsInfo;
import me.ghui.v2er.util.UserUtils;
import me.ghui.v2er.widget.LoadMoreRecyclerView;

/**
 * Created by ghui on 22/03/2017.
 */

public class NewsFragment extends BaseFragment<NewsContract.IPresenter> implements NewsContract.IView,
        MultiItemTypeAdapter.OnItemClickListener {

    @BindView(R.id.common_recyclerview)
    LoadMoreRecyclerView mRecyclerView;
    @BindView(R.id.news_fab_btn)
    FloatingActionButton mNewFab;

    @Inject
    LoadMoreRecyclerView.Adapter<NewsInfo.Item> mAdapter;

    private UpdateUnReadMsgDelegate mUpdateUnReadMsgDelegate;

    public void setUpdateUnReadMsgDelegate(UpdateUnReadMsgDelegate updateUnReadMsgDelegate) {
        mUpdateUnReadMsgDelegate = updateUnReadMsgDelegate;
    }

    public static NewsFragment newInstance() {
        Bundle args = new Bundle();
        NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.frag_news;
    }

    @Override
    protected void startInject() {
        DaggerNewsComponent.builder()
                .appComponent(getAppComponent())
                .newsModule(new NewsModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void init() {
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.addDivider();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnLoadMoreListener(willLoadPage -> {
            Logger.e("onLoadMore.willLoadPage: " + willLoadPage);
            if (!UserUtils.isLogin()) {
                Toast.makeText(getContext(), "登录后才能加载更多", Toast.LENGTH_SHORT).show();
                mRecyclerView.setHasMore(false);
                return;
            }
            mPresenter.loadMore(willLoadPage);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    mNewFab.hide(); // or hideFab(), see below
                else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    mNewFab.show(); // or showFab(), see below
            }
        });
    }

    @Override
    protected PtrHandler attachPtrHandler() {
        return new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mRecyclerView.resetWillLoadPage();
                mPresenter.start();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return super.checkCanDoRefresh(frame, mRecyclerView, header);
            }
        };
    }

    @Override
    public void fillView(NewsInfo newsInfos, boolean isLoadMore) {
        if (mUpdateUnReadMsgDelegate != null) {
            mUpdateUnReadMsgDelegate.updateUnReadMsg(1, newsInfos.getUnReadCount());
        }

        List<NewsInfo.Item> items = newsInfos.getItems();
        mAdapter.setData(items, isLoadMore);
        mRecyclerView.setHasMore(true);
    }

    @OnClick(R.id.news_fab_btn)
    void onNewFabClicked() {
        Navigator.from(getContext()).to(CreateTopicActivity.class).start();
    }

    @Override
    public void onItemClick(View view, ViewHolder holder, int position) {
        TopicActivity.open(mAdapter.getDatas().get(position).getLinkPath(), getContext());
    }
}