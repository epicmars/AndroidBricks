package com.androidpi.bricks.gallery;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageFileEntry implements Parcelable {
    private ImageFileEntry parentEntry;
    private List<ImageFileEntry> childEntries;

    private String title;
    private long dateAdded;
    private String displayName;
    private int width;
    private int height;
    private File file;

    /**
     * 选择序号。
     */
    private int selectIndex;

    // 构造函数初始化块
    {
        childEntries = new ArrayList<>();
    }

    public ImageFileEntry() {

    }

    public ImageFileEntry(String title, long dateAdded, String displayName, int width, int height, File file) {
        this.title = title;
        this.dateAdded = dateAdded;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.file = file;
    }

    public boolean isDirectory() {
        return null != file && file.isDirectory();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File path) {
        this.file = path;
    }

    public ImageFileEntry getParentEntry() {
        return parentEntry;
    }

    public void setParentEntry(ImageFileEntry parentEntry) {
        this.parentEntry = parentEntry;
    }

    public List<ImageFileEntry> getChildEntries() {
        return childEntries;
    }

    public void setChildEntries(List<ImageFileEntry> childEntries) {
        this.childEntries = childEntries;
    }

    public void addChildEntry(ImageFileEntry childEntry) {
        if (null != childEntry)
            childEntries.add(childEntry);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }

    public void clearSelectIndex() {
        this.selectIndex = -1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ImageFileEntry{");
        sb.append("parentEntry=").append(parentEntry);
        sb.append(", childEntries=").append(childEntries);
        sb.append(", title='").append(title).append('\'');
        sb.append(", dateAdded=").append(dateAdded);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", file=").append(file);
        sb.append(", selectIndex=").append(selectIndex);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.parentEntry, flags);
        dest.writeTypedList(this.childEntries);
        dest.writeString(this.title);
        dest.writeLong(this.dateAdded);
        dest.writeString(this.displayName);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeSerializable(this.file);
        dest.writeInt(this.selectIndex);
    }

    protected ImageFileEntry(Parcel in) {
        this.parentEntry = in.readParcelable(ImageFileEntry.class.getClassLoader());
        this.childEntries = in.createTypedArrayList(ImageFileEntry.CREATOR);
        this.title = in.readString();
        this.dateAdded = in.readLong();
        this.displayName = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.file = (File) in.readSerializable();
        this.selectIndex = in.readInt();
    }

    public static final Creator<ImageFileEntry> CREATOR = new Creator<ImageFileEntry>() {
        @Override
        public ImageFileEntry createFromParcel(Parcel source) {
            return new ImageFileEntry(source);
        }

        @Override
        public ImageFileEntry[] newArray(int size) {
            return new ImageFileEntry[size];
        }
    };
}