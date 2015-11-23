package cbn.cn.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cbn.cn.download.entities.FileInfo;
import cbn.cn.download.service.DownloadService;
import cbn.cn.ndkexample.R;

/**
 * Created by boning on 15/11/21.
 */
public class MyAdapter extends CommonAdapter<FileInfo> {

    public MyAdapter(Context context, List<FileInfo> data) {
        super(context,data);
    }

    @Override
    public void convert(ViewHolder holder, final FileInfo fileInfo) {
        TextView tv = holder.getView(R.id.item_text);
        tv.setText(fileInfo.getFileName());

        ProgressBar pb = holder.getView(R.id.item_pb);
        pb.setMax(100);
        pb.setProgress(fileInfo.getFinished());

        Button btStart = holder.getView(R.id.item_start);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileinfo", fileInfo);
                mContext.startService(intent);
            }
        });
        Button btStop = holder.getView(R.id.item_stop);
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileinfo", fileInfo);
                mContext.startService(intent);
            }
        });
    }

    /**
     * 更新列表项里的进度条
     */
    public void updateProgress(int fileId, int progress) {
        FileInfo fileInfo = mDatas.get(fileId);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }


}
