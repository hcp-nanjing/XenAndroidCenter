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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class VMDetailsActivity extends ListActivity {

    private String sessionUUID;
    private String vmUUID;
    private VmItem vm = null;
    private PopupMenu mOPMenu;

    private ListView vmAttributesListView;
    private List<ItemValue> listItems = new ArrayList<ItemValue>();
    private VMDetailItemsAdapter listAdapter;

    protected XenAndroidApplication mContext;

    private ProgressDialog progressDialog;
    private void showProgressDialog(String title, String msg) {
        progressDialog = new ProgressDialog(VMDetailsActivity.this);
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
        AlertDialog notifyDialog = new AlertDialog.Builder(VMDetailsActivity.this)
                .setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        notifyDialog.show();
    }

    private final static int OPCODE_STARTVM = 0x10000001;
    private final static int OPCODE_STOPVM = 0x10000002;
    private final static int OPCODE_SNAPSHOTVM = 0x10000003;

    private final static int OPSUCCESSMESSAGE = 0x00000001;
    private final static int OPSTARTMESSAGE = 0x00000002;
    private final static int OPENDMESSAGE = 0x00000003;
    private final static int OPFAILMESSAGE = 0x00000004;

    Handler msgHandler = new MHandler(this);
    static class MHandler extends Handler {
        WeakReference<VMDetailsActivity> mActivity;

        MHandler(VMDetailsActivity mAct) {
            mActivity = new WeakReference<VMDetailsActivity>(mAct);
        }

        @Override
        public void handleMessage(Message msg) {
            VMDetailsActivity theActivity = mActivity.get();

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
        setContentView(R.layout.activity_vm_details);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.cust_activity_title);

        mContext = (XenAndroidApplication)this.getApplication();

        //set the window title
        TextView titleTextView = (TextView) findViewById(R.id.title_text);
        titleTextView.setText(getResources().getString(R.string.title_activity_vm_details_activity));

        //read out the sessionID
        Bundle bundle = this.getIntent().getExtras();
        sessionUUID = bundle.getString(XenAndroidApplication.SESSIONID);
        vmUUID = bundle.getString(XenAndroidApplication.VMUUID);

        Log.d("SESSIONID", sessionUUID);
        Log.d("VMUUID", vmUUID);

        vmAttributesListView = getListView();

        populateDetailItems();
        listAdapter = new VMDetailItemsAdapter(this, R.layout.vm_details_item_view, listItems);
        vmAttributesListView.setAdapter(listAdapter);

        ImageButton opVMBtn = (ImageButton) findViewById(R.id.title_btn);
        opVMBtn.setVisibility(View.VISIBLE);
        opVMBtn.setImageResource(R.drawable.opbtn);
        opVMBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VMDetailsActivity.this.popUpMenu();
            }
        });

        mOPMenu = new PopupMenu(this, opVMBtn);
        mOPMenu.inflate(R.menu.vmdetails);
        mOPMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.start_vm:
                        ExecuteOPAsyncTask reboot_task = new ExecuteOPAsyncTask(OPCODE_STARTVM);
                        reboot_task.execute((Void)null);
                        break;
                    case R.id.stop_vm:
                        ExecuteOPAsyncTask shutdown_task = new ExecuteOPAsyncTask(OPCODE_STOPVM);
                        shutdown_task.execute((Void)null);
                        break;
                    case R.id.snapshot_vm:
                        ExecuteOPAsyncTask snapshot_task = new ExecuteOPAsyncTask(OPCODE_SNAPSHOTVM);
                        snapshot_task.execute((Void)null);
                        break;
                }
                return true;
            }
        });
    }

    private void populateDetailItems() {
        //Find out the VM in the VM list
        vm = mContext.sessionDB.get(sessionUUID).getVMs().get(this.vmUUID);

        if(vm != null) {
            {
                ItemValue itm = new ItemValue();
                itm.setItemTitle("VM Name: ");
                itm.setItemValue(vm.getName());
                listItems.add(itm);
            }

            {
                ItemValue itm = new ItemValue();
                itm.setItemTitle("VM Status: ");
                itm.setItemValue(vm.getPowerStatus());
                listItems.add(itm);
            }

            {
                ItemValue itm2 = new ItemValue();
                itm2.setItemTitle("IP Address: ");
                itm2.setItemValue(vm.getIpAddress());
                listItems.add(itm2);
            }

            {
                ItemValue itm = new ItemValue();
                itm.setItemTitle("UP Time: ");
                itm.setItemValue(vm.getUptime());
                listItems.add(itm);
            }
        }

    }

    private void refleshAdaptorData() {
        this.listAdapter.notifyDataSetChanged();
    }

    static class VMDetailViewHolder {
        View itemView;
        ItemValue item;
    };

    class VMDetailItemsAdapter extends ArrayAdapter<ItemValue> {
        private Context mContext;
        int layoutResId;

        public VMDetailItemsAdapter(Context context, int resID, List<ItemValue> listItems) {
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
            VMDetailViewHolder holder;
            ItemValue item = getItem(position);
            if(item == null) {
                Log.d("getView", "return null");
                return null;
            }

            if (convertView == null) {
                holder = new VMDetailViewHolder();
                convertView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, parent, false);
                holder.itemView = convertView;
                convertView.setTag(holder);
            } else {
                holder = (VMDetailViewHolder) convertView.getTag();
            }

            Log.d("getView ItemName:", item.getItemTitle());
            Log.d("getView ItemValue:", item.getItemValue());
            holder.item = item;
            TextView vm_detail_title_view = (TextView) holder.itemView.findViewById(R.id.vm_detail_title);
            TextView vm_detail_value_view = (TextView) holder.itemView.findViewById(R.id.vm_detail_value);

            vm_detail_title_view.setText(item.getItemTitle());
            vm_detail_value_view.setText(item.getItemValue());

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
            msg.what = VMDetailsActivity.OPSTARTMESSAGE;
            msgHandler.sendMessage(msg);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                switch ( this.opCode ) {
                    case OPCODE_STARTVM:
                        mContext.startVM(mContext.sessionDB.get(sessionUUID), vmUUID);
                        break;
                    case OPCODE_STOPVM:
                        mContext.stopVM(mContext.sessionDB.get(sessionUUID), vmUUID);
                        break;
                    case OPCODE_SNAPSHOTVM:
                        mContext.snapshotVM(mContext.sessionDB.get(sessionUUID), vmUUID, "new snapshot");
                        break;

                    default:
                        break;
                }
            } catch (XenAndroidException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = VMDetailsActivity.OPFAILMESSAGE;
                msgHandler.sendMessage(msg);
                return false;
            }

            Message msg = new Message();
            msg.what = VMDetailsActivity.OPSUCCESSMESSAGE;
            msgHandler.sendMessage(msg);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Message msg = new Message();
            msg.what = VMDetailsActivity.OPENDMESSAGE;
            msgHandler.sendMessage(msg);

            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
        }
    }
}
