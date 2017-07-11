package com.sixsixsix.yiyang.openwifi;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;


public class SettingsActivity extends Activity implements OnClickListener, OnLongClickListener {
    private final String NOTIFICATION_LIGHT_STATES = "notification_follow_up_status";
    private final String TORCH_LIGHT_STATES = "torch_widget_view_status";
    private final String SETTINGS_LIGHT_STATES = "settings_follow_up_status";
    private final String fileName = "torchfile";
    private ImageView first_one_image_view;
    private ImageView second_one_image_view;
    private ImageView third_one_image_view;
    private TextView  third_one_text_view;

    private ImageView first_two_image_view;
    private ImageView second_two_image_view;
    private ImageView third_two_image_view;

    private ImageView first_three_image_view;
    private ImageView second_three_image_view;
    private ImageView third_three_image_view;

    private ImageView first_four_image_view;
    private TextView  first_four_text_view;
    private ImageView second_four_image_view;
    private ImageView third_four_image_view;

    private ImageView first_five_image_view;
    private ImageView second_five_image_view;
    private ImageView third_five_image_view;

    private FrameLayout mFlBackground;
    private FrameLayout mFlBackground2;

    private WifiManager wifiManager;
    private AudioManager audioManager;
    private final String NOTIFICATION_CLOSE_CAMERA = "close_camera";
    private final String NOTIFICATION_DATA_REFRESH = "com.freeme.notification.data.refresh";
    private MyObserver mObserver;
    private LightnessObserver mLightnessObserver;
    private SettingsBrightnessDialog mBrightnessDialog;
    private Context mContext;
    private final String lightKey = "isLight";
    private FunctionUtils mFunctionUtils;
    private PhoneStateUtils mPhoneStateUtils;
    private int mMinBright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.settings_main);
        applyBlur();
        mContext = getApplicationContext();
        init();
        initLongClick();
        initShortClick();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction("android.intent.action.ANY_DATA_STATE");
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(audioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction(SETTINGS_LIGHT_STATES);
        registerReceiver(myReceiver, filter);
        mFunctionUtils = FunctionUtils.getInstance(mContext);
        mPhoneStateUtils = mFunctionUtils.getPhoneStateUtils();
        mObserver = new MyObserver(new Handler());
        mObserver.startObserver();
        mLightnessObserver = new LightnessObserver(new Handler());
        mLightnessObserver.startObserver();
        isLightOpen = Sputil.getTorchState(mContext, lightKey, fileName);
        mMinBright = mFunctionUtils.getMinBright();
    }


    private class LightnessObserver extends ContentObserver {
        ContentResolver mResolver;
        public LightnessObserver(Handler handler) {
            super(handler);
            mResolver = mContext.getContentResolver();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            setFirstFourText();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.System
                            .getUriFor(Settings.System.SCREEN_BRIGHTNESS), false,
                    this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }
    }

    private class MyObserver extends ContentObserver {
        ContentResolver mResolver;

        public MyObserver(Handler handler) {
            super(handler);
            mResolver = mContext.getContentResolver();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            setBrightIV();
            setRatationIV();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.System
                            .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), false,
                    this);
            mResolver.registerContentObserver(Settings.System
                            .getUriFor(Settings.System.ACCELEROMETER_ROTATION), false,
                    this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }
    }

    @Override
    protected void onResume() {
        mHandler.sendEmptyMessage(0x13);
        initImageView();
        super.onResume();
    }


    private void init() {
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mLightThread = new LightThread();

        LinearLayout mAriplaneLl = (LinearLayout) findViewById(R.id.settings_ariplane);
        LinearLayout mDataLl = (LinearLayout) findViewById(R.id.settings_data);
        LinearLayout mWifiLl = (LinearLayout) findViewById(R.id.settings_wifi);
        LinearLayout mRingLl = (LinearLayout) findViewById(R.id.settings_ring);
        LinearLayout mHotpotLl = (LinearLayout) findViewById(R.id.settings_hotpot);
        LinearLayout mLocationLl = (LinearLayout) findViewById(R.id.settings_location);
        LinearLayout mRotateLl = (LinearLayout) findViewById(R.id.settings_rotate);
        LinearLayout mBluetoothLl = (LinearLayout) findViewById(R.id.settings_bluetooth);
        LinearLayout mSavePowerLl = (LinearLayout) findViewById(R.id.settings_save_power);
        LinearLayout mScreenLightLl = (LinearLayout) findViewById(R.id.settings_screen_light);
        LinearLayout mAutoLightLl = (LinearLayout) findViewById(R.id.settings_auto_light);
        LinearLayout mTorchLl = (LinearLayout) findViewById(R.id.settings_torch);
        LinearLayout mApplicationLl = (LinearLayout) findViewById(R.id.settings_application);
        LinearLayout mSystemSettingLl = (LinearLayout) findViewById(R.id.settings_system_setting);
        LinearLayout mLauncherSettingLl = (LinearLayout) findViewById(R.id.settings_launcher_setting);


        first_one_image_view = (ImageView) mAriplaneLl.findViewById(R.id.setting_image_view);
        ((TextView) mAriplaneLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_airplane_mode_text);
        second_one_image_view = (ImageView) mDataLl.findViewById(R.id.setting_image_view);
        ((TextView) mDataLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_traffic_data_text);
        third_one_image_view = (ImageView) mWifiLl.findViewById(R.id.setting_image_view);
        third_one_text_view = (TextView) mWifiLl.findViewById(R.id.setting_text_view);
        third_one_text_view.setText(R.string.settings_wifi_text);

        first_two_image_view = (ImageView) mRingLl.findViewById(R.id.setting_image_view);
        ((TextView) mRingLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_ringtone_text);
        second_two_image_view = (ImageView) mHotpotLl.findViewById(R.id.setting_image_view);
        ((TextView) mHotpotLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_settings_hot_spot);
        third_two_image_view = (ImageView) mLocationLl.findViewById(R.id.setting_image_view);
        ((TextView) mLocationLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_location_mode_text);

        first_three_image_view = (ImageView) mRotateLl.findViewById(R.id.setting_image_view);
        ((TextView) mRotateLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_auto_rotate_text);
        second_three_image_view = (ImageView) mBluetoothLl.findViewById(R.id.setting_image_view);
        ((TextView) mBluetoothLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_blue_touch_text);
        third_three_image_view = (ImageView) mSavePowerLl.findViewById(R.id.setting_image_view);
        third_three_image_view.setImageResource(R.drawable.third_three_open);
        ((TextView) mSavePowerLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_power_saving_text);

        first_four_image_view = (ImageView) mScreenLightLl.findViewById(R.id.setting_image_view);
        first_four_text_view = (TextView) mScreenLightLl.findViewById(R.id.setting_text_view);
        first_four_text_view.setText(R.string.settings_brightness_text);
        second_four_image_view = (ImageView) mAutoLightLl.findViewById(R.id.setting_image_view);
        ((TextView) mAutoLightLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_auto_brightness_text);
        third_four_image_view = (ImageView) mTorchLl.findViewById(R.id.setting_image_view);
        ((TextView) mTorchLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_flash_light_text);

        first_five_image_view = (ImageView) mApplicationLl.findViewById(R.id.setting_image_view);
        first_five_image_view.setImageResource(R.drawable.settings_application);
        ((TextView) mApplicationLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_application_management_text);
        second_five_image_view = (ImageView) mSystemSettingLl.findViewById(R.id.setting_image_view);
        second_five_image_view.setImageResource(R.drawable.settings_settings);
        ((TextView) mSystemSettingLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_settings_text);
        third_five_image_view = (ImageView) mLauncherSettingLl.findViewById(R.id.setting_image_view);
        third_five_image_view.setImageResource(R.drawable.settings_traffic);
        ((TextView) mLauncherSettingLl.findViewById(R.id.setting_text_view)).setText(R.string.settings_desk);

        mFlBackground = (FrameLayout) findViewById(R.id.fl_background);
        mFlBackground2 = (FrameLayout) findViewById(R.id.fl_background2);
    }

    private void initShortClick() {
        first_one_image_view.setOnClickListener(this);
        second_one_image_view.setOnClickListener(this);
        third_one_image_view.setOnClickListener(this);

        first_two_image_view.setOnClickListener(this);
        second_two_image_view.setOnClickListener(this);
        third_two_image_view.setOnClickListener(this);

        first_three_image_view.setOnClickListener(this);
        second_three_image_view.setOnClickListener(this);
        third_three_image_view.setOnClickListener(this);

        first_four_image_view.setOnClickListener(this);
        second_four_image_view.setOnClickListener(this);
        third_four_image_view.setOnClickListener(this);

        first_five_image_view.setOnClickListener(this);
        second_five_image_view.setOnClickListener(this);
        third_five_image_view.setOnClickListener(this);
    }

    private void setFirstFourText() {
        int currentBrightness = 0;
        int lightnessPercentage = 0;
        currentBrightness = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, -1) - mMinBright;
        if (currentBrightness > -1) {
            lightnessPercentage = (int) Math.floor(currentBrightness / (float) (255 - mMinBright) * 100);
        } else {
            lightnessPercentage = 0;
        }
        first_four_text_view.setText(mContext.getString(R.string.settings_brightness_text) + " " + lightnessPercentage + "%");
    }

    private void setFirstOneIV() {

        if (!mPhoneStateUtils.isAlplaneMode()) {
            first_one_image_view.setImageResource(R.drawable.first_one_close);
        } else {
            first_one_image_view.setImageResource(R.drawable.first_one_open);
        }
    }

    private void setThirdOneIV() {
        if (mPhoneStateUtils.isWifiOpen()) {
            third_one_image_view.setImageResource(R.drawable.third_one_open);
            second_two_image_view.setImageResource(R.drawable.second_two_close);
        } else {
            third_one_image_view.setImageResource(R.drawable.third_one_close);
        }
    }

    private void setRingtonIV() {
        int mode = audioManager.getRingerMode();
        if (AudioManager.RINGER_MODE_NORMAL == mode) {
            first_two_image_view.setImageResource(R.drawable.first_two_open_ringtone);
        } else if (AudioManager.RINGER_MODE_VIBRATE == mode) {
            first_two_image_view.setImageResource(R.drawable.first_two_open_vibrate);
        } else {
            first_two_image_view.setImageResource(R.drawable.first_two_close);
        }
    }

    private void setBrightIV() {
        if (mPhoneStateUtils.isAutoBrightness()) {
            first_four_image_view.setImageResource(R.drawable.first_four_close);
            second_four_image_view.setImageResource(R.drawable.second_four_open);
        } else {
            first_four_image_view.setImageResource(R.drawable.first_four_open);
            second_four_image_view.setImageResource(R.drawable.second_four_close);
        }
    }

    private void setSecondThreeIV() {
        if (mPhoneStateUtils.isBlueToochOpen()) {
            second_three_image_view.setImageResource(R.drawable.second_three_open);
        } else {
            second_three_image_view.setImageResource(R.drawable.second_three_close);
        }
    }

    private void setThirdTwoIV() {
        PermissionUtils.checkSelfPermissions(SettingsActivity.this, 0, new PermissionUtils.PermissionsRequestCallBackAdapter() {
            @Override
            public void onPermissionAllowed() {
                if (mPhoneStateUtils.isLocationModeOpen()) {
                    third_two_image_view.setImageResource(R.drawable.third_two_open);
                } else {
                    third_two_image_view.setImageResource(R.drawable.third_two_close);
                }
            }

            @Override
            public String[] onGetPermissions() {
                return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            }
        }, 0, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void setSecondTwoIV() {
        if (mPhoneStateUtils.isWifiApOpen()) {
            second_two_image_view.setImageResource(R.drawable.second_two_open);
        } else {
            second_two_image_view.setImageResource(R.drawable.second_two_close);
        }
    }

    private void setSecondOneIV() {
        if (mPhoneStateUtils.checkPhoneNet() && !mPhoneStateUtils.isAlplaneMode()) {
            if (mPhoneStateUtils.isMobileDataOpen()) {
                second_one_image_view.setImageResource(R.drawable.second_one_open);
            } else {
                second_one_image_view.setImageResource(R.drawable.second_one_close);
            }
        } else {
            second_one_image_view.setImageResource(R.drawable.second_one_close);
        }

    }

    private void setRatationIV() {
        if (mPhoneStateUtils.isAutoRatation()) {
            first_three_image_view.setImageResource(R.drawable.first_three_open);
        } else {
            first_three_image_view.setImageResource(R.drawable.first_three_close);
        }
    }

    private void setThirdFourIV() {
        if (isLightOpen == true) {
            third_four_image_view.setImageResource(R.drawable.third_four_open);
        } else {
            third_four_image_view.setImageResource(R.drawable.third_four_close);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Sputil.saveTorchState(mContext, isLightOpen, lightKey, fileName);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        mObserver.stopObserver();
        mLightnessObserver.stopObserver();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void initLongClick() {
        first_one_image_view.setOnLongClickListener(this);
        second_one_image_view.setOnLongClickListener(this);
        third_one_image_view.setOnLongClickListener(this);

        first_two_image_view.setOnLongClickListener(this);
        second_two_image_view.setOnLongClickListener(this);
        third_two_image_view.setOnLongClickListener(this);

        first_three_image_view.setOnLongClickListener(this);
        second_three_image_view.setOnLongClickListener(this);
        third_three_image_view.setOnLongClickListener(this);

        first_four_image_view.setOnLongClickListener(this);
        second_four_image_view.setOnLongClickListener(this);
    }

    private void initImageView() {
        setFirstOneIV();
        setSecondOneIV();
        setThirdOneIV();
        setThirdOneTextViewText();

        setRingtonIV();
        setSecondTwoIV();
        setThirdTwoIV();

        setRatationIV();
        setFirstFourText();
        setSecondThreeIV();

        setBrightIV();
        setThirdFourIV();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                setSecondOneIV();
                setThirdOneTextViewText();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                setThirdOneIV();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                setSecondThreeIV();
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                setFirstOneIV();
                setSecondOneIV();
            } else if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                setSecondOneIV();
            } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                setRingtonIV();
            } else if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
                setThirdTwoIV();
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                setSecondTwoIV();
                setThirdOneIV();
                setThirdOneTextViewText();
            } else if (SETTINGS_LIGHT_STATES.equals(action)) {
                isLightOpen = intent.getBooleanExtra(lightKey, true);
                if (isLightOpen == true) {
                    third_four_image_view.setImageResource(R.drawable.third_four_open);
                } else {
                    third_four_image_view.setImageResource(R.drawable.third_four_close);
                }
            }
        }
    };

    private void setThirdOneTextViewText() {
        if (mPhoneStateUtils.isWifiOpen()) {
            ifWifiLinkOk();
        } else {
            third_one_text_view.setText(R.string.settings_wifi_text);
        }
    }

    private int mNetWorkID = -1;
    private WifiInfo wifiInfo;
    private String   wifiString;

    private void ifWifiLinkOk() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                wifiInfo = wifiManager.getConnectionInfo();
                mNetWorkID = wifiInfo.getNetworkId();
                if (wifiInfo.getNetworkId() != -1) {
                    wifiString = wifiInfo.getSSID();
                    mHandler.sendEmptyMessage(0x12);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {
        if (v == first_one_image_view) {
            mFunctionUtils.operateFlightMode();
        } else if (v == second_one_image_view) {
            mFunctionUtils.operateNetwork();
        } else if (v == third_one_image_view) {
            mFunctionUtils.operateWifi(this);
            //=============two==================
        } else if (v == first_two_image_view) {
            mFunctionUtils.setRingtonMode();
        } else if (v == second_two_image_view) {
            mFunctionUtils.operateWifiAp(this);
        } else if (v == third_two_image_view) {
            mFunctionUtils.operateGps();
            //=============three==================
        } else if (v == first_three_image_view) {
            mFunctionUtils.operateRotation();
        } else if (v == second_three_image_view) {
            mFunctionUtils.operateBluetooth(this);
        } else if (v == third_three_image_view) {
//            startActivity(new Intent(mContext, BS_MainActivity.class));
            //=============four==================
        } else if (v == first_four_image_view) {
            if (!mFunctionUtils.requestWriteSetting()) {
                return;
            }
            if (mPhoneStateUtils.isAutoBrightness()) {
                Settings.System.putInt(getContentResolver(),
                        SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
            try {
                if (mBrightnessDialog == null) {
                    mBrightnessDialog = new SettingsBrightnessDialog(SettingsActivity.this);
                }
                mBrightnessDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v == second_four_image_view) {
            mFunctionUtils.operateDisplay();
        } else if (v == third_four_image_view) {
            if (!mFunctionUtils.requestCamera(SettingsActivity.this)) {
                return;
            }
            if (checkCameraHardware(mContext)) {
                if (isLightOpen == false) {
                    third_four_image_view.setImageResource(R.drawable.third_four_open);
                } else {
                    third_four_image_view.setImageResource(R.drawable.third_four_close);
                }
                Intent intent = new Intent();
                intent.putExtra(lightKey, !isLightOpen);
                intent.setAction(NOTIFICATION_LIGHT_STATES);
                sendBroadcast(intent);
                intent.setAction(TORCH_LIGHT_STATES);
                sendBroadcast(intent);
                new Thread(mLightThread).start();
            } else {
                Toast.makeText(mContext, R.string.settings_light_error_text, Toast.LENGTH_SHORT).show();
                isLightOpen = false;
                setThirdFourIV();
            }
            //=============five==================
        } else if (v == first_five_image_view) {
            mFunctionUtils.openApplicationManager();
        } else if (v == second_five_image_view) {
            mFunctionUtils.openSetting();
        } else if (v == third_five_image_view) {
//            LauncherUtils.launchSettingActivity(SettingsActivity.this);
        } else {
        }
    }

    private LightThread mLightThread;

    private class LightThread implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                if (isLightOpen == false) {
                    mFunctionUtils.openTorch();
                    isLightOpen = true;
                } else {
                    mFunctionUtils.closeTorch();
                    isLightOpen = false;
                }
                mHandler.sendEmptyMessage(0x11);
            }
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x11:
                    setThirdFourIV();
                    break;
                case 0x12:
                    if (mPhoneStateUtils.isWifiOpen()) {
                        if (wifiString.length() > 1) {
                            wifiString = wifiString.substring(1, wifiString.length() - 1);
                        }
                        third_one_text_view.setText(wifiString);
                    }
                    break;
                case 0x13:
                    setSecondOneIV();
                    Intent intent = new Intent();
                    intent.setAction(NOTIFICATION_DATA_REFRESH);
                    sendBroadcast(intent);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private boolean isLightOpen;

    @Override
    public boolean onLongClick(View v) {
        if (v == first_one_image_view) {
            try {
                Intent airplaneIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                startActivity(airplaneIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }

        } else if (v == second_one_image_view) {
            try {
                Intent dataIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                dataIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(dataIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
        } else if (v == third_one_image_view) {
            try {
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(wifiIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
            //=============two==================
        } else if (v == first_two_image_view) {
            try {
                Intent soundIntent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                startActivity(soundIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }


        } else if (v == second_two_image_view) {//							Intent sound1Intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            try {
                Intent packageIntent = new Intent();
                ComponentName componetName = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                packageIntent.setComponent(componetName);
                packageIntent.setAction("android.intent.action.VIEW");
                startActivity(packageIntent);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Intent sound1Intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    startActivity(sound1Intent);
                } catch (Exception e1) {
                    e.printStackTrace();
                    Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
                }
            }

        } else if (v == third_two_image_view) {
            try {
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locationIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
            //=============three==================
        } else if (v == first_three_image_view) {
            try {
                Intent displayIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(displayIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
        } else if (v == second_three_image_view) {
            try {
                Intent blueToothIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(blueToothIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }

        } else if (v == third_three_image_view) {
            //=============four==================
        } else if (v == first_four_image_view) {
            try {
                Intent brightIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(brightIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
        } else if (v == second_four_image_view) {
            try {
                Intent autoBrightIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(autoBrightIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.settings_error_text, Toast.LENGTH_SHORT).show();
            }
        } else if (v == third_four_image_view) {
            //=============five==================
        } else if (v == first_five_image_view) {
        } else if (v == second_five_image_view) {
        } else if (v == third_five_image_view) {
        } else {
        }
        return true;
    }

    /**
     * 设置模糊背景
     */
    private void applyBlur() {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = null;
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                            Bitmap bkg = ((BitmapDrawable) wallpaperDrawable).getBitmap();
                            float radius = 25;
                            if (bkg == null || bkg.isRecycled()) {
                                return bitmap;
                            }
                            bitmap = bkg.copy(bkg.getConfig(), true);
                            final RenderScript rs = RenderScript.create(mContext);
                            final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                                    Allocation.USAGE_SCRIPT);
                            final Allocation output = Allocation.createTyped(rs, input.getType());
                            final ScriptIntrinsicBlur script;
                            script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                            script.setRadius(radius);
                            script.setInput(input);
                            script.forEach(output);
                            output.copyTo(bitmap);
                            rs.destroy();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (mFlBackground == null || mFlBackground2 == null) {
                        return;
                    }
                    if (bitmap != null) {
                        mFlBackground.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                    }
                    mFlBackground2.setBackgroundResource(R.color.background_black_color);
                    ObjectAnimator animator1 = ObjectAnimator.ofFloat(mFlBackground, "alpha", 0f, 1f);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(mFlBackground2, "alpha", 0f, 1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(animator1, animator2);
                    animatorSet.setDuration(2000);
                    animatorSet.start();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}