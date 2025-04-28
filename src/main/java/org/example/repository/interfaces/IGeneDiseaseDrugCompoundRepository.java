package org.example.repository.interfaces;

import org.example.domain.Compound;
import org.example.domain.Disease;
import org.example.domain.Drug;
import org.example.domain.Gene;
import org.example.repository.RepositoryException;

import java.util.HashMap;
import java.util.List;

public interface IGeneDiseaseDrugCompoundRepository {
    public boolean existGene(Gene potentialGene) throws RepositoryException;
    public boolean existDrug(Drug potentialDrug) throws RepositoryException;
    public boolean existDisease(Disease potentialDisease) throws RepositoryException;
    public boolean existCompound(Compound potentialCompound) throws RepositoryException;

    public HashMap<String, String> retrieveInfoAboutGene(Gene potentialGene) throws RepositoryException;
    public HashMap<String, String> retrieveInfoAboutDisease(Disease potentialDisease) throws RepositoryException;
    public HashMap<String, String> retrieveInfoAboutCompound(Compound potentialCompound) throws RepositoryException;
    public HashMap<String, String> retrieveInfoAboutDrug(Drug potentialDrug) throws RepositoryException;

    public List<String> getGeneNameSuggestion(Gene potentialGene) throws RepositoryException;
    public List<String> getCompoundNameSuggestion(Compound potentialCompound) throws RepositoryException;
    public List<String> getDrugNameSuggestion(Drug potentialDrug) throws RepositoryException;
}
