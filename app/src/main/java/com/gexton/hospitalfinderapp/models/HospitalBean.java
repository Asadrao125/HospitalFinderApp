package com.gexton.hospitalfinderapp.models;

public class HospitalBean {
    public String hospitalName;
    public String imageHospital;
    public Double lat, lng;
    public String address;

    public HospitalBean(String hospitalName, String imageHospital, Double lat, Double lng, String address) {
        this.hospitalName = hospitalName;
        this.imageHospital = imageHospital;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    @Override
    public String toString() {
        return "HospitalBean{" +
                "hospitalName='" + hospitalName + '\'' +
                ", imageHospital='" + imageHospital + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", address='" + address + '\'' +
                '}';
    }
    
}
