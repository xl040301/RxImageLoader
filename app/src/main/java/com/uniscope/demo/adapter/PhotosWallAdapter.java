package com.uniscope.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.uniscope.demo.R;
import com.uniscope.rximageloader.loader.RxImageLoader;

import java.util.ArrayList;
import java.util.List;

public class PhotosWallAdapter extends BaseAdapter {

    private Context context;
    private List<String> imageUrlList = new ArrayList<>();

    /**
     * 记录每个子项的高度
     */
    private int mItemHeight = 0;

    public PhotosWallAdapter(Context context, List<String> data) {
        this.imageUrlList = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (imageUrlList != null) {
            return imageUrlList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (imageUrlList != null) {
            return imageUrlList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = imageUrlList.get(position);
        //Log.d("majun",position + "-url: " + url);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            RelativeLayout.LayoutParams layoutParams
                    = new RelativeLayout.LayoutParams(mItemHeight, mItemHeight);
            convertView = View.inflate(context, R.layout.photo_layout, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.photo);
            holder.iv.setLayoutParams(layoutParams);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.iv.setTag(url);
        // 设置默认的图片
        holder.iv.setImageResource(R.drawable.empty_photo);
        holder.iv.setBackgroundResource(android.R.color.transparent);
        // 加载数据
        RxImageLoader.getInstance(context).loader(url).compress(true).into(holder.iv);
        return convertView;

    }

    private class ViewHolder {
        ImageView iv;
    }

    /**
     * 设置item子项的大小
     */
    public void setItemSize(int edgeLength) {
        mItemHeight = edgeLength;
    }

}