package com.androidpi.app.bricks.base.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import jetbooster.JetBooster;
import layoutbinder.LayoutBinder;
import layoutbinder.runtime.LayoutBinding;

public abstract class BaseFragment extends Fragment {

    protected LayoutBinding layoutBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JetBooster.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        layoutBinding = LayoutBinder.bind(this, inflater, container, false);
        return layoutBinding.getView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //        if (!hidden) {
        //            if (getActivity() != null && getActivity().getWindow() != null) {
        //                Window window = getActivity().getWindow();
        //                View view = window.getDecorView();
        //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        //                    view.setSystemUiVisibility(
        //                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
        // View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //                }
        //
        //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //
        // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //                    window.setStatusBarColor(Color.TRANSPARENT);
        //                }
        //                // 明亮主题的状态栏，字体为黑色
        //                if (getActivity() != null) {
        //                    if (R.style.AppTheme_NoActionBar_Light == getThemeRes()) {
        //                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //                            view.setSystemUiVisibility(
        //                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //                                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //                                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //                        }
        //                    }
        //                }
        //            }
        //        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (layoutBinding != null) {
            layoutBinding.unbind();
        }
    }

    protected int getThemeRes() {
        if (getActivity() == null) return 0;
        try {
            return getActivity()
                    .getPackageManager()
                    .getActivityInfo(getActivity().getComponentName(), 0)
                    .getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            return getActivity().getApplicationInfo().theme;
        }
    }

    protected int getColorCompat(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorRes, getContext().getTheme());
        } else {
            return getResources().getColor(colorRes);
        }
    }

    protected <T extends ViewModel> T getViewModelOfActivity(Class<T> viewModelClass) {
        return ViewModelProviders.of(getActivity()).get(viewModelClass);
    }

    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        return ViewModelProviders.of(this).get(viewModelClass);
    }

    public void fragmentReplace(@IdRes int id, Fragment fragment) {
        fragmentReplace(id, fragment, false);
    }

    public void fragmentReplace(@IdRes int id, Fragment fragment, boolean addToStack) {
        FragmentManager fm = getChildFragmentManager();
        if (fragment == null) {
            Fragment f = fm.findFragmentById(id);
            if (f != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(f);
                ft.commit();
            }
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(id, fragment);
            if (addToStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }

    public void fragmentAdd(@IdRes int id, Fragment fragment) {
        fragmentAdd(id, fragment, false);
    }

    public void fragmentAdd(@IdRes int id, Fragment fragment, boolean addToStack) {
        FragmentManager fm = getChildFragmentManager();
        if (fragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(id, fragment);
            if (addToStack)
                ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void showDialogFragment(DialogFragment fragment) {
        if (fragment == null)
            return;
        if (!fragment.isAdded()) {
            fragment.show(getChildFragmentManager(), fragment.getClass().getName());
        }
    }

    protected void hide(Fragment f) {
        if (f == null)
            return;
        getChildFragmentManager().beginTransaction().hide(f).commit();
    }

    protected void show(Fragment f) {
        if (f == null)
            return;
        getChildFragmentManager().beginTransaction().show(f).commit();
    }
}
