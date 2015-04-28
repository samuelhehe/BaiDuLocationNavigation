package com.samuelnoes.bdmaps.aty;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.samuelnoes.bdmaps.view.NaviPointView;
import com.samuelnotes.bdmaps.R;
import com.samuelnotes.bdmaps.app.AppSharedPreference;

/**
 * 百度地图选点
 * 
 * @author superuser
 *
 */
public class AtyNaviDestPointSelect extends Activity implements OnClickListener {

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
	private String address;;

	private LinearLayout mapView_layout;
	private PoiSearch mPoiSearch;

	private ImageView docenter;
	private float zoomLevel = 14f;// 当前地图缩放级别

	private TextView mylocation_tv, mylocation_address_tv;

	/**
	 * 目标坐标
	 */
	private LatLng destLL;
	/**
	 * 起始坐标
	 */
	private LatLng startLL;

	private String startAddress = "我的位置";

	private String destAddress;

	private NaviPointView navipointView;
	protected boolean mIsEngineInitSuccess;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.navi_point_select);
		navipointView = new NaviPointView(this);
		getWindow().addContentView(
				navipointView,
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));

		mapView_layout = (LinearLayout) findViewById(R.id.mapView_layout);

		findViewById(R.id.plus_layout).setOnClickListener(this);// / 放大
		findViewById(R.id.sub_layout).setOnClickListener(this); // / 缩小

		findViewById(R.id.mylocation_choice_btn).setOnClickListener(this);

		mylocation_tv = (TextView) findViewById(R.id.mylocation_tv);
		mylocation_address_tv = (TextView) findViewById(R.id.mylocation_address_tv);
		mLatitude = AppSharedPreference.getLastLocationLatitude(this);
		mLongitude = AppSharedPreference.getLastLocationLongitude(this);

		destLL = new LatLng(mLatitude, mLongitude);
		startLL = new LatLng(mLatitude, mLongitude);
		// 设置默认位置为北京 缩放级别为
		MapStatus mapStatus = new MapStatus.Builder().target(destLL)
				.zoom(14.5f).build();
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		// 隐藏地图缩放控件
		mapOptions.zoomControlsEnabled(false).mapStatus(mapStatus);
		// 因需要设置mapOptions，所以无法在XML生成mMapView。
		mMapView = new MapView(this, mapOptions);
		mapView_layout.addView(mMapView);

		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMapStatusChangeListener(new MapStatusChange());
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

		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, new LBSAuthManagerListener() {
					@Override
					public void onAuthResult(int status, String msg) {
						String str = null;
						if (0 == status) {
							str = "导航引擎启动成功";
						} else {
							str = "导航引擎启动失败" + msg;
						}
						Toast.makeText(AtyNaviDestPointSelect.this, str,
								Toast.LENGTH_LONG).show();
					}
				});
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private void startLocation() {
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		// option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setScanSpan(15000);// 设置发起定位请求的间隔时间为10s(小于1秒则一次定位)
		mLocClient.setLocOption(option);
		mLocClient.start();
		if (mylocation_tv != null) {
			mylocation_tv.setText("正在获取地址信息， 请稍等..");
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
				destLL = ll;
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

		case R.id.mylocation_choice_btn:
			launchNavigator2(startLL, destLL, startAddress, destAddress);
			break;

		}
	}

	/**
	 * 指定导航起终点启动GPS导航.起终点可为多种类型坐标系的地理坐标。 前置条件：导航引擎初始化成功
	 */
	private void launchNavigator2(LatLng startLL, LatLng destLL,
			String startAddress, String destAddress) {
		// 这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标
		BNaviPoint startPoint = new BNaviPoint(startLL.longitude,
				startLL.latitude, startAddress,
				BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(destLL.longitude, destLL.latitude,
				destAddress, BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(this, startPoint, // 起点（可指定坐标系）
				endPoint, // 终点（可指定坐标系）
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				true, // 真实导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(AtyNaviDestPointSelect.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	class MapStatusChange implements OnMapStatusChangeListener {

		@Override
		public void onMapStatusChange(MapStatus mapStatus) {

		}

		@Override
		public void onMapStatusChangeFinish(MapStatus mapStatus) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.mapView_layout);
			int x = layout.getWidth() / 2;
			int y = layout.getHeight() / 2 ;

			Point point = new Point(x, y);
			// 将布局的像素值转化为map 上的点 /// 获取屏幕中心的点对应的坐标值
			LatLng latLng = mBaiduMap.getProjection().fromScreenLocation(point);
			destLL = latLng;
			// Log.d("latitude: ", "" + latLng.latitude);
			// Log.d("longitude: ", "" + latLng.longitude);
			mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		}

		@Override
		public void onMapStatusChangeStart(MapStatus mapStatus) {
			if (mylocation_tv != null && mylocation_address_tv != null) {
				mylocation_tv.setTextSize(18.0f);
				mylocation_tv.setText("正在定位...");
			}
		}

	}

	class GeoCodeResult implements OnGetGeoCoderResultListener {

		// private int zIndex = 0x123;

		@Override
		public void onGetGeoCodeResult(
				com.baidu.mapapi.search.geocode.GeoCodeResult geoCodeResult) {
			// 地址-->经纬度
			// mBaiduMap.clear();
			// mBaiduMap.addOverlay(new MarkerOptions().position(
			// geoCodeResult.getLocation()).icon(
			// BitmapDescriptorFactory
			// .fromResource(R.drawable.icon_gcoding)));
			// mBaiduMap.setMapStatus(MapStatusUpdateFactory
			// .newLatLng(geoCodeResult.getLocation()));
			// String strInfo = String.format("纬度：%f 经度：%f",
			// geoCodeResult.getLocation().latitude,
			// geoCodeResult.getLocation().longitude);
			// Toast.makeText(AtyNaviDestPointSelect.this, strInfo,
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onGetReverseGeoCodeResult(
				ReverseGeoCodeResult reverseGeoCodeResult) {
			// / 经纬度--> 地址
			if (reverseGeoCodeResult == null
					|| reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(AtyNaviDestPointSelect.this, "抱歉，未能找到结果",
						Toast.LENGTH_LONG).show();
			}

			address = reverseGeoCodeResult.getAddress();
			if (TextUtils.isEmpty(address)) {
				mylocation_tv.setText("暂未获取到地址信息");
			} else {
				destAddress = address;
				mylocation_tv.setText(address);
			}
			List<PoiInfo> poiList = reverseGeoCodeResult.getPoiList();
			if (poiList != null && poiList.size() > 0) {
				PoiInfo poiInfo = poiList.get(0);
				Log.d("poiInfo: ", "address :" + poiInfo.address + "name: "
						+ poiInfo.name);
				if (!TextUtils.isEmpty(poiInfo.name)) {
					mylocation_address_tv.setText(poiInfo.name + "附近");
				} else {
					mylocation_address_tv.setText("");
				}
				destLL = poiInfo.location;
			} else {
				mylocation_address_tv.setText("抱歉，未能找到相关参照物");
			}
			// for (PoiInfo poiInfo : poiList) {
			// Log.d("poiInfo: ", "address :"+poiInfo.address +"name: "+
			// poiInfo.name);
			// }

			// mBaiduMap.clear();
			//
			// View view = getLayoutInflater().inflate(R.layout.location_view,
			// null);
			// TextView textView = (TextView)
			// view.findViewById(R.id.address_text);
			// textView.setText(reverseGeoCodeResult.getAddress());
			// // 将View转化成用于显示的bitmap
			// BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
			// OverlayOptions overlayOptions = new MarkerOptions()
			// .position(reverseGeoCodeResult.getLocation()).icon(bitmap)
			// .zIndex(zIndex);
			// mBaiduMap.addOverlay(overlayOptions);
			// mBaiduMap.setMapStatus(MapStatusUpdateFactory
			// .newLatLng(reverseGeoCodeResult.getLocation()));

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
				startLL = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(startLL);
				mBaiduMap.animateMapStatus(u);
			}

			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			if (mLatitude > 0 && mLongitude > 0) {
				AppSharedPreference.setLastLocationLatitude(
						AtyNaviDestPointSelect.this, mLatitude);
				AppSharedPreference.setLastLocationLongitude(
						AtyNaviDestPointSelect.this, mLongitude);
			}
			address = location.getAddrStr();
			if (TextUtils.isEmpty(address)) {
				mylocation_tv.setText("暂未获取到地址信息");
			} else {
				mylocation_tv.setText(address);
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
