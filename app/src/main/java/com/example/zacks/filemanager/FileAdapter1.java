package com.example.zacks.filemanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by zacks on 2018/1/14.
 */

public class FileAdapter1 extends RecyclerView.Adapter<FileAdapter1.MyViewHolder> {
    //文件名列表
    private List<String> mFileNameList;
    //文件对应的路径列表
    private List<String> mFilePathList;
    private Context mContext;
    private OnItemListener mOnItemListener;

    public FileAdapter1(List<String> mFileNameList, List<String> mFilePathList, Context context) {
        this.mFileNameList = mFileNameList;
        this.mFilePathList = mFilePathList;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder;
        viewHolder = new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_child_recyclerview, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        File mFile = new File(mFilePathList.get(position));

        if (mFileNameList.get(position).equals("BacktoRoot")) {
            //添加返回根目录的按钮
            holder.iv.setBackgroundResource(R.drawable.ic_home_black_48dp);
            holder.tv.setText("返回根目录");
        } else if (mFileNameList.get(position).equals("BacktoUp")) {
            //添加返回上一级菜单的按钮
            holder.iv.setBackgroundResource(R.drawable.ic_arrow_back_black_48dp);
            holder.tv.setText("返回上一级");
        } else if (mFileNameList.get(position).equals("BacktoSearchBefore")) {
            //添加返回搜索之前目录的按钮
            holder.iv.setBackgroundResource(R.drawable.ic_arrow_back_black_48dp);
            holder.tv.setText("返回搜索之前目录");
        } else {
            String fileName = mFile.getName();
            holder.tv.setText(fileName);
            if (mFile.isDirectory()) {
                holder.iv.setBackgroundResource(R.drawable.ic_folder_black_48dp);
            } else {
                String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();//取出文件后缀名并转成小写
                if (fileEnds.equals("m4a") || fileEnds.equals("mp3") || fileEnds.equals("mid") || fileEnds.equals("xmf") || fileEnds.equals("ogg") || fileEnds.equals("wav")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_ondemand_video_black_48dp);
                } else if (fileEnds.equals("3gp") || fileEnds.equals("mp4")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_audiotrack_black_48dp);
                } else if (fileEnds.equals("jpg") || fileEnds.equals("gif") || fileEnds.equals("png") || fileEnds.equals("jpeg") || fileEnds.equals("bmp")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_image_black_48dp);
                } else if (fileEnds.equals("apk")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_apps_black_48dp);
                } else if (fileEnds.equals("txt")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_text_fields_black_48dp);
                } else if (fileEnds.equals("rar")||fileEnds.equals("zip")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_zip_black_48dp);
                } else if (fileEnds.equals("html") || fileEnds.equals("htm") || fileEnds.equals("mht")) {
                    holder.iv.setBackgroundResource(R.drawable.ic_web_black_48dp);
                } else {
                    holder.iv.setBackgroundResource(R.drawable.ic_tag_faces_black_48dp);
                }
            }
        }
        if (mOnItemListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemListener.onItemClick(v, pos);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemListener.onItemLongClick(v, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFilePathList.size();
    }

    //获得当前位置对应的文件名
    public Object getItem(int position) {
        return mFileNameList.get(position);
    }

    //获得当前的位置
    public long getItemId(int position) {
        return position;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.rv_child_image);
            tv = itemView.findViewById(R.id.rv_child_text);
        }


    }

    public interface OnItemListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemListener onItemListener) {
        mOnItemListener = onItemListener;
    }

    public void upData(List<String> name, List<String> path) {
        mFileNameList = name;
        mFilePathList = path;
    }
}
