package com.samuelnoes.bdmaps.aty;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.samuelnotes.bdmaps.R;

public class AtyNaviDestSelect extends BaseActivity implements OnClickListener {

	private EditText navi_road_point_input;

	private Button navi_road_point_selet_btn;

	private GeoCoder mSearch;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_road_search);
		initView();
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(new GeoCodeResult());
	}

	private void initView() {
		navi_road_point_input = (EditText) findViewById(R.id.navi_road_point_input);
		navi_road_point_selet_btn = (Button) findViewById(R.id.navi_road_point_selet_btn);
		navi_road_point_selet_btn.setOnClickListener(this);
		navi_road_point_input
				.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							mSearch.geocode(new GeoCodeOption().city("郑州").address("河南省郑州市航空港区郑港四街沃金广场"));
						}
						return false;
					}
				});
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.navi_road_point_selet_btn:

			
			
			break;
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
//			// / 经纬度--> 地址
//			if (reverseGeoCodeResult == null
//					|| reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
//				Toast.makeText(AtyNaviDestPointSelect.this, "抱歉，未能找到结果",
//						Toast.LENGTH_LONG).show();
//			}
//
//			address = reverseGeoCodeResult.getAddress();
//			if (TextUtils.isEmpty(address)) {
//				mylocation_tv.setText("暂未获取到地址信息");
//			} else {
//				destAddress = address;
//				mylocation_tv.setText(address);
//			}
//			List<PoiInfo> poiList = reverseGeoCodeResult.getPoiList();
//			if (poiList != null && poiList.size() > 0) {
//				PoiInfo poiInfo = poiList.get(0);
//				Log.d("poiInfo: ", "address :" + poiInfo.address + "name: "
//						+ poiInfo.name);
//				if (!TextUtils.isEmpty(poiInfo.name)) {
//					mylocation_address_tv.setText(poiInfo.name + "附近");
//				} else {
//					mylocation_address_tv.setText("");
//				}
//				destLL = poiInfo.location;
//			} else {
//				mylocation_address_tv.setText("抱歉，未能找到相关参照物");
//			}

		}

	}
}
