package com.example.leo.channeleditpro;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;



/**
 * Created by leo on 2017/4/1.
 */

public class NomalRecycleAdapter extends RecyclerView.Adapter<NomalRecycleAdapter.DragHolder>{

    private List<String> mDatas;
    private Context mContext;
    private boolean isShow;


    public NomalRecycleAdapter(List<String> Datas, Context context){
        this.mDatas = Datas;
        this.mContext = context;
    }

    @Override
    public DragHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DragHolder(inflater.inflate(R.layout.adapter_draggridview_item, null));
    }

    @Override
    public void onBindViewHolder(DragHolder holder, int position) {
        holder.mTextItem.setText(mDatas.get(position));
        holder.mDeleteItem.setVisibility(isShow? View.VISIBLE: View.GONE);
    }
    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public class DragHolder extends RecyclerView.ViewHolder {
       public TextView mTextItem;
        public ImageView mDeleteItem;
        public DragHolder(View itemView) {
            super(itemView);
            ScreenUtil.initScale(itemView);
            mTextItem = (TextView) itemView.findViewById(R.id.text_item);
            mDeleteItem = (ImageView) itemView.findViewById(R.id.delete_item);
        }
    }
}
