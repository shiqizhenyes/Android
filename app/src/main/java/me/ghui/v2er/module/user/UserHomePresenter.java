package me.ghui.v2er.module.user;

import com.orhanobut.logger.Logger;

import io.reactivex.functions.Consumer;
import me.ghui.v2er.network.APIService;
import me.ghui.v2er.network.GeneralConsumer;
import me.ghui.v2er.network.bean.SimpleInfo;
import me.ghui.v2er.network.bean.UserPageInfo;
import me.ghui.v2er.util.RefererUtils;

/**
 * Created by ghui on 01/06/2017.
 */

public class UserHomePresenter implements UserHomeContract.IPresenter {

    private UserHomeContract.IView mView;

    public UserHomePresenter(UserHomeContract.IView view) {
        mView = view;
    }

    @Override
    public void start() {
        APIService.get().userPageInfo(mView.getUsername())
                .compose(mView.rx())
                .subscribe(new GeneralConsumer<UserPageInfo>(mView) {
                    @Override
                    public void onConsume(UserPageInfo userPageInfo) {
                        mView.fillView(userPageInfo);
                    }
                });
    }

    @Override
    public void blockUser(String url) {
        APIService.get().blockUser(url)
                .compose(mView.rx())
                .subscribe(new GeneralConsumer<SimpleInfo>() {
                    @Override
                    public void onConsume(SimpleInfo simpleInfo) {
                        mView.afterBlockUser(!url.contains("unblock"));
                    }
                });
    }

    @Override
    public void followUser(String userName, String url) {
        APIService.get().followUser(RefererUtils.userReferer(userName), url)
                .compose(mView.rx())
                .subscribe(new GeneralConsumer<UserPageInfo>() {
                    @Override
                    public void onConsume(UserPageInfo userPageInfo) {
                        mView.afterfollowUser(userPageInfo);
                    }
                });
    }
}
