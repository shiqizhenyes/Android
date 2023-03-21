package me.ghui.v2er.module.topic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import me.ghui.v2er.util.AdvertisementFilterUtil;
import me.ghui.v2er.util.Assets;
import me.ghui.v2er.util.Check;
import me.ghui.v2er.BuildConfig;
import me.ghui.v2er.R;
import me.ghui.v2er.general.App;
import me.ghui.v2er.general.GlideApp;
import me.ghui.v2er.module.gallery.GalleryActivity;
import me.ghui.v2er.module.imgviewer.ImagesInfo;
import me.ghui.v2er.network.BaseConsumer;
import me.ghui.v2er.util.DarkModelUtils;
import me.ghui.v2er.util.FontSizeUtil;
import me.ghui.v2er.util.L;
import me.ghui.v2er.util.RxUtils;
import me.ghui.v2er.util.UriUtils;
import me.ghui.v2er.util.Utils;
import me.ghui.v2er.util.Voast;
import okio.Okio;

public class HtmlView extends WebView {
    private List<String> mImgs = new ArrayList<>();
    private OnHtmlRenderListener onHtmlRenderListener;

    public HtmlView(Context context) {
        super(context);
        init(context);
    }

    public HtmlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setOnHtmlRenderListener(OnHtmlRenderListener onHtmlRenderListener) {
        this.onHtmlRenderListener = onHtmlRenderListener;
    }

    private void init(Context context) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false); // 解决图片不显示
        settings.setDatabaseEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setTextZoom(100);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        setWebViewClient(new V2exWebViewClient(context));
        setVerticalScrollBarEnabled(false);
        addJavascriptInterface(new ImgClickJSInterface(), "imagelistener");
        setBackgroundColor(DarkModelUtils.isDarkMode() ?
                getContext().getResources().getColor(R.color.night_default_page_bg) :
                getContext().getResources().getColor(R.color.default_page_bg));
    }

    public void loadContentView(String contentStr) {
        // add css style to contentView
        String formattedHtml = replaceImageSrc(contentStr);
        String container = Assets.getString("html/v2er.html", getContext());
        container = injectParams(container);
        formattedHtml = container.replace("{injecttedContent}", formattedHtml);
        loadDataWithBaseURL("https://www.v2ex.com", formattedHtml, "text/html", "UTF-8", null);
    }

    private String injectParams(String html) {
        boolean isDark = DarkModelUtils.isDarkMode();
        float fontSize = FontSizeUtil.getHtmlFontSize();
        String params = isDark + ", " + "'" + fontSize + "px" + "'";
        return html.replace("{INJECT_PARAMS}", params);
    }

    /**
     * 处理IMG标签
     * 将src属性值保存到original_src中，将默认的loading图路径赋值给src
     *
     * @param html
     * @return
     */
    private String replaceImageSrc(String html) {
        mImgs.clear();
        Document doc = Jsoup.parse(html);
        Elements es = doc.getElementsByTag("img");
        for (Element e : es) {
            String imgUrl = UriUtils.checkSchema(e.attr("src"));
            if (Check.isEmpty(imgUrl)) {
                continue;
            }
            mImgs.add(imgUrl);
            e.attr("original_src", imgUrl);
            e.attr("src", "file:///android_asset/html/image_holder_loading.gif");
        }
        return doc.body().html();
    }

    public interface OnHtmlRenderListener {
        void onRenderCompleted();
    }

    private Message v2exWebViewClientMsg;
    private V2exWebViewClientHandler v2exWebViewClientHandler;
    private int advertisementFilterMsg = 0x11;
    private WebView v2exWebView;

    private class V2exWebViewClientHandler extends Handler {

        V2exWebViewClientHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                String clearAdvertisementJs = AdvertisementFilterUtil.clearAdvertisementDivJs(getContext());
                if (v2exWebView != null) {
                    v2exWebView.loadUrl(clearAdvertisementJs);
                }
            }catch (Exception ignored) {}
        }

    }


    private class V2exWebViewClient extends WebViewClient {

        V2exWebViewClient(Context context) {
            v2exWebViewClientHandler = new V2exWebViewClientHandler(context.getMainLooper());
            v2exWebViewClientMsg = new Message();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Utils.openWap(request.getUrl().toString(), getContext());
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            v2exWebView = view;
            v2exWebViewClientMsg.what = advertisementFilterMsg;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (v2exWebViewClientHandler != null && v2exWebViewClientMsg != null) {
                v2exWebViewClientHandler.sendMessage(v2exWebViewClientMsg);
            }
            // download the imgs in mImgs list
            downloadImgs();
            if (onHtmlRenderListener != null) {
                HtmlView.this.postDelayed(() -> {
                    ViewGroup parent = (ViewGroup) getParent();
                    parent.setMinimumHeight(0);
                }, 100);
                onHtmlRenderListener.onRenderCompleted();
            }
            addIMGClickListener();
        }

        private void downloadImgs() {
            for (String url : mImgs) {
                L.d("start download image: " + url);
                GlideApp.with(App.get())
                        .downloadOnly()
                        .load(url)
                        .listener(new RequestListener<File>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                                // TODO: 2018/12/23 click load again
                                HtmlView.this.post(() -> {
                                    String localPath = "file:///android_asset/html/image_holder_failed.png";
                                    HtmlView.this.loadUrl("javascript:reloadImg(" + "'" + url + "'" + "," + "'" + localPath + "'" + ");");
                                    Voast.debug("load image failed");
                                });
                                return false;
                            }

                            @SuppressLint("CheckResult")
                            @Override
                            public boolean onResourceReady(File file, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                                L.d("image download finished: " + url);
                                Observable.just(file)
                                        .compose(RxUtils.io_main())
                                        .map(rawFile -> {
                                            String directory = rawFile.getParentFile().getPath();
                                            String name = rawFile.getName().split("\\.")[0] + "." + Utils.getTypeFromImgUrl(url);
                                            // check file is exist
                                            File imgFile = new File(directory, name);
                                            if (!imgFile.exists()) {
                                                try {
                                                    Okio.buffer(Okio.source(rawFile))
                                                            .readAll(Okio.sink(imgFile));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            return "file://" + directory + File.separator + name;
                                        })
                                        .subscribe(new BaseConsumer<String>() {
                                            @Override
                                            public void onConsume(String localPath) {
                                                L.d("reload image: " + localPath);
                                                String encodeUrl = URLEncoder.encode(url);
                                                String js = "javascript:reloadImg(" + "'" + encodeUrl + "'" + "," + "'" + localPath + "'" + ");";
                                                HtmlView.this.loadUrl(js);
                                            }
                                        });
                                return false;
                            }
                        }).submit();
            }
        }

        private void addIMGClickListener() {
            HtmlView.this.post(() -> HtmlView.this.loadUrl("javascript:addClickToImg()"));
        }

    }

    private class ImgClickJSInterface {
        @JavascriptInterface
        public void openImage(int index, String[] imgs) {
            ImagesInfo imagesInfo = new ImagesInfo(index, imgs);
            GalleryActivity.open(imagesInfo, getContext());
        }

    }

}
