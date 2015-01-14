package com.lucasiturbide.lisectionadapter.adapter.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public abstract class SectionArrayAdapter<T> extends BaseAdapter {

	public interface Sectionizer<T>{
		public String getSection(T item);
	}
	
	protected static int VIEW_TYPE_HEADER = -55;
	protected static int VIEW_TYPE_OTHER = -59;
	private static final String NULL_HEADER = "NULL_HEADER";
	private Context mContext;
	private int mHeaderResource;
	private int mHeaderTextViewID;
	private Map<String,ArrayList<T>> mItems;
	private Sectionizer<T> mSectionizer;
	private Object mLock = new Object();
	private boolean notifyOnDataChanged;
	private int headerMargin = 0;

	public SectionArrayAdapter(Context context, List<T> items, Sectionizer<T> sectionizer) {
		this(context,android.R.layout.simple_list_item_1, android.R.id.text1, items, sectionizer);
	}
	
	public SectionArrayAdapter(Context context, int headerResource, int headerTextViewID, List<T> items, Sectionizer<T> sectionizer) {
		super();
		mHeaderResource = headerResource;
		mHeaderTextViewID = headerTextViewID;
		mContext = context;
		mItems = new LinkedHashMap<String,ArrayList<T>>();
		mSectionizer = sectionizer;
		notifyOnDataChanged = true;
		Resources r = getContext().getResources();
		headerMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
		sectionizeAllItems(items);
	}

	public void addSections(String[] sections){
		for (String section : sections) {
			if (!mItems.containsKey(section)){
				ArrayList<T> sectionItems = new ArrayList<T>();
				mItems.put(section.toUpperCase(), sectionItems);
			}
		}
	}
	
	@Override
	public Object getItem(int position) {
		if (mItems.size() == 1 && mItems.containsKey(NULL_HEADER)){
			return mItems.get(NULL_HEADER).get(position);
		}else{
			int sectionNumber=0;
			int accumulatedItemSize = 0;
			for (String section : mItems.keySet()) {
				int headerPosition = sectionNumber + accumulatedItemSize;
				if (position == headerPosition){
					return section;
				}else{
					sectionNumber++;
					ArrayList<T> sectionItems = mItems.get(section);
					int itemIndex = position - (accumulatedItemSize + sectionNumber);
					if (sectionItems.size() < (itemIndex+1)){
						accumulatedItemSize += sectionItems.size();
					}else{
						return sectionItems.get(itemIndex);
					}
				}
			}
		}
		return null;
	}

	protected Set<String> getSections(){
		return mItems.keySet();
	}

	protected ArrayList<T> getSection(String sectionName){
		return mItems.get(sectionName);
	}

	protected int getSectionsCount(){
		return mItems.size();
	}

	@Override
	public int getCount() {
		if (mItems.isEmpty()){
			return 0;
		}else{
			if (mItems.size() == 1 && mItems.containsKey(NULL_HEADER)){
				return mItems.get(NULL_HEADER).size();
			}else{
				int itemCount = 0;
				for (String section : mItems.keySet()) {
					itemCount++;
					itemCount+=mItems.get(section).size();
				}
				return itemCount;
			}
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mItems.containsKey(getItem(position)) ? VIEW_TYPE_HEADER : VIEW_TYPE_OTHER;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (convertView == null) {
        	LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	if (viewType == VIEW_TYPE_HEADER){
        		convertView = inflater.inflate(getHeaderResource(), parent, false);
        		createSectionHeaderViewHolder(convertView);
        	}else{
        		convertView = inflateItemView(inflater, position, parent);
        	}
        }
        bindView(position, convertView, parent, viewType);
        return convertView;
	}

	@Override
	public boolean isEnabled(int position) {
		return VIEW_TYPE_HEADER != getItemViewType(position);
	}
	
	protected View inflateItemView(LayoutInflater inflater, int position, ViewGroup parent){
		View convertView = inflater.inflate(getItemResource(position), parent, false);
		createItemViewHolder(position, convertView);
		return convertView;
	}
	
	protected abstract int getItemResource(int position);
	protected abstract void createItemViewHolder(int position, View convertView);
	protected abstract void bindItemView(int position, View convertView, ViewGroup parent, int viewType);

	protected void bindView(int position, View convertView, ViewGroup parent, int viewType) {
    	if (viewType == VIEW_TYPE_HEADER){
            bindHeaderView(position, convertView, parent);
    	}else{
            bindItemView(position, convertView, parent, viewType);
    	}
	}

	protected void bindHeaderView(int position, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		final SectionHeaderViewHolder holder = (SectionHeaderViewHolder)convertView.getTag();
		String sectionName = (String)getItem(position);
		holder.sectionTitle.setText(sectionName);
    	convertView.setPadding(0, position == 0 ? 0 : headerMargin, 0, 0);
    	holder.sectionEmpty.setVisibility(mItems.get(sectionName).size()>0 || getEmptySectionText() == null? View.GONE:View.VISIBLE);
    	holder.sectionEmpty.setText(getEmptySectionText());
	}

	protected abstract CharSequence getEmptySectionText();

	protected void createSectionHeaderViewHolder(View convertView) {
		SectionHeaderViewHolder holder = new SectionHeaderViewHolder();
		holder.sectionTitle = (TextView) convertView.findViewById(android.R.id.text1);
		holder.sectionEmpty = (TextView) convertView.findViewById(android.R.id.text2);
		convertView.setTag(holder);
	}

	protected int getHeaderResource() {
		return mHeaderResource;
	}

	protected int getHeaderTextViewID() {
		return mHeaderTextViewID;
	}
	
	public Context getContext() {
		return mContext;
	}
	
    public void clear() {
        synchronized (mLock) {
        	mItems.clear();
        }
        if (notifyOnDataChanged){
        	notifyDataSetChanged();
        }
    }

    public boolean isNotifyDataChanged() {
		return notifyOnDataChanged;
	}

	public void setNotifyDataChanged(boolean notifyDataChanged) {
		this.notifyOnDataChanged = notifyDataChanged;
	}

	public void addAllItems(Collection<T> items){
    	sectionizeAllItems(items);
    }
    
    public void addItem(T item){
    	sectionizeItem(item);
    }
    
	private void sectionizeAllItems(Collection<T> items){
		if (items == null){
			return;
		}
		boolean oldNotifyDataChanged = notifyOnDataChanged;
		notifyOnDataChanged = false;
		for (T item : items) {
			sectionizeItem(item);
		}
		notifyOnDataChanged = oldNotifyDataChanged;
        if (notifyOnDataChanged){
        	notifyDataSetChanged();
        }
	}
	
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        notifyOnDataChanged = true;
    }

	private void sectionizeItem(T item){
        synchronized (mLock) {
			String section = NULL_HEADER;
			if (mSectionizer != null){
				section = mSectionizer.getSection(item);
				if (NULL_HEADER.equals(section)){
					throw new RuntimeException("Section name cannot be '" + NULL_HEADER + "'");
				}
			}
			ArrayList<T> sectionItems = mItems.get(section);
			if (sectionItems == null){
				sectionItems = new ArrayList<T>();
				mItems.put(section, sectionItems);
			}
			sectionItems.add(item);
        }
        if (notifyOnDataChanged){
        	notifyDataSetChanged();
        }
	}
	
	protected class SectionHeaderViewHolder{
		TextView sectionTitle;
		TextView sectionEmpty;
	}
	
}
