package com.xen.xenandroidcenter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class VMsListMainActivity extends ListActivity {
    private ListView hostsListView;
    private List<VmItem> listItems = new ArrayList<VmItem>();
    private VMListViewAdapter listAdapter;

    protected XenAndroidApplication mContext;

    private ProgressDialog progressDialog;
    private void showProgressDialog(String title, String msg) {
        progressDialog = new ProgressDialog(VMsListMainActivity.this);
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
        AlertDialog notifyDialog = new AlertDialog.Builder(VMsListMainActivity.this)
                .setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        notifyDialog.show();
    }


    private final static int LOADSUCCESSMESSAGE = 0x00000001;
    Handler msgHandler = new MHandler(this);
    static class MHandler extends Handler {
        WeakReference<VMsListMainActivity> mActivity;

        MHandler(VMsListMainActivity mAct) {
            mActivity = new WeakReference<VMsListMainActivity>(mAct);
        }

        @Override
        public void handleMessage(Message msg) {
            VMsListMainActivity theActivity = mActivity.get();
            theActivity.hideProgressBox();

            switch (msg.what) {
                case LOADSUCCESSMESSAGE:
                    theActivity.refleshAdaptorData();
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
        setContentView(R.layout.activity_vm_list_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_vm_list_activity));

        //read out the sessionID
        Bundle bundle = this.getIntent().getExtras();
        String sessionUUID = bundle.getString(XenAndroidApplication.SESSIONID);
        Log.d("SESSION-UUID", sessionUUID);

        hostsListView = getListView();
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        hostsListView.setEmptyView(emptyView);

        listAdapter = new VMListViewAdapter(this, R.layout.host_list_item_view, listItems);
        hostsListView.setAdapter(listAdapter);

        this.showProgressDialog("Notice", "Please wait, loading......");

        LoadVMsAsyncTask loadTask = new LoadVMsAsyncTask(sessionUUID, listItems);
        loadTask.execute((Void) null);

        hostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                VmItem item = listItems.get(i);
                //
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d("onResume", "onResume()");
        super.onResume();
    }

    static class PoolListViewHolder {
        View itemView;
        VmItem item;
    };

    class VMListViewAdapter extends ArrayAdapter<VmItem> {
        private Context mContext;
        int layoutResId;

        public VMListViewAdapter(Context context, int resID, List<VmItem> listItems) {
            super(context, resID, listItems);
            this.layoutResId = resID;
            this.mContext = context;
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
            PoolListViewHolder holder;
            VmItem item = getItem(position);
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

            Log.d("getView", item.getName());
            holder.item = item;
            TextView vm_name_view = (TextView) holder.itemView.findViewById(R.id.vm_name);
            TextView vm_ip_view = (TextView) holder.itemView.findViewById(R.id.vm_ip);
            ImageView vm_status_image = (ImageView)holder.itemView.findViewById(R.id.vm_status);

            vm_name_view.setText(""+item.getName());
            vm_ip_view.setText(item.getIpAddress());
            String vmStatus = item.getPowerStatus();

            if(VmItem.VMSTATUS_STOP.equals(vmStatus)) {
                vm_status_image.setImageResource(R.drawable.vm);
            } else if(VmItem.VMSTATUS_PAUSED.equals(vmStatus)) {
                vm_status_image.setImageResource(R.drawable.vm);
            } else if(VmItem.VMSTATUS_RUNNING.equals(vmStatus)) {
                vm_status_image.setImageResource(R.drawable.vm);
            } else if(VmItem.VMSTATUS_SUSPENDED.equals(vmStatus)) {
                vm_status_image.setImageResource(R.drawable.vm);
            }

            return convertView;
        }
    };

    public void refleshAdaptorData() {
        Log.d("DISPLAY", listItems.size() + "");
        listAdapter.notifyDataSetChanged();
    }

    class LoadVMsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String sessID;

        LoadVMsAsyncTask(String sessID, List<VmItem> hostsList) {
            this.sessID = sessID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            listItems = mContext.sessionDB.get(this.sessID).getVMs();

            Log.d("VMSLIST", listItems.size() + "");

            Message msg = new Message();
            msg.what = VMsListMainActivity.LOADSUCCESSMESSAGE;
            msgHandler.sendMessage(msg);

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

