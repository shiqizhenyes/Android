package me.ghui.v2er.module.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.appbar.AppBarLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.r0adkll.slidr.model.SlidrInterface;
import com.trello.rxlifecycle2.LifecycleTransformer;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.Stack;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.ObservableTransformer;
import me.ghui.v2er.util.Check;
import me.ghui.v2er.util.Flurry;
import me.ghui.v2er.util.Theme;
import me.ghui.v2er.R;
import me.ghui.v2er.bus.Bus;
import me.ghui.v2er.bus.event.DayNightModeEvent;
import me.ghui.v2er.general.App;
import me.ghui.v2er.general.Navigator;
import me.ghui.v2er.general.SlideBackManager;
import me.ghui.v2er.injector.component.AppComponent;
import me.ghui.v2er.module.home.MainActivity;
import me.ghui.v2er.module.login.LoginActivity;
import me.ghui.v2er.module.login.TwoStepLoginActivity;
import me.ghui.v2er.module.settings.UserManualActivity;
import me.ghui.v2er.network.APIService;
import me.ghui.v2er.network.GeneralError;
import me.ghui.v2er.network.ResultCode;
import me.ghui.v2er.network.bean.TwoStepLoginInfo;
import me.ghui.v2er.util.DarkModelUtils;
import me.ghui.v2er.util.L;
import me.ghui.v2er.util.RxUtils;
import me.ghui.v2er.util.UserUtils;
import me.ghui.v2er.util.Utils;
import me.ghui.v2er.util.Voast;
import me.ghui.v2er.widget.BaseToolBar;
import me.ghui.v2er.widget.dialog.ConfirmDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by ghui on 05/03/2017.
 */

