package com.example.pickupapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Intent mdsStart;
    MachineDataService mdService;
    boolean mdsBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mdsStart = new Intent(this, MachineDataService.class);
        bindService(mdsStart,connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, MachineDataService.class));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView temp = findViewById(R.id.tv_temperature);
                        temp.setText(Double.toString(intent.getDoubleExtra(MachineDataService.TANK_TEMP,0.0)));
                    }
                }, new IntentFilter(MachineDataService.ACTION_MILK_TEMP_BROADCAST)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView vol = findViewById(R.id.tv_volume);
                        vol.setText(Double.toString(intent.getDoubleExtra(MachineDataService.TANK_VOLUME,0.0)));
                    }
                }, new IntentFilter(MachineDataService.ACTION_MILK_VOL_BROADCAST)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView temp = findViewById(R.id.tv_truck);
                        temp.setText(intent.getStringExtra(MachineDataService.TRUCK_ARRIVAL));
                    }
                }, new IntentFilter(MachineDataService.ACTION_TRUCK_ARRIVAL)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        TextView vol = findViewById(R.id.tv_pickup);
                        vol.setText(intent.getStringExtra(MachineDataService.TANK_PICKUP));
                    }
                }, new IntentFilter(MachineDataService.ACTION_PICKUP)
        );


        Button btn  = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = findViewById(R.id.tv_temperature);
                temp.setText(Double.toString(mdService.getTemperature()));
                TextView vol = findViewById(R.id.tv_volume);
                vol.setText(Double.toString(mdService.getVolume()));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mdsBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final ServiceConnection connection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MachineDataService.MDSBinder binder = (MachineDataService.MDSBinder) service;
            mdService = binder.getService();
            mdsBound = true;
            System.out.println("Service connection true");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mdsBound = false;
            System.out.println("Service connection false");
        }
    };
}