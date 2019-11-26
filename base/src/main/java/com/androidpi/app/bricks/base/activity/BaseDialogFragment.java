package com.androidpi.app.bricks.base.activity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.androidpi.app.bricks.base.R;

import jetbooster.JetBooster;
import layoutbinder.LayoutBinder;
import layoutbinder.runtime.LayoutBinding;

public class BaseDialogFragment extends AppCompatDialogFragment {

    protected LayoutBinding layoutBinding;
    private boolean canceledOnTouchOutside = true;

    {
        setStyle(STYLE_NO_TITLE, R.style.Dialog);
    }

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
    public void onDestroyView() {
        super.onDestroyView();
        if (layoutBinding != null) {
            layoutBinding.unbind();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        return dialog;
    }

    protected int getColorCompat(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorRes, getContext().getTheme());
        } else {
            return getResources().getColor(colorRes);
        }
    }

    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    protected <T extends ViewModel> T getViewModelOfActivity(Class<T> viewModelClass) {
        return ViewModelProviders.of(getActivity()).get(viewModelClass);
    }

    protected <T extends ViewModel> T getViewModel(Class<T> viewModelClass) {
        return ViewModelProviders.of(this).get(viewModelClass);
    }

    public void show(FragmentManager fm) {
        show(fm, getClass().getName());
    }

    public void fragmentReplace(@IdRes int id, Fragment fragment) {
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
            ft.commit();
        }
    }
}
