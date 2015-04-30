package com.samuelnoes.bdmaps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
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
	private Button navi_road_point_input , navi_road_dest_input;
	
	
	private Button navi_road_point_exchange_btn; 
	
	
	private NaviSDInfo startNaviInfo = new NaviSDInfo(); 
	
	
	private NaviSDInfo destNaviInfo  = new NaviSDInfo() ; 
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_road_search);
		initView();
		startLocationClient();
	}

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
		
		startNaviInfo.setSdLatitude(currentLL.latitude);
		startNaviInfo.setSdLongitude(currentLL.longitude);
		startNaviInfo.setSdAddress(address);
	}

	private void initView() {
		navi_lanucher_btn = (Button) this.findViewById(R.id.navi_lanucher_btn);
		navi_lanucher_btn.setOnClickListener(this);
		navi_road_point_input_btn = (Button) this.findViewById(R.id.navi_road_point_input_btn);
		navi_road_point_input_btn.setOnClickListener(this);
		
		navi_road_dest_input_btn = (Button) this.findViewById(R.id.navi_road_point_dest_input_btn);
		navi_road_dest_input_btn.setOnClickListener(this);
		
		navi_road_point_input = (Button) this.findViewById(R.id.navi_road_point_input_addrss);
		navi_road_point_input.setOnClickListener(this);
		
		navi_road_dest_input = (Button) this.findViewById(R.id.navi_road_dest_input_addrss);
		navi_road_dest_input.setOnClickListener(this);
		
		navi_road_point_exchange_btn =(Button) this.findViewById(R.id.navi_road_point_exchange_btn);
		navi_road_point_exchange_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.navi_lanucher_btn:

			
			
			
			
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

			///起始地点交换
			break;
		}
	}

	
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			switch (requestCode) {
			case navi_flag_startAddrss:
				NaviSDInfo naviSdInfo = 	(NaviSDInfo) data.getSerializableExtra(NaviPointObj);
				Log.d(TAG, "navisdinfo: "+ naviSdInfo);
				startNaviInfo = naviSdInfo;
				
				break;
			case navi_flag_destAddrss:
				
				NaviSDInfo navidestInfo = 	(NaviSDInfo) data.getSerializableExtra(NaviPointObj);
				Log.d(TAG, "navidestInfo: "+ navidestInfo);
				destNaviInfo = navidestInfo;
				
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

		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}
	
}
