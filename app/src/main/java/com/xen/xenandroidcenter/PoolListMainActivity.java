package com.xen.xenandroidcenter;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class PoolListMainActivity extends ListActivity {
    private ListView poolListView;
    private ArrayList<PoolItem> listItems = new ArrayList<PoolItem>();
    private ListViewAdapter listAdapter = new ListViewAdapter();

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

        poolListView = this.getListView();
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listItems);
        poolListView.setAdapter(listAdapter);

        ImageButton addPoolBtn = (ImageButton) findViewById(R.id.title_btn);
        addPoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItems.add("10.158.2.12");
                listAdapter.notifyDataSetChanged();
            }
        });



    }

}
