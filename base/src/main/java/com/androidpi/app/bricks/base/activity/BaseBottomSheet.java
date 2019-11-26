package com.androidpi.app.bricks.base.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

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

import com.androidpi.app.bricks.base.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import jetbooster.JetBooster;
import layoutbinder.LayoutBinder;
import layoutbinder.runtime.LayoutBinding;
import timber.log.Timber;

public class BaseBottomSheet extends BottomSheetDialogFragment implements ViewTreeObserver.OnGlobalLayoutListener {

    protected LayoutBinding layoutBinding;
    private boolean canceledOnTouchOutside = true;
    protected BottomSheetBehavior behavior;
    private boolean hideable = true;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        View view = getView();
        super.onActivityCreated(savedInstanceState);
        if (view != null) {
            View bottomSheet = (View) (view.getParent());
            if (bottomSheet != null) {
                try {
                    this.behavior = BottomSheetBehavior.from(bottomSheet);
                    if (!hideable) {
                        behavior.setHideable(false);
                        behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                }
            }
            FrameLayout content = view.getRootView().findViewById(R.id.design_bottom_sheet);
            if (content != null) {
                content.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        if (behavior == null || getView() == null) return;
        if (!hideable) {
            behavior.setPeekHeight(getView().getHeight());
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (layoutBinding != null) {
            layoutBinding.unbind();
        }
        if (getView() != null) {
            getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);

        return dialog;
    }

    private boolean isShowing = false;

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShowing) return;
        isShowing = true;
        super.show(manager, tag);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShowing = false;
        super.onDismiss(dialog);
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
            if (addToStack) ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void showDialogFragment(DialogFragment fragment) {
        if (fragment == null) return;
        if (!fragment.isAdded()) {
            fragment.show(getChildFragmentManager(), fragment.getClass().getName());
        }
    }

    protected void hide(Fragment f) {
        if (f == null) return;
        getChildFragmentManager().beginTransaction().hide(f).commit();
    }

    protected void show(Fragment f) {
        if (f == null) return;
        getChildFragmentManager().beginTransaction().show(f).commit();
    }

    public void setHideable(boolean hideable) {
        this.hideable = hideable;
    }
}
