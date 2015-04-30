package com.samuelnoes.bdmaps.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.samuelnotes.bdmaps.R;

public class ChoicePointAdapter extends BaseAdapter {
	private Context context;
	private List<PoiInfo> data;
	private int item;
	private LayoutInflater inflater;

	public ChoicePointAdapter(Context context, List<PoiInfo> list,int item) {
		this.context = context;
		this.data = list;
		this.item = item;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setData(List<PoiInfo> datas){
		this.data = datas;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv_title = null;
		if (convertView == null) {
			convertView = inflater.inflate(item, null);
			tv_title = (TextView) convertView.findViewById(R.id.navi_sd_isearch_tv);
			convertView.setTag(new DataWrapper(tv_title));
		} else {
			DataWrapper dataWrapper = (DataWrapper) convertView.getTag();
			tv_title = dataWrapper.tv_title;
		}
		PoiInfo  obj =  data.get(position);
		tv_title.setText(obj.address+"附近");
		return convertView;
	}

	private class DataWrapper {
		TextView tv_title = null;

		public DataWrapper(TextView tv_title) {
			this.tv_title = tv_title;
		}
	}

}
