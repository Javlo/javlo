package org.javlo.service.location;

public class IP2LocationResult {
	public String ip;
    public String country_code;
    public String country_name;
    public String region;
    public String city;
    public String isp;
    public float latitude;
    public float longitude;
    public String domain;
    public String zipcode;
    public String timezone;
    public String netspeed;
    public String iddcode;
    public String areacode;
    public String weatherstation_code;
    public String weatherstation_name;
    public String mcc;
    public String mnc;
    public String mobilebrand;
    public float elevation;
    public String usagetype;

    IP2LocationResult(String ipAddress) {
        ip = ipAddress;
    }
}
