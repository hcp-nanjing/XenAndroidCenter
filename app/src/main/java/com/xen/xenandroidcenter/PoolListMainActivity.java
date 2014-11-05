package com.xen.xenandroidcenter;


import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
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
import java.util.ArrayList;


public class PoolListMainActivity extends ListActivity {
    private ListView poolListView;
    private ArrayList<PoolItem> listItems = new ArrayList<PoolItem>();
    private ListViewAdapter listAdapter = new ListViewAdapter();
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //self define window title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_pool_list_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_pool_list_main));

        mInflater = this.getLayoutInflater();
        poolListView = this.getListView();
        listAdapter = new ListViewAdapter();

        ImageButton addPoolBtn = (ImageButton) findViewById(R.id.title_btn);
        addPoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.notifyDataSetChanged();
            }
        });

        setListAdapter(listAdapter);

        poolListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoolItem item = listItems.get(i);

                //Intent intent = new Intent(PoolListMainActivity.this.getActivity(), PoolLoginActivity.class);

            }
        });

//        Connection connection;
//        try {
//           connection = new Connection(new URL("http://10.158.160.131"));
//        }
//        catch (MalformedURLException e) {
//            System.err.println("IndexOutOfBoundsException: " + e.getMessage());
//        }

    }

    private static class PoolListViewHolder {
        View itemView;
        PoolItem item;
    };

    private class ListViewAdapter extends BaseAdapter {
        public ListViewAdapter() {
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
            PoolListViewHolder holder;
            PoolItem item = getItem(position);
            if(item == null) return null;

            if (convertView == null) {
                holder = new PoolListViewHolder();

                convertView = mInflater.inflate(R.layout.pool_list_item_view, null);
                holder.itemView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (PoolListViewHolder) convertView.getTag();
            }

            holder.item = item;
            TextView pool_name = (TextView) holder.itemView.findViewById(R.id.pool_name);
            pool_name.setText("pool_name");
            return convertView;
        }
    };

}
