package cbn.cn.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import cbn.cn.download.entities.FileInfo;

/**
 * Created by boning on 15/11/15.
 */
public class DownloadService extends Service {
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final int MSG_INIT = 0;
    private DownloadTask task = null;
    private static final int DEFAULT_THREADS = 3;
    //下载任务的集合
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();



    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileinfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("test", "准备初始化下载文件->"+fileinfo.toString());
            //启动初始化线程
            new InitThread(fileinfo).start();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("test", "准备停止下载任务->"+fileInfo.toString());
            //从集合中取出下载任务
            DownloadTask task = mTasks.get(fileInfo.getId());
            if (task != null) {
                //停止下载任务
                task.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    //线程和我们的Service如何交互,交给Handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i("test", "创建下载文件成功,准备下载->"+fileInfo.toString());
                    //启动下载任务
                    task = new DownloadTask(DownloadService.this, fileInfo,DEFAULT_THREADS);
                    task.download();
                    mTasks.put(fileInfo.getId(), task);

                    break;
            }
        }
    };

    class InitThread extends Thread {
        private FileInfo fileInfo = null;

        InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int length = -1;
                if (conn.getResponseCode() == 200) {
                    //获得文件长度
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                //在本地创建一个文件长度
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    boolean flag = dir.mkdir();
                    Log.i("test", dir.toString() + " 不存在" + "mkdir " + flag);
                }
                File file = new File(dir, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //设置文件长度
                raf.setLength(length);
                fileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    if (raf != null) raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
