package cbn.cn.download.db;

import java.util.List;

import cbn.cn.download.entities.ThreadInfo;

/**
 * 数据访问接口
 * Created by boning on 15/11/18.
 */
public interface ThreadDAO {
    void insertThread(ThreadInfo threadInfo);

    void deleteThread(String url, int thread_id);

    void deleteThread(String url);

    void updateThread(String url, int thread_id, int finished);

    List<ThreadInfo> getThreadsInfo(String url);

    boolean isExists(String url, int thread_id);


}
