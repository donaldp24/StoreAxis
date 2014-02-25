package com.wallmat.StoreAxis;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Donald Pae
 * Date: 1/17/14
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommonData {

    //enum for transform
    public static final int TRANSFORM_ALPHA_FADE = 0;
    public static final int TRANSFORM_TRANSFORM_SLIDE = 1;
    public static final int TRANSFORM_ALPHA_SLIDE = 2;

    public static final int transformAnimation = TRANSFORM_ALPHA_SLIDE; //trans_0

    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
