package com.lucasiturbide.lisectionadapter.adapter.widget;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

public abstract class SectionExpandableArrayAdapter<T> extends SectionArrayAdapter<T> implements ExpandableListAdapter{

	public SectionExpandableArrayAdapter(Context context, List<T> items, Sectionizer<T> sectionizer) {
		super(context, items, sectionizer);
	}

	public SectionExpandableArrayAdapter(Context context, int headerResource, int headerTextViewID, List<T> items, Sectionizer<T> sectionizer) {
		super(context, headerResource, headerTextViewID, items, sectionizer);
	}

	@Override
	public int getGroupCount() {
		return getSectionsCount();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return getSection((String)getGroup(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		int i = 0;
		String sectionInPosition = null;
		for (String section : getSections()) {
			if (groupPosition == i){
				sectionInPosition = section;
				break;
			}
			i++;
		}
		return sectionInPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getSection((String)getGroup(groupPosition)).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		int position = 0;
		for(int i = 0 ; i < groupPosition ; i++){
			int groupSize = getChildrenCount(i);
			position += groupSize;
			position++;
		}
		View v = getView(position, convertView, parent);
		setViewExpandedIcon(v, isExpanded);
		return v;
	}

	protected void setViewExpandedIcon(View v, boolean isExpanded){
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		int position = 1;
		for(int i = 0 ; i < groupPosition ; i++){
			position++;
			int groupSize = getChildrenCount(i);
			position += groupSize;
		}
		position += childPosition;
		View v = getView(position, convertView, parent);
		return v;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
        return 0x8000000000000000L | ((groupId & 0x7FFFFFFF) << 32) | (childId & 0xFFFFFFFF);
	}

	@Override
	public long getCombinedGroupId(long groupId) {
        return (groupId & 0x7FFFFFFF) << 32;
	}

}
