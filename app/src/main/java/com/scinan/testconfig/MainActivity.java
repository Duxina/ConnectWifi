package com.scinan.testconfig;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scinan.sdk_ext.smartlink.ConfigDeviceCallback;
import com.scinan.sdk_ext.smartlink.ScinanConfigExtra;
import com.scinan.sdk_ext.smartlink.ScinanConfigTask;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity implements ConfigDeviceCallback{

    EditText ssid, pwd;
    Button startOrStop;
    TextView result;

    ScinanConfigTask demoConfigTask;

    //配置的设置参数
    ScinanConfigExtra scinanConfigExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssid = (EditText) findViewById(R.id.ssid);
        pwd = (EditText) findViewById(R.id.pwd);
        startOrStop = (Button) findViewById(R.id.start);
        result = (TextView) findViewById(R.id.result);

        ssid.setText(getWifiName(this));

        //1055需要改成你的厂商ID
        scinanConfigExtra = new ScinanConfigExtra("10B5");
        //日志输出关闭
        scinanConfigExtra.setLoggable(true);
        //正式环境参数为false，测试环境是true
        scinanConfigExtra.setTestApi(false);

        //第一个参数是上下文，第二个参数是callback，第三个参数是否打印log
        demoConfigTask = new ScinanConfigTask(this, this, scinanConfigExtra);

        startOrStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(ssid.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, "ssid is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pwd.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, "pwd is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (demoConfigTask.getStatus()) {
                    case FINISHED:
                        result.setText("");
                        demoConfigTask = new ScinanConfigTask(MainActivity.this, MainActivity.this, scinanConfigExtra);
                    case PENDING:
                        demoConfigTask.execute(ssid.getText().toString().trim(), pwd.getText().toString().trim());
                        startOrStop.setText("结束");
                        break;
                    case RUNNING:
                        demoConfigTask.finish();
                        startOrStop.setText("开始");
                        break;
                }

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        demoConfigTask.finish();
    }

    @Override
    public void onConfigLog(String s) {
        Log.e("AAA", "---->" + s);
    }

    @Override
    public void onConfigFail() {
        result.setText("失败");
        startOrStop.setText("开始");
    }

    @Override
    public void onConfigSuccess(String s) {
        result.setText("成功，收到 " + s);
        startOrStop.setText("开始");
    }

    public static String getWifiName(Context context) {
        String ssid = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        if (wifiinfo == null) {
            return ssid;
        }
        String router = wifiinfo.getSSID();
        if (TextUtils.isEmpty(router))
            return ssid;
        if (!"<unknown ssid>".equals(router) && !"0X".equals(router) && !"0x".equals(router))
            ssid = router.replace("\"", "");
        return ssid;
    }
}
