package org.example.service;

import org.example.service.interfaces.IGeneDiseaseDrugCompoundService;
import org.example.service.interfaces.IUserService;

public class AllServices {
    private final IUserService userService;
    private final IGeneDiseaseDrugCompoundService geneDiseaseDrugCompoundService;

    public AllServices(IUserService userService, IGeneDiseaseDrugCompoundService geneDiseaseDrugCompoundService){
        this.userService = userService;
        this.geneDiseaseDrugCompoundService = geneDiseaseDrugCompoundService;
    }

    public IUserService getUserService() {
        return userService;
    }

    public IGeneDiseaseDrugCompoundService getGeneDiseaseDrugCompoundService() {
        return geneDiseaseDrugCompoundService;
    }
}
