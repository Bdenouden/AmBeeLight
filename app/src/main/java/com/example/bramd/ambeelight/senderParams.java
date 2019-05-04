package com.example.bramd.ambeelight;

class senderParams {
    static byte [] colordata;
    static String targetIp = "192.168.1.0"; //fallback IP address

    senderParams(String target, byte [] colorDataIn){
        colordata=colorDataIn;
        targetIp=target;
    }

    public static byte[] getColordata() {return colordata;}
    public static String getTargetIp()  {return targetIp;}
}
