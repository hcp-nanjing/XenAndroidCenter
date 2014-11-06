package com.xen.xenandroidcenter;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PoolListMainActivity extends ListActivity {
    private ListView poolListView;
    private ArrayList<PoolItem> listItems = new ArrayList<PoolItem>();
    private PoolListViewAdapter listAdapter;

    protected XenAndroidApplication mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //self define window title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_pool_list_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_pool_list_main));

        poolListView = getListView();
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        poolListView.setEmptyView(emptyView);

        listAdapter = new PoolListViewAdapter(this, R.layout.pool_list_item_view, listItems);
        poolListView.setAdapter(listAdapter);

        ImageButton addPoolBtn = (ImageButton) findViewById(R.id.title_btn);
        addPoolBtn.setVisibility(View.VISIBLE);
        addPoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PoolListMainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        poolListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoolItem item = listItems.get(i);
                Intent intent = new Intent(PoolListMainActivity.this, PoolDetailsActivity.class);
                intent.putExtra(mContext.SESSIONID, item.getSessionUUID());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {

        Log.d("onResume", "onResume()");
        refleshAdaptorData();

        super.onResume();
    }

    static class PoolListViewHolder {
        View itemView;
        PoolItem item;
    };

    class PoolListViewAdapter extends ArrayAdapter<PoolItem> {
        private Context mContext;
        int layoutResId;
        private List<PoolItem> listItems;

        public PoolListViewAdapter(Context context, int resID, List<PoolItem> listItems) {
            super(context, resID, listItems);
            this.layoutResId = resID;
            this.mContext = context;
            this.listItems = listItems;
        }

        @Override
        public PoolItem getItem(int position) {
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
            PoolListViewHolder holder;
            PoolItem item = getItem(position);
            if(item == null) {
                Log.d("getView", "return null");
                return null;
            }

            if (convertView == null) {
                holder = new PoolListViewHolder();
                convertView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, parent, false);
                holder.itemView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (PoolListViewHolder) convertView.getTag();
            }

            Log.d("getView", item.getHostName());
            holder.item = item;
            TextView pool_name_view = (TextView) holder.itemView.findViewById(R.id.pool_name);
            pool_name_view.setText(item.getHostName());
            return convertView;
        }
    };

    public void refleshAdaptorData() {
        listItems.clear();
        for (String key : mContext.sessionDB.keySet()) {
            Log.d("refleshAdaptorData", key);
            PoolItem poolItem = mContext.sessionDB.get(key);
            listItems.add(poolItem);
            listAdapter.notifyDataSetChanged();
        }
    }
}
