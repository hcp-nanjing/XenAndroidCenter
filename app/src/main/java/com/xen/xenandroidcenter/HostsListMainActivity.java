package com.xen.xenandroidcenter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class HostsListMainActivity extends ListActivity {
    private ListView hostsListView;
    private ArrayList<HostItem> listItems = new ArrayList<HostItem>();
    private ListViewAdapter listAdapter;
    private String sessionUUID;
    private List<HostItem> hostsList;


    protected XenAndroidApplication mContext;

    private ProgressDialog progressDialog;
    private void showProgressDialog(String title, String msg) {
        progressDialog = new ProgressDialog(HostsListMainActivity.this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideProgressBox() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    private void InfoMessageBox(String title, String msg) {
        AlertDialog notifyDialog = new AlertDialog.Builder(HostsListMainActivity.this)
                .setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        notifyDialog.show();
    }


    Handler msgHandler = new MHandler(this);
    static class MHandler extends Handler {
        WeakReference<HostsListMainActivity> mActivity;

        MHandler(HostsListMainActivity mAct) {
            mActivity = new WeakReference<HostsListMainActivity>(mAct);
        }

        @Override
        public void handleMessage(Message msg) {
            HostsListMainActivity theActivity = mActivity.get();
            theActivity.hideProgressBox();

            switch (msg.what) {
                case DATALOADEDMSG:
                    break;

                case DATALOADERRMSG:
                    theActivity.InfoMessageBox("Error", "Network Error Happened during loading.");
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //self define window title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_hosts_list_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_pool_list_main));

        hostsListView = getListView();
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        hostsListView.setEmptyView(emptyView);

        listAdapter = new ListViewAdapter(this, R.layout.pool_list_item_view, listItems);
        hostsListView.setAdapter(listAdapter);

        this.showProgressDialog("Notice", "Please wait, loading......");

        //read out the sessionID
        Bundle bundle = this.getIntent().getExtras();
        this.sessionUUID = bundle.getString(XenAndroidApplication.SESSIONID);

        LoadHostsAsyncTask loadTask = new LoadHostsAsyncTask(sessionUUID, hostsList);
        loadTask.execute((Void)null);

        hostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HostItem item = listItems.get(i);
                //
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
        HostItem item;
    };

    class ListViewAdapter extends ArrayAdapter<HostItem> {
        private Context mContext;
        int layoutResId;
        private List<HostItem> listItems;

        public ListViewAdapter(Context context, int resID, List<HostItem> listItems) {
            super(context, resID, listItems);
            this.layoutResId = resID;
            this.mContext = context;
            this.listItems = listItems;
        }

        @Override
        public HostItem getItem(int position) {
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
            HostItem item = getItem(position);
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
            HostItem HostItem = mContext.sessionDB.get(key);
            listItems.add(HostItem);
            listAdapter.notifyDataSetChanged();
        }
    }

    class LoadHostsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final String sessID;
        private final List<HostItem> hostsList;

        LoadHostsAsyncTask(String sessID, List<HostItem> hostsList) {
            this.sessID = sessID;
            this.hostsList = hostsList;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                this.poolItem = mContext.sessionDB.get(this.sessID);

            }
            catch (XenAndroidException e){
                e.printStackTrace();
                return false;
            }

            // Login successful
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

        @Override
        protected void onCancelled() {
        }
    }
}