public abstract class BaseActivity<T extends BaseContract.IPresenter> extends RxActivity implements BaseContract.IView,
        IBindToLife, IBackHandler, BaseToolBar.OnDoubleTapListener {
    public static long FIRST_LOADING_DELAY = 100;
    @Inject
    public T mPresenter;
    protected FrameLayout mRootView;
    protected ViewGroup mContentView;
    @Nullable
    protected BaseToolBar mToolbar;
    protected AppBarLayout mToolbarWrapper;
    protected View mLoadingView;
    private Stack<IBackable> mBackables;
    private long mFirstLoadingDelay = FIRST_LOADING_DELAY;
    private Runnable mDelayLoadingRunnable;
    @Nullable
    protected SlidrInterface mSlidrInterface;
    protected DayNightModeEvent mDayNightModeEvent;
    private boolean displayStatusBarArea = true;

    protected static String KEY(String key) {
        return Utils.KEY(key);
    }

    public void setFirstLoadingDelay(long delay) {
        mFirstLoadingDelay = delay;
    }

    /**
     * bind a layout resID to the content of this page
     *
     * @return layout res id
     */
    @LayoutRes
    protected abstract int attachLayoutRes();


    /**
     * 显示状态栏区域
     * 在attachToolbar调用，或者之前
     * @param displayStatusBarArea
     */
    public void displayStatusBarArea(boolean displayStatusBarArea) {
        this.displayStatusBarArea = displayStatusBarArea;
    }

    /**
     * Set a default Toolbar, if you don't want certain page to have a toolbar,
     * just return null;
     * The toolbar must has a id and value is : inner_toolbar
     *
     * @return
     */
    protected BaseToolBar attachToolbar() {
        int layoutId = attachToolBar() == 0 ? R.layout.appbar_wrapper_toolbar : attachToolBar();
        mToolbarWrapper = (AppBarLayout) getLayoutInflater().inflate(layoutId, null);
        BaseToolBar baseToolBar = mToolbarWrapper.findViewById(R.id.inner_toolbar);
        if (baseToolBar != null) {
            baseToolBar.setTitleTextColor(Theme.getColor(R.attr.icon_tint_color, this));
            baseToolBar.setSubtitleTextColor(Theme.getColor(R.attr.icon_tint_color, this));
            return baseToolBar;
        } else  {
            return null;
        }
    }

    @LayoutRes
    protected int attachToolBar() {
        return 0;
    }

    /**
     * config toolbar here
     */
    protected void configToolBar(BaseToolBar toolBar) {
        if (toolBar == null) return;
        toolBar.setTitle(getTitle());
        toolBar.setNavigationOnClickListener(view -> {
            if (isTaskRoot()) finishToHome();
            else onBackPressed();
        });
        toolBar.setOnDoubleTapListener(this);
    }

    @Override
    public boolean onToolbarDoubleTaped() {
        RecyclerView recyclerView = $(R.id.base_recyclerview);
        if (recyclerView != null) {
            // todo
//            recyclerView.smoothScrollToPosition(0);
            recyclerView.scrollToPosition(0);
            return true;
        }
        return false;
    }

    protected void setTitle(String title, String subTitle) {
        if (mToolbar != null) {
            mToolbar.setTitle(title, subTitle);
        }
    }

    protected void setTitle(String title) {
        setTitle(title, null);
    }

    public @MenuRes int attachOptionsMenuRes() {
        return 0;
    }

    /**
     * 配置右侧可选菜单
     * @param menu
     */
    public void configOptionsMenu(Menu menu) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (attachOptionsMenuRes() != 0) {
            getMenuInflater().inflate(attachOptionsMenuRes(), menu);
            configOptionsMenu(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * if you want to support ptr, then attach a PtrHandler to it
     *
     * @return PtrHandler
     */
    protected SwipeRefreshLayout.OnRefreshListener attachOnRefreshListener() {
        return null;
    }

    @Override
    public void handleBackable(IBackable backable) {
        if (mBackables == null) {
            mBackables = new Stack<>();
        }
        if (!mBackables.contains(backable)) {
            mBackables.push(backable);
        }
    }

    @Override
    public IBackable popBackable(IBackable backable) {
        if (mBackables != null && mBackables.contains(backable)) {
            mBackables.remove(backable);
        }
        return backable;
    }

    @Override
    public void onBackPressed() {
        if (Check.notEmpty(mBackables)) {
            mBackables.pop().onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isBackableEmpty() {
        return Check.isEmpty(mBackables);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bus.unRegister(this);
        if (mBackables != null) {
            mBackables.clear();
        }
    }

    /**
     * push a fragment to the fragment stack
     *
     * @param fragment
     */
    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(mRootView.getId(), fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    /**
     * just add a fragment to the top of the activity, unbackable
     *
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(mRootView.getId(), fragment)
                .commitAllowingStateLoss();
    }

    /**
     * init Dagger2 injector
     */
    protected void startInject() {

    }

    /**
     * parse the intent extra data
     */
    protected void parseExtras(Intent intent) {

    }

    /**
     * init views in this page
     */
    protected void init() {

    }


    /**
     * init theme here
     */
    protected void initTheme() {
        int dayNightMode = DarkModelUtils.getMode();
        switch (dayNightMode) {
            case DarkModelUtils.DARK_MODE:
                setTheme(R.style.NightTheme);
                break;
            case DarkModelUtils.DEFAULT_MODE:
            default:
                setTheme(R.style.DayTheme);
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
        edge2edge();
        Bus.register(this);
        setContentView(onCreateRootView());
        if (supportSlideBack()) {
            mSlidrInterface = configSlideBack();
        }
        ButterKnife.bind(this);
        startInject();
        parseExtras(getIntent());
        if (supportShareElement()) {
            postponeEnterTransition();
        }
        configToolBar(mToolbar);
        init();
        autoLoad();
    }

    private void edge2edge() {
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onToolbarDoubleTaped();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    /**
     * 刷新当前页面的日夜模式
     */
    protected abstract void reloadMode(@DarkModelUtils.DayNightMode int mode);

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DayNightModeEvent event) {
        L.d("need change mode to: " + event.getModeName());
        mDayNightModeEvent = event.copy();
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        if (mDayNightModeEvent != null) {
            int changeToMode = mDayNightModeEvent.getMode();
            mDayNightModeEvent = null;
            reloadMode(changeToMode);
        }
    }

    protected SlidrInterface configSlideBack() {
        return SlideBackManager.attach(this);
    }

    protected boolean supportSlideBack() {
        return true;
    }

    protected boolean supportShareElement() {
        return false;
    }

    protected void autoLoad() {
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    /**
     * 查找Appbar
     * @param viewGroup
     */
    private void findAppbar(ViewGroup viewGroup) {
        if (viewGroup instanceof BaseToolBar) {
            mToolbar = (BaseToolBar) viewGroup;
        } else {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof ViewGroup) {
                    findAppbar((ViewGroup) childView);
                } else {
                    continue;
                }
            }
        }
    }

    protected ViewGroup onCreateRootView() {
        if ((mToolbar = attachToolbar()) != null) {
            LinearLayout rootView = new LinearLayout(this);
            rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rootView.setOrientation(LinearLayout.VERTICAL);
            rootView.addView(mToolbarWrapper == null ? mToolbar : mToolbarWrapper, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mContentView = rootView;
        }

        ViewGroup viewBelowToolbar;
        if (attachOnRefreshListener() != null) {
            SwipeRefreshLayout ptrLayout = new SwipeRefreshLayout(this);
            View content = getLayoutInflater().inflate(attachLayoutRes(), ptrLayout, false);
            ptrLayout.addView(content);
            ptrLayout.setOnRefreshListener(attachOnRefreshListener());
            viewBelowToolbar = ptrLayout;
        } else {
            viewBelowToolbar = (ViewGroup) getLayoutInflater().inflate(attachLayoutRes(), null);
        }

        if (mContentView != null) { //has toolbar
            mContentView.addView(viewBelowToolbar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            mContentView = viewBelowToolbar;
            mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        mRootView = new FrameLayout(this);
        mRootView.setId(R.id.act_root_view_framelayout);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRootView.addView(mContentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (displayStatusBarArea) {
            mRootView.setFitsSystemWindows(true);
        } else {
            mRootView.setFitsSystemWindows(false);
            findAppbar(mRootView);
            if (mToolbar != null) {
                Utils.setPaddingForStatusBar(mToolbar);
            }
        }
        mRootView.setBackgroundColor(pageColor());
        return mRootView;
    }

    @ColorInt
    protected int pageColor() {
        return Theme.getColor(R.attr.page_bg_color, this);
    }

    @Nullable
    protected SwipeRefreshLayout getPtrLayout() {
        if (attachOnRefreshListener() == null) return null;
        SwipeRefreshLayout ptrLayout;
        if (mToolbar != null) {
            ptrLayout = (SwipeRefreshLayout) mContentView.getChildAt(1);
        } else {
            ptrLayout = (SwipeRefreshLayout) mContentView;
        }
        return ptrLayout;
    }

    protected AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }

    protected AppComponent getAppComponent() {
        return App.get().getAppComponent();
    }

    protected View onCreateLoadingView() {
        if (mLoadingView == null) {
            mLoadingView = LayoutInflater.from(this).inflate(R.layout.base_loading_view, mRootView, false);
            mRootView.addView(mLoadingView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            mLoadingView.bringToFront();
        }
        return mLoadingView;
    }

    @Override
    public void showLoading() {
        if (mLoadingView != null && mLoadingView.getVisibility() == View.VISIBLE) return;
        if (getPtrLayout() != null && getPtrLayout().isRefreshing()) return;
        onCreateLoadingView();
        if (mFirstLoadingDelay > 0) {
            mDelayLoadingRunnable = () -> {
                if (mFirstLoadingDelay != 0) {
                    mFirstLoadingDelay = 0;
                    mLoadingView.setVisibility(View.VISIBLE);
                    L.d("delay show loading");
                }
            };
            delay(mFirstLoadingDelay, mDelayLoadingRunnable);
        } else {
            L.d("show loading");
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (getPtrLayout() != null) {
            getPtrLayout().setRefreshing(false);
        }
        if (mLoadingView != null) {
            if (mFirstLoadingDelay != 0) {
                //first loading doesn't show, yet
                if (mDelayLoadingRunnable != null) {
                    cancleRunnable(mDelayLoadingRunnable);
                    mFirstLoadingDelay = 0;
                    mDelayLoadingRunnable = null;
                }
            }
            mLoadingView.setVisibility(View.INVISIBLE);
        }
        L.d(" hide loading");
    }

    protected void toast(@StringRes int msgId) {
        toast(getString(msgId));
    }

    @Override
    public void toast(String msg) {
        Voast.show(msg);
    }

    public void toast(String msg, boolean isToastLong) {
        Voast.show(msg, isToastLong);
    }

    @Override
    public <K> LifecycleTransformer<K> bindToLife() {
        return bindToLifecycle();
    }

    @Override
    public <K> ObservableTransformer<K, K> rx() {
        return rx(this);
    }

    @Override
    public <K> ObservableTransformer<K, K> rx(int page) {
        return this.rx(page == 1 ? this : null);
    }

    @Override
    public <K> ObservableTransformer<K, K> rx(IViewLoading viewLoading) {
        return RxUtils.rxActivity(this, viewLoading);
    }

    @Override
    public void delay(long millisecond, Runnable runnable) {
        mContentView.postDelayed(runnable, millisecond);
    }

    protected void post(Runnable runnable) {
        delay(0, runnable);
    }

    protected void cancleRunnable(Runnable runnable) {
        mContentView.removeCallbacks(runnable);
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    @Override
    public void handleError(GeneralError generalError) {
        if (supportShareElement()) {
            startPostponedEnterTransition();
        }
        if (generalError.getErrorCode() == ResultCode.LOGIN_EXPIRED || generalError.getErrorCode() == ResultCode.LOGIN_NEEDED) {
            handleNotLoginError(generalError.getErrorCode(), generalError.getMessage());
        } else if (generalError.getErrorCode() == ResultCode.REDIRECT_TO_HOME) {
            // TODO 回到首页不一定是错误
            new ConfirmDialog.Builder(this)
                    .title("遇到错误")
                    .msg("可能的原因：\n" +
                            "1. 新注册用户无回复及发帖权限\n" +
                            "2. 查看的帖子已被删除\n" +
                            "3. 查看的帖子需要登录或你无查看权限\n" +
                            "4. 其它可能的账户权限问题")
                    .positiveText("回到首页", dialog -> {
                        Navigator.from(BaseActivity.this).setFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP).to(MainActivity.class).start();
                        finish();
                    }).negativeText("查看详情", dialog -> {
                Navigator.from(BaseActivity.this).to(UserManualActivity.class).start();
                finish();
            }).build().show();
        } else if (generalError.getErrorCode() == ResultCode.LOGIN_TWO_STEP) {
            String once = APIService.fruit().fromHtml(generalError.getResponse(), TwoStepLoginInfo.class).getOnce();
            TwoStepLoginActivity.open(once, getActivity());
        } else {
            hideLoading();
            toast(generalError.toast());
        }
    }

    protected void handleNotLoginError(int errCode, String errorMsg) {
        toast(errorMsg);
        UserUtils.clearLogin();
        Navigator.from(getContext())
                .to(LoginActivity.class).start();
    }

    public void scheduleStartPostponedTransition(final View sharedElement) {
        if (sharedElement == null) return;
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    protected void finishToHome() {
        if (!MainActivity.isAlive) {
            Navigator.from(getActivity())
                    .addFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .to(MainActivity.class).start();
            super.finish();
            return;
        }
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
