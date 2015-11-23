package cbn.cn.ndkexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import cbn.cn.download.entities.FileInfo;
import cbn.cn.download.service.DownloadService;
import cbn.cn.mutiItemDownload.ItemListView;

public class MainActivity extends AppCompatActivity {
    private TextView textView = null;
    private TextView mTvFileName = null;
    private ProgressBar mPbProgress = null;
    private Button btStop = null;
    private Button btStart = null;
    private Button btItemlist = null;
    private Button btTest = null;
    //    private static final String dlUrl = "http://dl.google.com/android/ndk/android-ndk-r10d-darwin-x86_64.bin";
    private static final String dlUrl = "http://dldir1.qq.com/qqfile/QQforMac/QQ_V4.0.6.dmg";
    //    private static final String fileName = "android-ndk-r10d-darwin-x86_64.bin";
    private static final String fileName = "QQ_V4.0.6.dmg";

    static {
        System.loadLibrary("hello-jni");
    }

    public static native String getStringFromNative();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvFileName = (TextView) findViewById(R.id.tvFileName);
        mPbProgress = (ProgressBar) findViewById(R.id.btProgress);
        mPbProgress.setMax(100);
        btStop = (Button) findViewById(R.id.btStop);
        btStart = (Button) findViewById(R.id.btStart);
        btItemlist = (Button) findViewById(R.id.btItemlist);
        btTest = (Button) findViewById(R.id.testbutton);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("test", "测试按钮");
                Intent intent = new Intent(MainActivity.this, ItemListView.class);
                startActivity(intent);
            }
        });
        //创建文件对象信息
        final FileInfo fileInfo = new FileInfo(0, dlUrl, fileName, 0, 0);

        btItemlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("11111");
                Log.i("test", "列表Activity按钮");
                Intent intent = new Intent(MainActivity.this, ItemListView.class);
                startActivity(intent);
            }
        });

        btItemlist = (Button) findViewById(R.id.btItemlist);
        btItemlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ItemListView.class);
            }
        });
        textView = (TextView) findViewById(R.id.textcode);
        textView.setText(getStringFromNative());



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
