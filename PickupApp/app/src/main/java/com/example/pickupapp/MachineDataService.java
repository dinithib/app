package com.example.pickupapp;

import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

public class MachineDataService extends Service {

    public static final String ACTION_MILK_VOL_BROADCAST = "ACTION_MILK_VOL_BROADCAST";
    public static final String ACTION_MILK_TEMP_BROADCAST = "ACTION_MILK_TEMP_BROADCAST";
    public static final String ACTION_TRUCK_ARRIVAL = "ACTION_TRUCK_ARRIVAL";
    public static final String ACTION_PICKUP = "ACTION_PICKUP";

    public static final String TRUCK_CT_ID = "ct-1";
    public static final String TANK_CT_ID = "ct-3";

    public static final String EMU = "emu";

    public static final String TANK_TEMP = "ns:MilkTemperature";
    public static final String TANK_PICKUP = "ns:MilkPickupEvent";
    public static final String TANK_VOLUME = "ns:MilkVolume";
    public static final String TRUCK_ARRIVAL = "ns:TruckArrivalEvent";

    //public static final String TANK_TOPIC_VOLUME = TANK_CT_ID+"/"+TANK_VOLUME;
    public static final String TANK_TOPIC_EMU_VOLUME = TANK_CT_ID+"/"+EMU+"/"+TANK_VOLUME;

    //public static final String TANK_TOPIC_TEMP = TANK_CT_ID+"/"+TANK_TEMP;
    public static final String TANK_TOPIC_EMU_TEMP = TANK_CT_ID+"/"+EMU+"/"+TANK_TEMP;

    //public static final String TANK_TOPIC_PICKUP = TANK_CT_ID+"/"+TANK_PICKUP;
    public static final String TANK_TOPIC_EMU_PICKUP = TANK_CT_ID+"/"+EMU+"/"+TANK_PICKUP;

    //public static final String TRUCK_TOPIC_TRUCK_ARRIVAL = TANK_CT_ID+"/"+TRUCK_ARRIVAL;
    public static final String TRUCK_TOPIC_EMU_TRUCK_ARRIVAL = TANK_CT_ID+"/"+EMU+"/"+TRUCK_ARRIVAL;

   // public static final String TANK_TOPIC_TRUCK_ARRIVAL = TANK_CT_ID+"/"+TRUCK_ARRIVAL;
    public static final String TANK_TOPIC_EMU_TRUCK_ARRIVAL = TANK_CT_ID+"/"+EMU+"/"+TRUCK_ARRIVAL;

    final String tankCtUrl = "tcp://136.186.108.52:30167";
    final String tankCtUsername = TANK_CT_ID;
    final String tankCtPwd =  "AhHcGXgRuw"; //"oBNR3HzOkK" ;

    final String truckCtUrl = "tcp://136.186.108.52:30165";
    final String truckCtUsername = TRUCK_CT_ID;
    final String truckCtPwd = "4iLMDGbYZe";

    private final IBinder binder = new MDSBinder();
    private double temperature = 0.0;
    private double volume = 0.0;

    String clientIdtank = "andient4522";
    String clientIdtruck = "andient4521";
    String clientIdtankpub = "andient4520";

    private MqttAndroidClient tankCtClient;
    private MqttAndroidClient truckCTClient;

    private TankMqttCallback tankCtCallBack;
    private TruckMqttCallback truckCtCallback;

    //integration 24

    private final double FARM_LAT = 50.20;
    private final double FARM_LON = 41.20;
//use 29

    public MachineDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        truckCTClient = new MqttAndroidClient(getApplicationContext(),
                                        truckCtUrl, clientIdtruck);
        truckCtCallback = new TruckMqttCallback();
        truckCTClient.setCallback(truckCtCallback);
        Subscribe(truckCTClient, TRUCK_CT_ID, truckCtPwd, TRUCK_CT_ID+"/"+EMU+"/#");


