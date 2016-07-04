package com.qian.zhu.adbwireless;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String STR_IP = "设备IP:";
    private static final String STR_STATE = "ADB无线调试状态:";
    private static final String STR_SWITCH_ON = "关闭ADB无线调试";
    private static final String STR_SWITCH_OFF = "打开ADB无线调试";
    private static final String STR_TIS = "请在PC的终端输入： \n adb connect ";

    private static final int ON = 0;
    private static final int OFF = 1;

    private String[] command_adb_start = new String[] {
            "setprop service.adb.tcp.port 5556", "stop adbd", "start adbd",
            "netstat" };
    private String command_adb_stop = "setprop service.adb.tcp.port -1";
    private String command_checkadb = "getprop service.adb.tcp.port";


    private TextView ip,adbstate,tis;



    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ON:
                    adbstate.setText(STR_STATE+"已开启");
                    Bundle data = msg.getData();
                    String result = (String) data.get("result");
                    String ip = getIP();
                    tis.setText(STR_TIS+ip+":"+result);
                    break;

                case OFF:
                    adbstate.setText(STR_STATE+"已关闭");
                    tis.setVisibility(View.GONE);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = (TextView) findViewById(R.id.ip);
        adbstate = (TextView) findViewById(R.id.adbstate);
        tis = (TextView) findViewById(R.id.tis);
        checkState();
        ip.setText(STR_IP+getIP());
    }

    private void checkState(){
        new Thread(){
            public void run() {
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command_checkadb, false);
                if(commandResult.result==0 && commandResult.successMsg!=null && !commandResult.successMsg.equals("")){
                    Message msg = new Message();
                    msg.what = ON;
                    Bundle data = new Bundle();
                    data.putString("result", commandResult.successMsg);
                    msg.setData(data);
                    handler.sendMessage(msg);
                }else{
                    handler.sendEmptyMessage(OFF);
                }
            };
        }.start();
    }

    public void open(View view){
        Toast.makeText(MainActivity.this, "open",Toast.LENGTH_LONG).show();
        new Thread(){
            public void run() {
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command_adb_start, true);
                Log.i("qenter", "commandResult.result:"+commandResult.result);
                if(commandResult.result==0 ){
                    handler.sendEmptyMessage(ON);
                }else{

                }
            };
        }.start();
    }

    public void close(View view){
        Toast.makeText(MainActivity.this, "close",Toast.LENGTH_LONG).show();
        new Thread(){
            public void run() {
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command_adb_stop, true);
                if(commandResult.result==0){
                    handler.sendEmptyMessage(OFF);
                }else{

                }
            };
        }.start();
    }

    public String getIP(){
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        Log.i("qenter", "ip:"+ip);
        return ip;
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
