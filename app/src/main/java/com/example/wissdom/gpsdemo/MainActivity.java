package com.example.wissdom.gpsdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import didikee.com.permissionshelper.PermissionsHelper;
import didikee.com.permissionshelper.permission.DangerousPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GPS_LOCATION_NAME = android.location.LocationManager.GPS_PROVIDER;
    private static final int REQUEST_PRESSMION_CODE = 10000;
    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private LocationManager locationManager;
    private boolean isGpsEnabled;
    private String locateType;
    private TextView textLocationShow;
    private Button btnLocation;

    //权限检测类
    private PermissionsHelper permissionsHelper;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.web);
        //支持App内部javascript交互

        webview.getSettings().setJavaScriptEnabled(true);

//自适应屏幕

        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webview.getSettings().setLoadWithOverviewMode(true);

//设置可以支持缩放

        webview.getSettings().setSupportZoom(true);

//扩大比例的缩放

        webview.getSettings().setUseWideViewPort(true);

//设置是否出现缩放工具

        webview.getSettings().setBuiltInZoomControls(false);

        webview.loadUrl("file:///android_asset/index.html");
//        checkPermissions();
//        initViews();
//        initData();
    }

    /**
     * 方法描述：初始化定位相关数据
     */
    private void initData() {
        //获取定位服务
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启GPS定位功能
        isGpsEnabled = locationManager.isProviderEnabled(GPS_LOCATION_NAME);
        //定位类型：GPS
        locateType = LocationManager.GPS_PROVIDER;

    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper(MainActivity.this, MULTI_PERMISSIONS, true);
        if (permissionsHelper.checkAllPermissions(MULTI_PERMISSIONS)) {
            permissionsHelper.onDestroy();
            initData();
            initViews();
        } else {
            //申请权限
            permissionsHelper.startRequestNeedPermissions();
        }
        permissionsHelper.setonAllNeedPermissionsGrantedListener(new PermissionsHelper.onAllNeedPermissionsGrantedListener() {


            @Override
            public void onAllNeedPermissionsGranted() {
                initData();
                initViews();
                Log.d("test", "onAllNeedPermissionsGranted");
            }

            @Override
            public void onPermissionsDenied() {
                Log.d("test", "onPermissionsDenied");
            }

            @Override
            public void hasLockForever() {

            }

            @Override
            public void onBeforeRequestFinalPermissions(PermissionsHelper helper) {

            }
        });
    }

    /**
     * 方法描述：初始化View组件信息及相关点击事件
     */
    private void initViews() {
        textLocationShow = (TextView) findViewById(R.id.text_location_show);
        btnLocation = (Button) findViewById(R.id.btn_location);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        ((Button) findViewById(R.id.btn_skip)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionsHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void getLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
//                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        if (location != null) {
            updateUI(location);
        }
        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
        locationManager.requestLocationUpdates(locateType, 100, 0,
                locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MainActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
            updateUI(location);
            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Toast.makeText(MainActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
            getLocation();
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 方法描述：在View上更新位置信息的显示
     *
     * @param location
     */
    private void updateUI(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        textLocationShow.setText("当前经度：" + longitude + "\n当前纬度：" + latitude);
    }
}
