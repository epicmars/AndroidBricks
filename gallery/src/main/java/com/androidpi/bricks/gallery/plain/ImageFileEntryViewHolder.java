package com.androidpi.bricks.gallery.plain;

import android.app.Activity;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;

import com.androidpi.app.bricks.base.databinding.BindData;
import com.androidpi.bricks.gallery.BaseViewHolder;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.ViewHolderImageFileEntryBinding;

import java.util.List;

@BindData(value = R.layout.view_holder_image_file_entry, dataTypes = ImageFileEntry.class)
public class ImageFileEntryViewHolder extends BaseViewHolder<ImageFileEntry, ViewHolderImageFileEntryBinding> {

    private OnImageFileEntryOperationListener mListener;

    public ImageFileEntryViewHolder(View view) {
        super(view);
        if (context instanceof ImageFileEntryViewHolder.OnImageFileEntryOperationListener) {
            mListener = (ImageFileEntryViewHolder.OnImageFileEntryOperationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void render(ImageFileEntry data, int position) {
        if (null == data)
            return;
        List<ImageFileEntry> childEntries = data.getChildEntries();
        if (data.isDirectory() && (null == childEntries || childEntries.isEmpty()))
            return;
        ImageFileEntry show = data.isDirectory() ? childEntries.get(0) : data;
        if (null == show || null == show.getFile() || !show.getFile().exists())
            return;
        //
        if (data.isDirectory()) {
            binding.vFolder.setVisibility(View.VISIBLE);
            binding.tvName.setText(data.getFile().getName());
            binding.tvNum.setText(String.valueOf(data.getChildEntries().size()));
        } else {
            binding.cbChooseImage.setVisibility(View.VISIBLE);
            binding.cbChooseImage.setChecked(false);
            if (data.getSelectIndex() > 0)
                binding.cbChooseImage.setChecked(true);
        }
        //
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        itemView.setOnClickListener((v) -> mListener.onImageFileEntryClick(data));

        binding.cbChooseImage.setOnCheckedChangeListener((buttonView, isChecked) -> mListener.onImageFileEntryCheck(data, isChecked));
    }

    public interface OnImageFileEntryOperationListener {
        void onImageFileEntryClick(ImageFileEntry entry);

        void onImageFileEntryCheck(ImageFileEntry entry, boolean isChecked);
    }
}
