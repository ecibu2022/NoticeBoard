package com.example.noticeboard;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    ArrayList<Uri> imageUrls;
    LayoutInflater layoutInflater;

    public ViewPagerAdapter(Context context, ArrayList<Uri> imageUrls){
        this.context=context;
        this.imageUrls=imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.show_images_layout, container, false);
        ImageView imageView=view.findViewById(R.id.UploadImage);
        imageView.setImageURI(imageUrls.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((RelativeLayout)object).removeView(container);
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }
}
