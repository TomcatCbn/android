package cbn.cn.download.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cbn.cn.download.db.ThreadDAO;
import cbn.cn.download.db.ThreadDaoImpl;
import cbn.cn.download.entities.FileInfo;
import cbn.cn.download.entities.ThreadInfo;

/**
 * Created by boning on 15/11/18.
 */
public class DownloadTask {
    private Context context = null;
    private FileInfo fileInfo = null;
    private ThreadDAO mDao = null;
    public volatile boolean isPause = false;
    private static final String TAG = "test";

    private int mThreadCount = 1;//线程数量
    private List<DownloadThread> mThreadList = null;
    private List<ThreadInfo> threadInfos = null;

    private volatile int mFinished = 0;

    //线程池
    private static ExecutorService services = Executors.newFixedThreadPool(30);

    public DownloadTask(Context context, FileInfo fileInfo, int mThreadCount) {
        this.context = context;
        this.fileInfo = fileInfo;
        this.mThreadCount = mThreadCount;
        mDao = new ThreadDaoImpl(context);
    }

    public void download() {
        //读取数据库的线程信息
        threadInfos = mDao.getThreadsInfo(fileInfo.getUrl());
//        ThreadInfo threadInfo = null;
//        if (threadInfos.size() == 0) {
//            threadInfo = new ThreadInfo(0, fileInfo.getUrl(), 0, fileInfo.getLength(), 0);
//        } else {
//            threadInfo = threadInfos.get(0);
//        }
//        new DownloadThread(threadInfo).start();
        if (threadInfos.size() == 0) {
            //获得每个线程下载的数据段
            int length = fileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.getUrl(), length * i, (i + 1) * length - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(fileInfo.getLength());
                }
                threadInfos.add(threadInfo);
                //向数据库插入线程信息
                mDao.insertThread(threadInfo);

            }

        }
        mThreadList = new ArrayList<>();
        //启动多个线程下载
        for (ThreadInfo threadInfo : threadInfos) {
            DownloadThread thread = new DownloadThread(threadInfo);
            services.execute(thread);
            mThreadList.add(thread);
        }
        services.execute(new UpdateProgress());

    }

    private synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;
        for (DownloadThread thread : mThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            //发送广播通知完成下载任务
            //删除线程信息
            mDao.deleteThread(fileInfo.getUrl());
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileinfo", fileInfo);
            context.sendBroadcast(intent);
        }
    }

    class UpdateProgress implements Runnable {
        @Override
        public void run() {
            Log.i("test", "UI更新线程已经启动!!!");
            while (!isPause) {
                int finished = 0;
                for (ThreadInfo threadInfo : threadInfos) {
                    finished += threadInfo.getFinished();
                }
                fileInfo.setFinished(finished);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                intent.putExtra("fileinfo", fileInfo);
                Log.i("test", "更新的fileinfo为->" + fileInfo.toString());
                context.sendBroadcast(intent);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            Log.i("test", "UI更新线程已经停止!!!");
        }
    }

    /**
     * 下载线程
     */
    class DownloadThread implements Runnable {
        private ThreadInfo mThreadInfo = null;
        public boolean isFinished = false;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            Log.i("test", "开启第" + mThreadInfo.getId() + "个线程下载文件->" + fileInfo.getFileName()+" 已完成->"+mThreadInfo.getFinished());
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                //找到线程下载位置,从上次下载位置下载,设置下载位置
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                //找到文件的写入位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                Log.i(TAG, "文件信息->"+fileInfo.toString()+" 线程信息->" + mThreadInfo.getId() + " start-> " + start + " end-> " + mThreadInfo.getEnd() + " Finished->" + mThreadInfo.getFinished());
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());

                File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
//                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
//                mFinished += mThreadInfo.getFinished();
                raf.seek(start);
                //把下载进度发送广播给Activity
                if (conn.getResponseCode() == 206) {
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
//                        mFinished += len;
                        if (System.currentTimeMillis() - time > 5000) {
                            mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                            Log.i("test", "线程" + mThreadInfo.getId() + "已完成" + mThreadInfo.getFinished() * 100 / (mThreadInfo.getEnd() - mThreadInfo.getStart()) + "%");
                            time = System.currentTimeMillis();
//                            intent.putExtra("finished", mFinished*100 / fileInfo.getLength());
//                            intent.putExtra("id", fileInfo.getId());
//                            Log.i(TAG, "finished: " + mFinished * 100 / fileInfo.getLength());
//                            context.sendBroadcast(intent);
                        }
                        //在下载暂停,保存下载进度
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                    //表示线程执行完毕
                    isFinished = true;

                    //检查下载任务是否执行完毕
                    checkAllThreadsFinished();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //开始下载
        }
    }
}
