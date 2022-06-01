package com.laundromat.merchant.utils;

import com.laundromat.merchant.model.washable.ServiceType;

import java.util.ArrayList;
import java.util.List;

public class Globals {

    public static boolean orderInView = false;

    private static List<ServiceType> availableServices;

    // return a shallow copy of available services
    public static ArrayList<ServiceType> getAvailableServices() {

        ArrayList<ServiceType> serviceTypes = new ArrayList<>();

        for (ServiceType serviceType : Globals.availableServices) {

            ServiceType availableServiceType = new ServiceType(serviceType);
            serviceTypes.add(availableServiceType);
        }

        return serviceTypes;
    }

    public static void setAvailableServices(List<ServiceType> availableServices) {
        Globals.availableServices = availableServices;
    }
}
