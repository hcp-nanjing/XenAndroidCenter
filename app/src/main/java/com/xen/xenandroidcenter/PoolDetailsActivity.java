package com.xen.xenandroidcenter;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class PoolDetailsActivity extends ListActivity {

    private ListView poolDetailsListView;
    //private ArrayList<PoolDetailsItem> listItems = new ArrayList<PoolDetailsItem>();
    private ArrayAdapter<String> listAdapter ;  


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_details);

        poolDetailsListView = getListView();
        //poolDetailsListView = (ListView) findViewById( R.id.poolDetailsList );
  
        
        String[] poolDetails = new String[] { "Hosts", "VMs", "Templates", "Storage", "Network" };
        ArrayList<String> poolDetailsList = new ArrayList<String>();
        poolDetailsList.addAll( Arrays.asList(poolDetails) );
      
        // Create ArrayAdapter using the planet list.  
        listAdapter = new ArrayAdapter<String>(this, R.layout.pool_details_item_view, poolDetailsList);
        poolDetailsListView.setAdapter(listAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pool_details, menu);
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

}
