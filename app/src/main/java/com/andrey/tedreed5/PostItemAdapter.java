package com.andrey.tedreed5;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by andrey on 24.04.15.
 */
public class PostItemAdapter extends ArrayAdapter<PostData> {
    private LayoutInflater inflater;
    private ArrayList<PostData> datas;

    public PostItemAdapter(Context context, int textViewResourceId,
                           ArrayList<PostData> objects) {
        super(context, textViewResourceId, objects);
        // TODO Auto-generated constructor stub
        inflater = ((Activity) context).getLayoutInflater();
        datas = objects;
    }

    static class ViewHolder {
        TextView postTitleView;
        TextView postDateView;
        ImageView postThumbView;
        String postThumbViewURL;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //present the titles in the listView
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.postitem, null);

            viewHolder = new ViewHolder();
            viewHolder.postTitleView = (TextView) convertView
                    .findViewById(R.id.postTitleLabel);
            viewHolder.postDateView = (TextView) convertView
                    .findViewById(R.id.postDateLabel);
            viewHolder.postThumbView = (ImageView) convertView.findViewById(R.id.postThumb);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PostData post = datas.get(position);
        if (post.postThumbUrl != null) {
            viewHolder.postThumbViewURL = post.postThumbUrl;
            //new DownloadImageTask().execute(viewHolder);
        } else {
            //viewHolder.postThumbView.setImageResource(R.drawable.postthumb_loading);
        }


        viewHolder.postTitleView.setText(datas.get(position).postTitle);
        viewHolder.postDateView.setText(datas.get(position).postDate);


        return convertView;
    }
}
