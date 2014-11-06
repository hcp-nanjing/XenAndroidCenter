package com.xen.xenandroidcenter;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HostDetailsActivity extends ListActivity {

    private String sessionUUID;
    private String hostUUID;
    private HostItem host = null;
    private PopupMenu mOPMenu;

    private ListView hostsListView;
    private List<ItemValue> listItems = new ArrayList<ItemValue>();
    private HostDetailItemsAdapter listAdapter;

    protected XenAndroidApplication mContext;

    private ProgressDialog progressDialog;
    private void showProgressDialog(String title, String msg) {
        progressDialog = new ProgressDialog(HostDetailsActivity.this);
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
        AlertDialog notifyDialog = new AlertDialog.Builder(HostDetailsActivity.this)
                .setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        notifyDialog.show();
    }

    private final static int OPCODE_REBOOT = 0x10000001;
    private final static int OPCODE_SHUTDOWN = 0x10000002;

    private final static int OPSUCCESSMESSAGE = 0x00000001;
    private final static int OPSTARTMESSAGE = 0x00000002;
    private final static int OPENDMESSAGE = 0x00000003;
    private final static int OPFAILMESSAGE = 0x00000004;

    Handler msgHandler = new MHandler(this);
    static class MHandler extends Handler {
        WeakReference<HostDetailsActivity> mActivity;

        MHandler(HostDetailsActivity mAct) {
            mActivity = new WeakReference<HostDetailsActivity>(mAct);
        }

        @Override
        public void handleMessage(Message msg) {
            HostDetailsActivity theActivity = mActivity.get();


            switch (msg.what) {
                case OPSTARTMESSAGE:
                    theActivity.showProgressDialog("Info","Waiting please...");
                    break;
                case OPENDMESSAGE:
                    theActivity.hideProgressBox();
                    break;
                case OPSUCCESSMESSAGE:
                    theActivity.refleshAdaptorData();
                    break;
                case OPFAILMESSAGE:
                    theActivity.hideProgressBox();
                    theActivity.InfoMessageBox("Error", "Operation Failed");
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
        setContentView(R.layout.activity_host_details);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_host_details_item));

        //read out the sessionID
        Bundle bundle = this.getIntent().getExtras();
        sessionUUID = bundle.getString(XenAndroidApplication.SESSIONID);
        hostUUID = bundle.getString(XenAndroidApplication.HOSTUUID);

        Log.d("SESSIONID", sessionUUID);
        Log.d("hostUUID", hostUUID);

        hostsListView = getListView();
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        hostsListView.setEmptyView(emptyView);

        populateDetailItems();
        listAdapter = new HostDetailItemsAdapter(this, R.layout.host_details_item, listItems);
        hostsListView.setAdapter(listAdapter);

        ImageButton opHostBtn = (ImageButton) findViewById(R.id.title_btn);
        opHostBtn.setVisibility(View.VISIBLE);
        opHostBtn.setImageResource(R.drawable.opbtn);
        opHostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HostDetailsActivity.this.popUpMenu();
            }
        });

        mOPMenu = new PopupMenu(this, opHostBtn);
        mOPMenu.inflate(R.menu.host_details);
        mOPMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.reboot_host:
                        ExecuteOPAsyncTask reboot_task = new ExecuteOPAsyncTask(OPCODE_REBOOT);
                        reboot_task.execute((Void)null);
                        break;
                    case R.id.shutdown_host:
                        ExecuteOPAsyncTask shutdown_task = new ExecuteOPAsyncTask(OPCODE_SHUTDOWN);
                        shutdown_task.execute((Void)null);
                        break;
                }
                return true;
            }
        });
    }

    private void populateDetailItems() {

        List<HostItem> hosts = mContext.sessionDB.get(sessionUUID).getHosts();

        //Find out the host in the Host list
        for(HostItem itm : hosts) {
            if(itm.getUUID().equals(this.hostUUID)) {
                host = itm;
                break;
            }
        }


        if(host != null) {
            {
                ItemValue itm = new ItemValue();
                itm.setItemTitle("Host Name: ");
                itm.setItemValue(host.getName());
                listItems.add(itm);
            }

            {
                ItemValue itm2 = new ItemValue();
                itm2.setItemTitle("Role: ");
                itm2.setItemValue(host.getRole());
                listItems.add(itm2);
            }
        }

    }

    private void refleshAdaptorData() {
        this.listAdapter.notifyDataSetChanged();
    }

    static class HostDetailViewHolder {
        View itemView;
        ItemValue item;
    };

    class HostDetailItemsAdapter extends ArrayAdapter<ItemValue> {
        private Context mContext;
        int layoutResId;

        public HostDetailItemsAdapter(Context context, int resID, List<ItemValue> listItems) {
            super(context, resID, listItems);
            this.layoutResId = resID;
            this.mContext = context;
        }

        @Override
        public ItemValue getItem(int position) {
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
            HostDetailViewHolder holder;
            ItemValue item = getItem(position);
            if(item == null) {
                Log.d("getView", "return null");
                return null;
            }

            if (convertView == null) {
                holder = new HostDetailViewHolder();
                convertView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, parent, false);
                holder.itemView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (HostDetailViewHolder) convertView.getTag();
            }

            Log.d("getView ItemName:", item.getItemTitle());
            Log.d("getView ItemValue:", item.getItemValue());
            holder.item = item;
            TextView host_detail_title_view = (TextView) holder.itemView.findViewById(R.id.host_detail_title);
            TextView host_detail_value_view = (TextView) holder.itemView.findViewById(R.id.host_detail_value);

            host_detail_title_view.setText(item.getItemTitle());
            host_detail_value_view.setText(item.getItemValue());

            return convertView;
        }
    };

    /**
     * Popup menu
     */
    public void popUpMenu() {
        if(mOPMenu != null) {
            mOPMenu.show();
        }
    }


    class ExecuteOPAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private int opCode = -1;

        ExecuteOPAsyncTask(int opCode) {
            this.opCode = opCode;
        }

        @Override
        protected void onPreExecute() {
            Message msg = new Message();
            msg.what = HostDetailsActivity.OPSTARTMESSAGE;
            msgHandler.sendMessage(msg);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                switch ( this.opCode ) {
                   case OPCODE_REBOOT:
                        mContext.rebootHost(mContext.sessionDB.get(sessionUUID), hostUUID);
                        break;
                    case OPCODE_SHUTDOWN:
                        mContext.shutdownHost(mContext.sessionDB.get(sessionUUID), hostUUID);
                        break;
                default:
                    break;
                }
            } catch (XenAndroidException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = HostDetailsActivity.OPFAILMESSAGE;
                msgHandler.sendMessage(msg);
                return false;
            }

            Message msg = new Message();
            msg.what = HostDetailsActivity.OPSUCCESSMESSAGE;
            msgHandler.sendMessage(msg);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Message msg = new Message();
            msg.what = HostDetailsActivity.OPENDMESSAGE;
            msgHandler.sendMessage(msg);

            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
        }
    }
}
