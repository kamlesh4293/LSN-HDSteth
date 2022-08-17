package com.app.lsnhdsteth.hdsteth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import com.android.hdstethsdk.hdsteth.ConnectToHDSteth;
import com.android.hdstethsdk.hdsteth.HDSteth;
import com.app.lsnhdsteth.R;

import java.util.ArrayList;

public class HDStethCallBack implements HDSteth {

    Context context;
    ConnectToHDSteth connectToHDSteth;

    public HDStethCallBack(Context context) {
        this.context = context;
        connectToHDSteth = new ConnectToHDSteth();
    }

    @Override
    public void fnReceiveData(String sPointType, int iPoint) {
        if (sPointType.equalsIgnoreCase("ecg")) {
            Log.i("ECG","ECG : " + iPoint);
        } else if (sPointType.equalsIgnoreCase("mur")) {
            Log.i("MUR","MUR : " + iPoint);
        } else if (sPointType.equalsIgnoreCase("hs")) {
            Log.i("HS","HS : " + iPoint);
        }
    }

    @Override
    public void fnDetectDevice(ArrayList<BluetoothDevice> bluetoothDevices) {
        ArrayList<String> devicesNames = new ArrayList<String>();
        for (BluetoothDevice device : bluetoothDevices) {
            devicesNames.add(device.getName());
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context); builderSingle.setIcon(R.mipmap.app_logo); builderSingle.setTitle("Select One Device:-");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, devicesNames);
        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectToHDSteth.fnConnectDevice(context, bluetoothDevices.get(which));
                dialog.dismiss(); }
        });
        builderSingle.show();
    }

    @Override
    public void fnRecordData(String[] sPaths) {
//        sPaths[0] = ECG Path;
//        sPaths[1] = HS Path;
//        sPaths[2] = MUR Path;
//        sPaths[3] = Wavefile Path Path;
    }

    @Override
    public void fnDisconnected() {
//        runOnUiThread(new Runnable() { @Override
//        public void run() {
//            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
//        } });
    }

    @Override
    public void fnConnected(String sDeviceType) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(context, "Connected to " + sDeviceType, Toast.LENGTH_SHORT).show();
//            } });
    }
}
