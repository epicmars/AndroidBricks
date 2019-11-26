package com.androidpi.app.bricks.base.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidpi.app.bricks.base.R;
import com.androidpi.app.bricks.base.activity.BaseFragment;

public class HtmlFragment extends BaseFragment {

    private static String EXTRA_URL = "HtmlFragment.EXTRA_URL";
    private static String EXTRA_DATA = "HtmlActivity.EXTRA_DATA";

    private WebView wv;
    private ProgressBar pb;

    HtmlPageListener listener;

    public interface HtmlPageListener {
        void onLoadTitle(String title);
    }

    public static HtmlFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(EXTRA_URL, url);
        HtmlFragment fragment = new HtmlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static HtmlFragment newInstanceForData(String data) {
        Bundle args = new Bundle();
        args.putString(EXTRA_DATA, data);
        HtmlFragment fragment = new HtmlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = ((HtmlPageListener) context);
        } catch (ClassCastException e) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_html, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wv = view.findViewById(R.id.wv);
        pb = view.findViewById(R.id.progress_horizontal);
        pb.setMax(100);
        WebSettings settings = wv.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setBlockNetworkImage(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCachePath(CacheManager.getInstance(getContext()).webCacheDir().getPath());

        settings.setDatabaseEnabled(true);
        // 设置页面自适应手机屏幕
        // 支持viewport
        settings.setUseWideViewPort(true);
        // 超过屏幕宽度时重新布局为屏幕宽度
        settings.setLoadWithOverviewMode(true);
        wv.setWebChromeClient(
                new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        super.onProgressChanged(view, newProgress);
                        pb.setProgress(newProgress);
                        if (newProgress >= 100) {
                            pb.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        super.onReceivedTitle(view, title);
                        if (listener != null) {
                            listener.onLoadTitle(title);
                        }
                    }
                });
        wv.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Uri uri = Uri.parse(url);
                        if (uri.getScheme() != null && uri.getScheme().equals("tel")) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(uri);
                            if (getContext() != null
                                    && intent.resolveActivity(getContext().getPackageManager())
                                            != null) {
                                startActivity(intent);
                            }
                            return true;
                        }
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view, WebResourceRequest request) {
                        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            if (request.getUrl().getScheme() != null
                                    && request.getUrl().getScheme().equals("tel")) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(request.getUrl());
                                if (getContext() != null
                                        && intent.resolveActivity(getContext().getPackageManager())
                                                != null) {
                                    startActivity(intent);
                                }
                            } else {
                                view.loadUrl(request.getUrl().toString());
                            }
                            return true;
                        } else {
                            view.loadUrl(request.toString());
                        }
                        return super.shouldOverrideUrlLoading(view, request);
                    }

                    @Override
                    public void onReceivedSslError(
                            WebView view, SslErrorHandler handler, SslError error) {
                        super.onReceivedSslError(view, handler, error);
                    }

                    @Override
                    public void onReceivedError(
                            WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                    }

                    @Override
                    public void onReceivedError(
                            WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                    }
                });
        Bundle args = getArguments();
        if (args != null) {
            String url = args.getString(EXTRA_URL);
            if (url != null && !url.isEmpty()) wv.loadUrl(url);
            else {
                String data = args.getString(EXTRA_DATA);
                if (data != null && !data.isEmpty()) wv.loadData(data, "text/html", "utf-8");
            }
        }
    }
}