package com.samuelnoes.bdmaps.aty;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.samuelnoes.bdmaps.adapter.ChoicePointAdapter;
import com.samuelnoes.bdmaps.model.NaviSDInfo;
import com.samuelnotes.bdmaps.R;

/**
 * 
 * 
 * @author samuelnotes
 *
 */
public class AtyNaviSDSelect extends BaseActivity implements
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

	private AutoCompleteTextView navi_road_point_input_actv;

	private ListView navi_address_pendingitem_lv;

	private PoiSearch mPoiSearch;

	private SuggestionSearch mSuggestionSearch;

	private ArrayAdapter<String> arrayAdapter;

	private LatLng currentLatLng;

	private LocationClient mLocClient;

	private BDLocationListener myListener = new MyLocationListenner();;

	private List<PoiInfo> pendingAddressItems = new ArrayList<PoiInfo>();

	private ChoicePointAdapter choicePointAdapter;

	public String address;

	public double mLatitude;

	public double mLongitude;

	public String city;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_road_sd_select);
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);

		navi_road_point_input_actv = (AutoCompleteTextView) this
				.findViewById(R.id.navi_road_point_input_actv);
		navi_address_pendingitem_lv = (ListView) this
				.findViewById(R.id.navi_address_pendingitem_lv);
		choicePointAdapter = new ChoicePointAdapter(AtyNaviSDSelect.this,
				pendingAddressItems, R.layout.navi_choice_addres_list_item);
		navi_address_pendingitem_lv.setAdapter(choicePointAdapter);
		navi_address_pendingitem_lv.setVisibility(View.GONE);
		navi_address_pendingitem_lv
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (pendingAddressItems != null
								&& pendingAddressItems.size() >= position) {
							PoiInfo poiInfo = pendingAddressItems.get(position);
							Log.d("PoiInfo : ", poiInfo.address + "LL: "+ poiInfo.location);
							Intent intent = new Intent();
							NaviSDInfo naviSDInfo = new NaviSDInfo(
									poiInfo.location.longitude,
									poiInfo.location.latitude, poiInfo.address);
							intent.putExtra(AtyNaviDestSelect.NaviPointObj,
									naviSDInfo);
							AtyNaviSDSelect.this.setResult(RESULT_OK, intent);
							AtyNaviSDSelect.this.finish();
						}

					}
				});
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		navi_road_point_input_actv.setAdapter(arrayAdapter);
		navi_road_point_input_actv.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (TextUtils.isEmpty(s)) {
					return;
				} else {
					/**
					 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
					 */
					mSuggestionSearch
							.requestSuggestion((new SuggestionSearchOption())
									.location(currentLatLng).city(city)
									.keyword(s.toString().trim()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		navi_road_point_input_actv
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							String kw = navi_road_point_input_actv.getText()
									.toString().trim();
							if (!TextUtils.isEmpty(kw)) {
								Toast.makeText(AtyNaviSDSelect.this,
										"请稍等，正在查询中", Toast.LENGTH_SHORT).show();
								mPoiSearch
										.searchNearby(new PoiNearbySearchOption()
												.location(currentLatLng)
												.keyword(kw));
							} else {
								Toast.makeText(AtyNaviSDSelect.this, "信息不能为空",
										Toast.LENGTH_LONG).show();
							}
						}
						return false;
					}
				});

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

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			currentLatLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			address = location.getAddrStr();
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			city = location.getCity();

		}

		public void onReceivePoi(BDLocation poiLocation) {

		}
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		arrayAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				arrayAdapter.add(info.key);
		}
		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(AtyNaviSDSelect.this, "未找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			List<PoiInfo> allPoi = result.getAllPoi();

			if (allPoi != null && allPoi.size() > 0) {
				pendingAddressItems = allPoi;
				choicePointAdapter.setData(allPoi);
				choicePointAdapter.notifyDataSetChanged();
				if (navi_address_pendingitem_lv != null) {
					navi_address_pendingitem_lv.setVisibility(View.VISIBLE);
				}
			}
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			// String strInfo = "在";
			// for (CityInfo cityInfo : result.getSuggestCityList()) {
			// strInfo += cityInfo.city;
			// strInfo += ",";
			// }
			// strInfo += "找到结果";
			// Toast.makeText(AtyNaviSDSelect.this, strInfo, Toast.LENGTH_LONG)
			// .show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
	}

}
