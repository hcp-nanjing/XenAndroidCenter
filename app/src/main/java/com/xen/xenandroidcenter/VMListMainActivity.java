package com.xen.xenandroidcenter;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Session;

import java.util.ArrayList;
import java.util.List;


public class VMListMainActivity extends ListActivity {
    private ListView vmListView;
    private ArrayList<VmItem> listItems = new ArrayList<VmItem>();
    private ListViewAdapter listAdapter;

    protected XenAndroidApplication mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //self define window title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_vm_list_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_vm_list_activity));

        vmListView = getListView();
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        vmListView.setEmptyView(emptyView);

        listAdapter = new ListViewAdapter(this, R.layout.vm_list_item_view, listItems);
        vmListView.setAdapter(listAdapter);

    }

    @Override
    protected void onResume() {

        Log.d("onResume", "onResume()");
        refleshAdaptorData();

        super.onResume();
    }

    static class VmListViewHolder {
        View itemView;
        VmItem item;
    };

    class ListViewAdapter extends ArrayAdapter<VmItem> {
        private Context mContext;
        int layoutResId;
        private List<VmItem> listItems;

        public ListViewAdapter(Context context, int resID, List<VmItem> listItems) {
            super(context, resID, listItems);
            this.layoutResId = resID;
            this.mContext = context;
            this.listItems = listItems;
        }

        @Override
        public VmItem getItem(int position) {
            if (!listItems.isEmpty()) {
                return listItems.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            if(listItems == null) return 0;
            return listItems.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d("getView", "getView()");
            VmListViewHolder holder;
            VmItem item = getItem(position);
            if(item == null) {
                Log.d("getView", "return null");
                return null;
            }

            if (convertView == null) {
                holder = new VmListViewHolder();
                convertView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, parent, false);
                holder.itemView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (VmListViewHolder) convertView.getTag();
            }

            Log.d("getView", item.getName());
            holder.item = item;
            TextView vm_name_view = (TextView) holder.itemView.findViewById(R.id.vm_name);
            vm_name_view.setText(item.getName());
            return convertView;
        }
    };

    public void refleshAdaptorData() {
        listItems.clear();
    }
}
