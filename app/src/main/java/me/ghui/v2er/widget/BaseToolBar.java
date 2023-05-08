package me.ghui.v2er.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import me.ghui.v2er.util.ScaleUtils;
import me.ghui.v2er.util.Theme;
import me.ghui.v2er.R;

/**
 * Created by ghui on 21/03/2017.
 */

public class BaseToolBar extends Toolbar {

    static final int MIN_CELL_SIZE = 56; // dips
    static final int MIN_NAVI_ICON_SIZE = 48; // dips
    static final int GENERATED_ITEM_PADDING = 4; // dips
    static final int GENERATED_ITEM_MARGIN = 16; // dips

    public BaseToolBar(Context context) {
        super(context);
        init();
    }

    public BaseToolBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseToolBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() { }

    public void setTitle(String title, String subtitle) {
        super.setTitle(title);
        super.setSubtitle(subtitle);
    }

    @Override
    public void setTitle(int resId) {
        super.setTitle(resId);
    }

    private TextView mTitleView;
    private TextView mSubTitleView;

    private void initTitleViewAndSubTitleView() {
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view instanceof TextView) {
                    if (mTitleView != null) {
                        mSubTitleView = (TextView) view;
                        break;
                    }
                    mTitleView = (TextView) view;
                }
            }
        }
    }

    /**
     * 必须有主标题标题剧中才会生效
     * @param isCenter
     */
    public void setTileCenter(boolean isCenter) {
        if (isCenter) {
            initTitleViewAndSubTitleView();
            if (mTitleView != null) {
                mTitleView.setGravity(Gravity.CENTER);
                Toolbar.LayoutParams layoutParams = (Toolbar.LayoutParams) mTitleView.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.gravity = Gravity.CENTER;
                mTitleView.setLayoutParams(layoutParams);
                if (mSubTitleView != null) {
                    mSubTitleView.setGravity(Gravity.CENTER);
                    Toolbar.LayoutParams subTitleLayoutParams = (Toolbar.LayoutParams) mSubTitleView.getLayoutParams();
                    subTitleLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    subTitleLayoutParams.gravity = Gravity.CENTER;
                    mSubTitleView.setLayoutParams(subTitleLayoutParams);
                }
                float marginStart = 0;
                float marginEnd = 0;
                if (getNavigationIcon() != null) {
                    marginEnd = ScaleUtils.dp(GENERATED_ITEM_MARGIN + GENERATED_ITEM_PADDING * 3);
                } else if (getMenu().size() > 0) {
                    marginStart = ScaleUtils.dp(MIN_CELL_SIZE - GENERATED_ITEM_PADDING * 3);
                }
                setTitleMargin((int) marginStart, 0, (int) marginEnd, 0);
            }
        }
    }

    private View mViewTitleView;

    public void setViewTileCenter(View viewTitleView) {
        this.mViewTitleView = viewTitleView;
            if (mViewTitleView != null) {
                Toolbar.LayoutParams layoutParams = (Toolbar.LayoutParams) mViewTitleView.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.gravity = Gravity.CENTER;
                float marginStart = 0;
                float marginEnd = 0;
                if (getNavigationIcon() != null) {
                    marginEnd = ScaleUtils.dp(GENERATED_ITEM_MARGIN + GENERATED_ITEM_PADDING * 3);
                } else if (getMenu().size() > 0) {
                    marginStart = ScaleUtils.dp(MIN_CELL_SIZE - GENERATED_ITEM_PADDING * 3);
                }
                layoutParams.setMargins((int) marginStart, 0, (int) marginEnd, 0);
                mViewTitleView.setLayoutParams(layoutParams);
            }
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        ViewGroup barParentViewGroup = (ViewGroup) this.getParent();
        if (barParentViewGroup != null) {
            if (barParentViewGroup instanceof AppBarLayout) {
                barParentViewGroup.setElevation(elevation);
            }
        }
    }

    /**
     * 设置默认返回按键, 并默认可用
     */
    public void displayHomeAsUpButton(AppCompatActivity activity) {
        activity.setSupportActionBar(this);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeButtonEnabled(true);
        }
        if (getNavigationIcon() != null) {
            this.getNavigationIcon().setTint(Theme.getColor(R.attr.icon_tint_color, getContext()));
        }
        setNavigationOnClickListener(v -> activity.onBackPressed());
    }

    /**
     * 设置右边菜单
     * 如果调用 displayHomeAsUpButton 会导致此方法失效，可以在onCreateOptionsMenu方法中创建菜单，或
     * 在BaseActivity的attachOptionsMenuRes 方法返回菜单资源
     * @param resId
     */
    @Override
    public void inflateMenu(int resId) {
        super.inflateMenu(resId);
        int tintColor = Theme.getColor(R.attr.icon_tint_color, getContext());
        Menu menu = getMenu();
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.setTint(tintColor);
            }
        }
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        if (onDoubleTapListener != null) {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return onDoubleTapListener.onToolbarDoubleTaped();
                }
            });
            setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        }
    }

    public interface OnDoubleTapListener {
        boolean onToolbarDoubleTaped();
    }

}
