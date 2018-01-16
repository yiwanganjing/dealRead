package net.bjcore.read.diaread.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public abstract class HolderAdapter<T,H> extends AbstractAdapter<T>{

	public HolderAdapter(Context context, List<T> listData) {
		super(context, listData);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		H holder = null;
		T item = listData.get(position);
		if(convertView==null){
			convertView = initConvertView(layoutInflater, parent);
			holder = initHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (H)convertView.getTag();
		}
		bindData(holder,item);
		return convertView;
	}

	/**
	 * initConvertView
	 * @param layoutInflater
	 * @return
	 */
	public abstract View initConvertView(LayoutInflater layoutInflater, ViewGroup parent);

	/**
	 * initHolder
	 * @param convertView
	 * @return
	 */
	public abstract H initHolder(View convertView);

	/**
	 * bindData
	 * @param holder
	 * @param item
	 */
	public abstract void bindData(H holder,T item);


}