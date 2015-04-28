package com.samuelnoes.bdmaps.aty;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.samuelnotes.bdmaps.R;
import com.samuelnotes.bdmaps.app.AppSharedPreference;

public class UISettingDemo extends Activity implements OnClickListener {

	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private UiSettings mUiSettings;

	// 初始化经纬度地址转换搜索模块
	private GeoCoder mSearch;

	// / 根据是否App中获取到的经纬度来判断是否是首次定位

	private boolean isFirstLoc = true;

	private LocationClient mLocClient;

	public MyLocationListenner myListener = new MyLocationListenner();

	private double mLatitude = 0;
	private double mLongitude = 0;
	private float radius;
	private String city, address;;

	private LinearLayout mapView_layout;
	private PoiSearch mPoiSearch;

	private ImageView docenter;
	private float zoomLevel = 14f;// 当前地图缩放级别

	private TextView mylocation_radius, mylocation_address_tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uisetting);

		mapView_layout = (LinearLayout) findViewById(R.id.mapView_layout);
		findViewById(R.id.plus_layout).setOnClickListener(this);// / 放大
		findViewById(R.id.sub_layout).setOnClickListener(this); // / 缩小
		mylocation_radius = (TextView) findViewById(R.id.mylocation_radius);
		mylocation_address_tv = (TextView) findViewById(R.id.mylocation_address_tv);
		mLatitude = AppSharedPreference.getLastLocationLatitude(this);
		mLongitude  = AppSharedPreference.getLastLocationLongitude(this);
		// 设置默认位置为北京 缩放级别为13.5f
		MapStatus mapStatus = new MapStatus.Builder().target(new LatLng(mLatitude, mLongitude)).zoom(zoomLevel).build();
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		// 隐藏地图缩放控件
		mapOptions.zoomControlsEnabled(false).mapStatus(mapStatus);
		// 因需要设置mapOptions，所以无法在XML生成mMapView。
		mMapView = new MapView(this, mapOptions);
		mapView_layout.addView(mMapView);

		mBaiduMap = mMapView.getMap();
		mBaiduMap.isBuildingsEnabled();// 获取是否允许楼块效果

		mUiSettings = mBaiduMap.getUiSettings();
		mUiSettings.setZoomGesturesEnabled(true);
		mUiSettings.setScrollGesturesEnabled(true);
		mUiSettings.setRotateGesturesEnabled(true);
		mUiSettings.setOverlookingGesturesEnabled(false);
		mUiSettings.setCompassEnabled(true);

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(new GeoCodeResult());

		// 初始化Poi搜索模块，注册搜索事件监听
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(new GetPoiSearchResult());
		docenter = (ImageView) findViewById(R.id.docenter);
		docenter.setOnClickListener(this);
		/**
		 * 启动定位模块
		 */
		startLocation();
	}

	private void startLocation() {
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setScanSpan(15000);// 设置发起定位请求的间隔时间为10s(小于1秒则一次定位)
		mLocClient.setLocOption(option);
		mLocClient.start();
		if (mylocation_address_tv != null) {
			mylocation_address_tv.setText("正在获取地址信息， 请稍等..");
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.docenter:

			if (mLatitude != 0 && mLongitude != 0) {
				LatLng ll = new LatLng(mLatitude, mLongitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			break;
		case R.id.plus_layout:
			zoomLevel += 0.9f;
			// 设置地图的缩放比例
			MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(zoomLevel);
			mBaiduMap.setMapStatus(msu);
			break;

		case R.id.sub_layout:
			zoomLevel -= 0.9f;
			MapStatusUpdate msu1 = MapStatusUpdateFactory.zoomTo(zoomLevel);
			mBaiduMap.setMapStatus(msu1);
			// mBaiduMap.animateMapStatus(msu1);
			break;
		}
	}

	class GeoCodeResult implements OnGetGeoCoderResultListener {

		@Override
		public void onGetGeoCodeResult(
				com.baidu.mapapi.search.geocode.GeoCodeResult geoCodeResult) {
			// 地址-->经纬度
			mBaiduMap.clear();
			mBaiduMap.addOverlay(new MarkerOptions().position(
					geoCodeResult.getLocation()).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.icon_gcoding)));
			mBaiduMap.setMapStatus(MapStatusUpdateFactory
					.newLatLng(geoCodeResult.getLocation()));
			String strInfo = String.format("纬度：%f 经度：%f",
					geoCodeResult.getLocation().latitude,
					geoCodeResult.getLocation().longitude);
			Toast.makeText(UISettingDemo.this, strInfo, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onGetReverseGeoCodeResult(
				ReverseGeoCodeResult reverseGeoCodeResult) {

		}
	}

	class GetPoiSearchResult implements OnGetPoiSearchResultListener {

		@Override
		public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiResult(PoiResult poiResult) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			if(mLatitude>0&&mLongitude>0){
				AppSharedPreference.setLastLocationLatitude(UISettingDemo.this, mLatitude);
				AppSharedPreference.setLastLocationLongitude(UISettingDemo.this, mLongitude);
			}
			address = location.getAddrStr();
			radius = location.getRadius();
			if (radius >= 0) {
				mylocation_radius.setText("(精确到周围" + radius + "米)");
			}
			city = location.getCity();
			Log.d(address, mLatitude + "==" + mLongitude);
			if (TextUtils.isEmpty(address) || TextUtils.isEmpty(city)) {
				mylocation_address_tv.setText("暂未获取到地址信息");
			} else {
				mylocation_address_tv.setText("" + city + ":" + address + "附近");
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		mSearch.destroy();

	}

}
