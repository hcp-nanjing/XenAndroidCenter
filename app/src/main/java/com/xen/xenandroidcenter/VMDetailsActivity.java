package com.xen.xenandroidcenter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class VMDetailsActivity extends ListActivity {

    private String sessionUUID;
    private ListView vmDetailsListView;
    private CustomAdapter detailsAdapter;
    private ArrayList<String> vmDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vm_details);

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_vm_details_activity));

        Bundle bundle = this.getIntent().getExtras();
        sessionUUID = bundle.getString(XenAndroidApplication.SESSIONID);
        Log.d("SESSIONID", sessionUUID);

        populateDetailsList();
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vmdetails, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateDetailsList() {
        vmDetailsListView = getListView();

        String[] poolDetails = new String[] { "Hosts", "VMs", "Templates", "Storage", "Network" };
        vmDetailsList = new ArrayList<String>();
        vmDetailsList.addAll( Arrays.asList(poolDetails) );

        // Create ArrayAdapter using the planet list.
        //listAdapter = new ArrayAdapter<String>(this, R.layout.pool_details_item_view, poolDetailsList);
        detailsAdapter = new CustomAdapter(this, vmDetailsList);
        vmDetailsListView.setAdapter(detailsAdapter);
    }


    public class VmDetails{
        private String title;
        private String content;
        
    }

    public class CustomAdapter extends ArrayAdapter<String> {
        public CustomAdapter(Context context, ArrayList<String> details) {
            super(context, 0, details);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String item = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pool_details_item_view, parent, false);
            }
            // Lookup view for data population
            TextView itemName = (TextView) convertView.findViewById(R.id.pool_details_item_text);
            // Populate the data into the template view using the data object
            itemName.setText(item);
            ImageView image = (ImageView) convertView.findViewById(R.id.pool_details_item_image);
            if (item == "Hosts") {
                image.setImageResource(R.drawable.host);
            }
            else if (item == "VMs") {
                image.setImageResource(R.drawable.vm);
            }
            else if (item == "Templates") {
                image.setImageResource(R.drawable.template);
            }
            else if (item == "Storage") {
                image.setImageResource(R.drawable.storage);
            }
            else if (item == "Network") {
                image.setImageResource(R.drawable.network);
            }
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
