package com.samuelnoes.bdmaps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.samuelnoes.bdmaps.model.NaviSDInfo;
import com.samuelnotes.bdmaps.R;

public class AtyNaviDestSelect extends BaseActivity implements OnClickListener {


	private LocationClient mLocClient;


	public static final String TAG=  "AtyNaviDestSelect";
	
	
	public String address ="我的位置";

	public double mLatitude;

	public double mLongitude;

	public String city;

	public static final int navi_flag_startAddrss = 1;
	public static final int navi_flag_destAddrss = 2;
	public static final int navi_flag_startPoint = 3;
	public static final int navi_flag_destPoint = 4;
	
	public static final String NaviPointObj  = "NaviPointObj";
	
	
	///// 如果用户直接选择我的位置 ，则需要使用当前位置作为导航的出发点
	public LatLng currentLL;
	private BDLocationListener myListener =new MyLocationListenner();

	
	
	private Button navi_lanucher_btn; 
	
	/**
	 * 选点
	 */
	private Button navi_road_point_input_btn ,navi_road_dest_input_btn ; 
	
	/**
	 * 选址
	 */
	private Button navi_road_point_input_addrss , navi_road_dest_input_addrss;
	
	
	private Button navi_road_point_exchange_btn; 
	
	
	private NaviSDInfo startNaviInfo = new NaviSDInfo(); 
	
	
	private NaviSDInfo destNaviInfo  = new NaviSDInfo() ;

	private NaviSDInfo tempNaviInfo = new NaviSDInfo();
	

	protected boolean mIsEngineInitSuccess; 
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_road_search);
		initView();
		startLocationClient();
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
						Toast.makeText(AtyNaviDestSelect.this, str,
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

	
	private void startLocationClient() {
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setScanSpan(15000);// 设置发起定位请求的间隔时间为10s(小于1秒则一次定位)
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	private void initView() {
		navi_lanucher_btn = (Button) this.findViewById(R.id.navi_lanucher_btn);
		navi_lanucher_btn.setOnClickListener(this);
		
		navi_road_point_input_btn = (Button) this.findViewById(R.id.navi_road_point_input_btn);
		navi_road_point_input_btn.setOnClickListener(this);
		
		navi_road_dest_input_btn = (Button) this.findViewById(R.id.navi_road_point_dest_input_btn);
		navi_road_dest_input_btn.setOnClickListener(this);
		
		navi_road_point_input_addrss = (Button) this.findViewById(R.id.navi_road_point_input_addrss);
		navi_road_point_input_addrss.setOnClickListener(this);
		
		navi_road_dest_input_addrss = (Button) this.findViewById(R.id.navi_road_dest_input_addrss);
		navi_road_dest_input_addrss.setOnClickListener(this);
		
		navi_road_point_exchange_btn =(Button) this.findViewById(R.id.navi_road_point_exchange_btn);
		navi_road_point_exchange_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.navi_lanucher_btn:
			if(startNaviInfo!=null&&destNaviInfo!=null){
				
				launchNavigator2(startNaviInfo,destNaviInfo);
			}else{
				Toast.makeText(AtyNaviDestSelect.this, "请选择需要导航的起始点",
						Toast.LENGTH_LONG).show();
			}
			
			/// 启动导航
			break;
		case R.id.navi_road_point_input_btn:
			startActivityForResult(new Intent(AtyNaviDestSelect.this,AtyNaviSDSelect.class), navi_flag_startPoint);
			
			/// 起点选点
			break;
		case R.id.navi_road_point_dest_input_btn:
			startActivityForResult(new Intent(AtyNaviDestSelect.this,AtyNaviSDSelect.class), navi_flag_destPoint);
			
			/// 结束选点
			break;
		case R.id.navi_road_point_input_addrss:
			
			startActivityForResult(new Intent(AtyNaviDestSelect.this,AtyNaviSDSelect.class), navi_flag_startAddrss);
			/// 开始选址
			break;
		case R.id.navi_road_dest_input_addrss:
			startActivityForResult(new Intent(AtyNaviDestSelect.this,AtyNaviSDSelect.class), navi_flag_destAddrss);
			/// 结束选址
			break;
			
		case R.id.navi_road_point_exchange_btn:

			exchangeStartDestLL();
			///起始地点交换
			break;
		}
	}

	private void exchangeStartDestLL() {
		tempNaviInfo = startNaviInfo;
		startNaviInfo = destNaviInfo;
		destNaviInfo = tempNaviInfo;
		address = navi_road_point_input_addrss.getText().toString().trim();
		navi_road_point_input_addrss.setText(navi_road_dest_input_addrss.getText().toString().trim());
		navi_road_dest_input_addrss.setText(address);
	}

	
	/**
	 * 指定导航起终点启动GPS导航.起终点可为多种类型坐标系的地理坐标。 前置条件：导航引擎初始化成功
	 */
	private void launchNavigator2(NaviSDInfo startNaviInfo, NaviSDInfo destNaviInfo) {
		// 这里给出一个起终点示例，实际应用中可以通过POI检索、外部POI来源等方式获取起终点坐标
		BNaviPoint startPoint = new BNaviPoint(startNaviInfo.getSdLongitude(),
				startNaviInfo.getSdLatitude(), startNaviInfo.getSdAddress(),
				BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(destNaviInfo.getSdLongitude(), destNaviInfo.getSdLatitude(),
				destNaviInfo.getSdAddress(), BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(this, startPoint, // 起点（可指定坐标系）
				endPoint, // 终点（可指定坐标系）
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				true, // 真实导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(AtyNaviDestSelect.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}

	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			switch (requestCode) {
			case navi_flag_startAddrss:
				startNaviInfo= 	(NaviSDInfo) data.getSerializableExtra(NaviPointObj);
				Log.d(TAG, "startNaviInfo: "+ startNaviInfo);
				if(navi_road_point_input_addrss!=null){
					navi_road_point_input_addrss.setText(startNaviInfo.getSdAddress());
				}
				break;
			case navi_flag_destAddrss:
				destNaviInfo = 	(NaviSDInfo) data.getSerializableExtra(NaviPointObj);
				Log.d(TAG, "navidestInfo: "+ destNaviInfo);
				if(navi_road_dest_input_addrss!=null){
					navi_road_dest_input_addrss.setText(destNaviInfo.getSdAddress());
				}
				break;
			case navi_flag_startPoint:
				
				break;
			case navi_flag_destPoint:
				
				break;
			}
		}
	}
	
	
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			currentLL = new LatLng(location.getLatitude(), location.getLongitude());
			address = location.getAddrStr();
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			city = location.getCity();
			tempNaviInfo.setSdLatitude(currentLL.latitude);
			tempNaviInfo.setSdLongitude(currentLL.longitude);
			tempNaviInfo.setSdAddress(address);
			

		}
		public void onReceivePoi(BDLocation poiLocation) {

			
			
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mLocClient!=null){
			mLocClient.stop();
		}
	}
	
	
}
