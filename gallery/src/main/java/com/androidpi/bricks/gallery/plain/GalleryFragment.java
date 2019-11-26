package com.androidpi.bricks.gallery.plain;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import android.view.View;

import com.androidpi.app.bricks.base.activity.BaseFragment;
import com.androidpi.app.bricks.base.activity.RecyclerAdapter;
import com.androidpi.bricks.gallery.ImageFileEntry;
import com.androidpi.bricks.gallery.R;
import com.androidpi.bricks.gallery.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.List;

import layoutbinder.annotations.BindLayout;

@BindLayout(R.layout.fragment_gallery)
public class GalleryFragment extends BaseFragment<FragmentGalleryBinding> {
    public static final String ARG_IMAGE_FILE_ENTRIES = "ARG_IMAGE_FILE_ENTRIES";

    private List<ImageFileEntry> entries;

    private RecyclerAdapter adapter;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance(ArrayList<? extends Parcelable> entries) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_IMAGE_FILE_ENTRIES, entries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entries = getArguments().getParcelableArrayList(ARG_IMAGE_FILE_ENTRIES);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        adapter = new RecyclerAdapter();
//        adapter.register(TakePhotoItemViewHolder.class);
//        adapter.register(ImageFileEntryViewHolder.class);
//        binding.recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        binding.recycler.setAdapter(adapter);
//
//        adapter.clear();
//        adapter.addOne(new TakePhotoItem());
//        adapter.addAll(entries);
//        binding.recycler.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
////            int count = adapter.getItemCount();
////            for(int i = 0; i < count; i++){
////                RecyclerView.ViewHolder holder = binding.content.recycler.findViewHolderForAdapterPosition(i);
////            }
//        });
    }

    public void updateImageFileEntries() {
//        adapter.clear();
//        adapter.addOne(new TakePhotoItem());
//        adapter.addAll(entries);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
