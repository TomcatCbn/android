package cbn.cn.mutiItemDownload;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cbn.cn.adapter.MyAdapter;
import cbn.cn.download.entities.FileInfo;
import cbn.cn.download.service.DownloadService;
import cbn.cn.ndkexample.R;

/**
 * Created by boning on 15/11/21.
 */
public class ItemListView extends AppCompatActivity {
    private ListView mListView = null;
    private MyAdapter adapter = null;
    private List<FileInfo> mDatas = null;
    private static final String dlUrl = "http://dldir1.qq.com/qqfile/QQforMac/QQ_V4.0.6.dmg";
    private static final String fileName = "QQ_V4.0.6.dmg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        mDatas = new ArrayList<>();
        //创建文件对象信息
        final FileInfo fileInfo1 = new FileInfo(0, dlUrl, fileName+"01", 0, 0);
        final FileInfo fileInfo2 = new FileInfo(1, dlUrl, fileName+"02", 0, 0);
        final FileInfo fileInfo3 = new FileInfo(2, dlUrl, fileName+"03", 0, 0);
        final FileInfo fileInfo4 = new FileInfo(3, "http://gdown.baidu.com/data/wisegame/fffabacfedb8e21f/QQshurufa_1691.apk", "QQshurufa_1691.apk", 0, 0);
        mDatas.add(fileInfo1);
        mDatas.add(fileInfo2);
        mDatas.add(fileInfo3);
        mDatas.add(fileInfo4);
        adapter = new MyAdapter(this, mDatas);
        mListView = (ListView) findViewById(R.id.item_listview);
        mListView.setAdapter(adapter);
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileinfo");
                adapter.updateProgress(fileInfo.getId(), fileInfo.getFinished()*100/fileInfo.getLength());

            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
                //下载结束
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileinfo");
                adapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(ItemListView.this, mDatas.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
