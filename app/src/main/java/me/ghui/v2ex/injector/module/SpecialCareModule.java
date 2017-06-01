package me.ghui.v2ex.injector.module;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import dagger.Module;
import dagger.Provides;
import me.ghui.v2ex.R;
import me.ghui.v2ex.adapter.base.CommonLoadMoreAdapter;
import me.ghui.v2ex.adapter.base.ViewHolder;
import me.ghui.v2ex.general.Navigator;
import me.ghui.v2ex.injector.scope.PerActivity;
import me.ghui.v2ex.module.drawer.care.SpecialCareActivity;
import me.ghui.v2ex.module.drawer.care.SpecialCareContract;
import me.ghui.v2ex.module.drawer.care.SpecialCarePresenter;
import me.ghui.v2ex.module.node.NodeTopicActivity;
import me.ghui.v2ex.module.user.UserHomeActivity;
import me.ghui.v2ex.network.bean.CareInfo;
import me.ghui.v2ex.widget.LoadMoreRecyclerView;

/**
 * Created by ghui on 16/05/2017.
 */

@Module
public class SpecialCareModule {
    private SpecialCareActivity mView;

    public SpecialCareModule(SpecialCareActivity view) {
        mView = view;
    }


    @Provides
    public LoadMoreRecyclerView.Adapter<CareInfo.Item> provideAdapter() {
        return new CommonLoadMoreAdapter<CareInfo.Item>(mView, R.layout.common_list_item) {
            @Override
            protected void convert(ViewHolder holder, CareInfo.Item item, int position) {
                Glide.with(mContext)
                        .load(item.getAvatar())
                        .into((ImageView) holder.getView(R.id.avatar_img));
                holder.setText(R.id.user_name_tv, item.getUserName());
                holder.setText(R.id.time_tv, item.getTime());
                holder.setText(R.id.tagview, item.getTagName());
                holder.setText(R.id.title_tv, item.getTitle());
                holder.setText(R.id.comment_num_tv, "评论" + item.getComentNum());
            }

            @Override
            protected void bindListener(ViewHolder holder, int viewType) {
                super.bindListener(holder, viewType);
                holder.setOnClickListener(
                        v -> Navigator.from(mContext)
                                .to(UserHomeActivity.class)
                                .putExtra(UserHomeActivity.USER_NAME_KEY,
                                        getItem(holder.index()).getUserName())
                                .start(),
                        R.id.avatar_img, R.id.user_name_tv);
                holder.setOnClickListener(v -> Navigator.from(mContext)
                        .to(NodeTopicActivity.class)
                        .putExtra(NodeTopicActivity.TAG_LINK_KEY,
                                getItem(holder.index()).getTagLink())
                        .start(), R.id.tagview);
            }
        };
    }

    @Provides
    @PerActivity
    public SpecialCareContract.IPresenter providePresenter() {
        return new SpecialCarePresenter(mView);
    }

}