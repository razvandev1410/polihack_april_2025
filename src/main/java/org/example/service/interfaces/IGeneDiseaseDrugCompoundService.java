package org.example.service.interfaces;

import org.example.service.ServicesException;
import org.example.service.ServicesException;

import java.util.HashMap;
import java.util.List;

public interface IGeneDiseaseDrugCompoundService {
    public boolean existGene(String geneName) throws ServicesException;
    public boolean existDrug(String drugName) throws ServicesException;
    public boolean existDisease(String diseaseName) throws ServicesException;
    public boolean existCompound(String compoundName) throws ServicesException;

    public HashMap<String, String> retrieveInfoAboutGene(String geneName) throws ServicesException;
    public HashMap<String, String> retrieveInfoAboutDisease(String diseaseName) throws ServicesException;
    public HashMap<String, String> retrieveInfoAboutCompound(String compoundName) throws ServicesException;
    public HashMap<String, String> retrieveInfoAboutDrug(String drugName) throws ServicesException;

    public List<String> getGeneNameSuggestion(String genePrefix) throws ServicesException;
    public List<String> getCompoundNameSuggestion(String compoundPrefix) throws ServicesException;
    public List<String> getDrugNameSuggestion(String drugPrefix) throws ServicesException;
}
