package com.androidpi.bricks.gallery;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class DialogFragmentBuilder {

    private static DialogFragmentBuilder mHelper;
    private static FragmentActivity mActivity;

    private DialogFragmentBuilder(Activity activity) {
        mActivity = (FragmentActivity) activity;
    }

    public static DialogFragmentBuilder newInstance(Activity activity) {
        if (mHelper == null || mActivity != activity) {
            return new DialogFragmentBuilder(activity);
        }
        return mHelper;
    }

    public void show(DialogFragment fragment) {
        String tag = fragment.getClass().getName();
        FragmentManager fm = mActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
//		DialogFragment prev = (DialogFragment) fm.findFragmentByTag(tag);
//		if(prev != null){
//			ft.remove(prev);
//		}
        fragment.show(ft, tag);
//		ft.commit();
    }

    public void dimiss(DialogFragment fragment) {
        fragment.dismiss();
    }

    public DialogFragmentBuilder build() {

        return null;
    }

}