package com.example.pickupappaz;

import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.azure.core.credential.TokenCredential;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.net.InetSocketAddress;
import java.net.Proxy;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;

import java.io.IOException;

public class MachineDataService extends Service {

    public static final String ACTION_MILK_VOL_BROADCAST = "ACTION_MILK_VOL_BROADCAST";
    public static final String ACTION_MILK_TEMP_BROADCAST = "ACTION_MILK_TEMP_BROADCAST";
    public static final String ACTION_TRUCK_ARRIVAL = "ACTION_TRUCK_ARRIVAL";
    public static final String ACTION_PICKUP = "ACTION_PICKUP";

    public static final String TANK_TEMP = "ns:MilkTemperature";
    public static final String TANK_PICKUP = "ns:MilkPickupEvent";
    public static final String TANK_VOLUME = "ns:MilkVolume";
    public static final String TRUCK_ARRIVAL = "ns:TruckArrivalEvent";

    private static final String EH_COMPATIBLE_CONNECTION_STRING_FORMAT = "Endpoint=%s/;EntityPath=%s;"
            + "SharedAccessKeyName=%s;SharedAccessKey=%s";
    // az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}
    private static final String EVENT_HUBS_COMPATIBLE_ENDPOINT = "sb://ihsuprodmlres018dednamespace.servicebus.windows.net/";

    // az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}
    private static final String EVENT_HUBS_COMPATIBLE_PATH = "iothub-ehub-testhubau-5031984-55e883bf26";

    // az iot hub policy show --name service --query primaryKey --hub-name {your IoT Hub name}
    private static final String IOT_HUB_SAS_KEY = "W0Dpd/TMZMLZbqlPaWJ05mwUd8wbEQnwDlIgsDAp24I=";
    private static final String IOT_HUB_SAS_KEY_NAME = "service";

    private final IBinder binder = new MDSBinder();
    private double temperature = 0.0;
    private double volume = 0.0;


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

        // Build the Event Hubs compatible connection string.
        String eventHubCompatibleConnectionString = String.format(EH_COMPATIBLE_CONNECTION_STRING_FORMAT,
                EVENT_HUBS_COMPATIBLE_ENDPOINT, EVENT_HUBS_COMPATIBLE_PATH, IOT_HUB_SAS_KEY_NAME, IOT_HUB_SAS_KEY);

        // Setup the EventHubBuilder by configuring various options as needed.
        EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(eventHubCompatibleConnectionString);

        try (EventHubConsumerAsyncClient eventHubConsumerAsyncClient = eventHubClientBuilder.buildAsyncConsumerClient()) {
            System.out.println("try-----------");
            receiveFromAllPartitions(eventHubConsumerAsyncClient);

            // uncomment to run these samples
            // receiveFromSinglePartition(eventHubConsumerAsyncClient);
            // receiveFromSinglePartitionInBatches(eventHubConsumerAsyncClient);

            // Shut down cleanly.
        }

    }
    /**
     * This method receives events from all partitions asynchronously starting from the newly available events in
     * each partition.
     *
     * @param eventHubConsumerAsyncClient The {@link EventHubConsumerAsyncClient}.
     */
    private static void receiveFromAllPartitions(EventHubConsumerAsyncClient eventHubConsumerAsyncClient) {

        eventHubConsumerAsyncClient
                .receive(false) // set this to false to read only the newly available events
                .subscribe(partitionEvent -> {
                    System.out.println();
                    System.out.printf("%nTelemetry received from partition %s:%n%s",
                            partitionEvent.getPartitionContext().getPartitionId(), partitionEvent.getData().getBodyAsString());
                    System.out.printf("%nApplication properties (set by device):%n%s", partitionEvent.getData().getProperties());
                    System.out.printf("%nSystem properties (set by IoT Hub):%n%s",
                            partitionEvent.getData().getSystemProperties());
                }, ex -> {
                    System.out.println("Error receiving events " + ex);
                }, () -> {
                    System.out.println("Completed receiving events");
                });
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

    public class MDSBinder extends Binder {
        MachineDataService getService(){
            return MachineDataService.this;
        }
    }
}