        tankCtClient = new MqttAndroidClient(getApplicationContext(), tankCtUrl, clientIdtank);
        tankCtCallBack = new TankMqttCallback();
        tankCtClient.setCallback(tankCtCallBack);
        Subscribe(tankCtClient, tankCtUsername, tankCtPwd, TANK_CT_ID+"/"+EMU+"/#");

    }

    // integration in function 8
    //use 10

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public double getTemperature(){
        return temperature;
    }

    public double getVolume(){
        return volume;
    }
//use 10

    protected class TankMqttCallback implements MqttCallbackExtended{

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
        }
        @Override
        public void connectionLost(Throwable cause) {
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {


            if(topic.equalsIgnoreCase(TANK_TOPIC_EMU_TEMP)) {
                temperature = Double.parseDouble(message.toString());
                Intent intent = new Intent(ACTION_MILK_TEMP_BROADCAST);
                Bundle data =  new Bundle();
                data.putDouble(TANK_TEMP,temperature);
                intent.putExtras(data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            else if(topic.equalsIgnoreCase(TANK_TOPIC_EMU_VOLUME)) {
                Intent intent = new Intent(ACTION_MILK_VOL_BROADCAST);
                Bundle data =  new Bundle();
                volume = Double.parseDouble(message.toString());
                data.putDouble(TANK_VOLUME,volume);
                intent.putExtras(data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            else if(topic.equalsIgnoreCase(TANK_TOPIC_EMU_PICKUP)) {
                Intent intent = new Intent(ACTION_PICKUP);
                Bundle data =  new Bundle();
                Date pickup = new Date();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                data.putString(TANK_PICKUP,fmt.format(pickup));
                intent.putExtras(data);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
//use 25
    protected class TruckMqttCallback implements MqttCallbackExtended{
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
        }
        @Override
        public void connectionLost(Throwable cause) {
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            if(topic.equalsIgnoreCase(TRUCK_TOPIC_EMU_TRUCK_ARRIVAL)) {
                String[] splited = message.toString().split(" ");
                double lat = 0;
                double lon  = 0;
                if (splited.length == 2){
                    lat = Double.parseDouble(splited[0]);
                    lon = Double.parseDouble(splited[1]);
                    if(lat == FARM_LAT && lon == FARM_LON){
                        Intent intent = new Intent(ACTION_TRUCK_ARRIVAL);
                        Bundle data =  new Bundle();
                        Date arrival = new Date();
                        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                        data.putString(TRUCK_ARRIVAL,fmt.format(arrival));
                        intent.putExtras(data);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        Publish(tankCtClient, tankCtUsername, tankCtPwd,TANK_TOPIC_EMU_TRUCK_ARRIVAL,"1");

                    }
                }
            }
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
//--------------28 int only without function implementation code for dealing with the msg
//--- 16 use
    public class MDSBinder extends Binder {
        MachineDataService getService(){
            return MachineDataService.this;
        }
    }
    //use 5

    public void Subscribe(MqttAndroidClient mqttClient, String username, String password, String topic){

        MqttConnectOptions mqttconnoptions = new MqttConnectOptions();
        mqttconnoptions.setAutomaticReconnect(true);
        mqttconnoptions.setCleanSession(false);
        mqttconnoptions.setUserName(username);
        mqttconnoptions.setPassword(password.toCharArray());

        try {
            mqttClient.connect(mqttconnoptions, getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    try {
                        mqttClient.subscribe(topic, 0);
                    } catch (MqttException ex){
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println(exception.getCause().getMessage());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }
    //---25
    public void Publish(MqttAndroidClient mqttClient, String username, String password, String topic, String value){

        MqttConnectOptions mqttconnoptions = new MqttConnectOptions();
        mqttconnoptions.setAutomaticReconnect(true);
        mqttconnoptions.setCleanSession(false);
        mqttconnoptions.setUserName(username);
        mqttconnoptions.setPassword(password.toCharArray());
        MqttMessage msg = new MqttMessage();
        msg.setPayload(value.getBytes());

        try {
            mqttClient.connect(mqttconnoptions, getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    try {
                        mqttClient.publish(topic, msg);
                    } catch (MqttException ex){
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println(exception.getCause().getMessage());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }
    //--------26
}