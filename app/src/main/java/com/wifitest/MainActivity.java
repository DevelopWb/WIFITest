package com.wifitest;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.wifitest.Utils.GPRSUtil;
import com.wifitest.Utils.SPUtils;
import com.wifitest.Utils.WifiAPManager;
import com.wifitest.services.CheckApService;
import com.wifitest.wifi.WifiConnect.WifiCipherType;

import static com.wifitest.R.id.set_Mobile_Data;
import static com.wifitest.Utils.GPRSUtil.gprsIsOpenMethod;

public class MainActivity extends Activity implements OnClickListener {

    private String ssid;
    private WifiCipherType type;
    private String pswd;
    private EditText ssidEt;
    private EditText pswdEt;
    private ConnectivityManager mCM;
    private Button set_wifi_hot;
    private boolean wifiHotIsOpen = false;//wifi热点是否开启
    private WifiAPManager wifiAPManager;
    /**
     * 声音模式
     */
    private Button mSetSoundMode;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_setting);
        getActionBar().setTitle("设置默认连接热点信息");
        initView();
        wifiAPManager = WifiAPManager.getInstance(this);
        //注册handler
        wifiAPManager.regitsterHandler(mHandler);
        mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setSoundModeTextContent();

    }


    private void setSoundModeTextContent() {
        int mode = audioManager.getRingerMode();//静音值为0，振动值为1，响铃值为2
        if (mode == 0) {
// 声音模式
            mSetSoundMode.setText("静音模式");
        } else if (mode == 1) {
            //静音模式
            mSetSoundMode.setText("震动模式");
        } else {
            //震动模式
            mSetSoundMode.setText("响铃模式");
        }

    }

    private void initView() {
        ssidEt = (EditText) findViewById(R.id.ap_ssid);
        ssidEt.setText((String) SPUtils.get(this, "SPECIFY_SSID", "wanheng"));
        ((RadioGroup) findViewById(R.id.gp))
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rbnt1:
                                type = WifiCipherType.WIFICIPHER_WEP;
                                break;
                            case R.id.rbnt2:
                                type = WifiCipherType.WIFICIPHER_WPA;
                                break;
                            case R.id.rbnt3:
                                type = WifiCipherType.WIFICIPHER_NOPASS;
                                break;
                            case R.id.rbnt4:
                                type = WifiCipherType.WIFICIPHER_INVALID;
                                break;

                        }

                    }
                });

        pswdEt = (EditText) findViewById(R.id.ap_pswd);
        Button confire = (Button) findViewById(R.id.confire);
        confire.setOnClickListener(this);
        Button set_Mobile_Data = (Button) findViewById(R.id.set_Mobile_Data);
        set_Mobile_Data.setOnClickListener(this);
        set_wifi_hot = (Button) findViewById(R.id.set_Wifi_Hot);
        set_wifi_hot.setOnClickListener(this);

        mSetSoundMode = (Button) findViewById(R.id.set_sound_mode);
        mSetSoundMode.setOnClickListener(this);
    }


    private boolean checkInput() {
        ssid = ssidEt.getText().toString().trim();
        if (TextUtils.isEmpty(ssid)) {
            Toast.makeText(this, "ap名称未输入", Toast.LENGTH_SHORT).show();
            return false;
        }

        pswd = pswdEt.getText().toString().trim();
        if (TextUtils.isEmpty(pswd)) {
            Toast.makeText(this, "ap密码未输入", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (type == null) {
            Toast.makeText(this, "ap加密类型未选择", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //接收message，做处理
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiAPManager.MESSAGE_AP_STATE_ENABLED:
                    set_wifi_hot.setText("wifi热点关闭");
                    wifiHotIsOpen = true;
                    break;
                case WifiAPManager.MESSAGE_AP_STATE_FAILED:
                    set_wifi_hot.setText("wifi热点开启");
                    wifiHotIsOpen = false;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiAPManager.getInstance(this).unregitsterHandler();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.confire:
                if (checkInput()) {
                    SPUtils.put(MainActivity.this, "SPECIFY_SSID",
                            ssid);
                    SPUtils.put(MainActivity.this, "SPECIFY_TYPE",
                            type.toString());
                    SPUtils.put(MainActivity.this, "SPECIFY_PWD",
                            pswd);

                    CheckApService.SPECIFY_SSID = ssid;
                    CheckApService.SPECIFY_TYPE = type;
                    CheckApService.SPECIFY_PWD = pswd;

                    Toast.makeText(MainActivity.this, "设置成功",
                            Toast.LENGTH_SHORT).show();
                    CheckApService.isConnectting = false;
                    CheckApService.isStop = false;
                }
                break;
            case set_Mobile_Data:
                if (gprsIsOpenMethod(mCM)) {
                    GPRSUtil.setGprsOn_Off(false);
                } else {
                    GPRSUtil.setGprsOn_Off(true);
                }
                break;
            case R.id.set_Wifi_Hot:

                if (wifiHotIsOpen) {//关闭wifi
                    wifiAPManager.closeWifiAp();
                } else {//开启wifi热点
                    wifiAPManager.turnOnWifiAp("test", "88888888", WifiAPManager.WifiSecurityType.WIFICIPHER_WPA2);
                }


                break;
            default:
                break;
            case R.id.set_sound_mode:

//        //减少声音音量
//        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
////调大声音音量
//        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                int mode = audioManager.getRingerMode();//静音值为0，振动值为1，响铃值为2
                if (mode == 0) {
// 声音模式
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mSetSoundMode.setText("响铃模式");
                } else if (mode == 1) {
                    //静音模式
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    mSetSoundMode.setText("静音模式");
                } else {
                    //震动模式
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    mSetSoundMode.setText("震动模式");
                }
                break;
        }
    }

}
