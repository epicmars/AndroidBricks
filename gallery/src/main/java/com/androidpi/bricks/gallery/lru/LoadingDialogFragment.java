package com.androidpi.bricks.gallery.lru;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.androidpi.app.bricks.base.activity.BaseDialogFragment;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.DialogLoadingBinding;

import layoutbinder.annotations.BindLayout;

public class LoadingDialogFragment extends BaseDialogFragment {

    private static final String MESSAGE = "message";

    @BindLayout(R.layout.dialog_loading)
    private DialogLoadingBinding binding;

    public LoadingDialogFragment() {
        setStyle(STYLE_NO_TITLE, 0);
        setCancelable(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            binding.tvMsg.setText(getArguments().getString(MESSAGE, getString(R.string.loading)));
        }
    }

    public void setBinding(DialogLoadingBinding binding) {
        this.binding = binding;
    }
}