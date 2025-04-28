package org.example.service;

import org.example.domain.Gene;
import org.example.domain.Disease;
import org.example.domain.Compound;
import org.example.domain.Drug;
import org.example.domain.validation.PathwayComponentsValidator;
import org.example.domain.validation.ValidationException;
import org.example.repository.RepositoryException;
import org.example.repository.interfaces.IGeneDiseaseDrugCompoundRepository;
import org.example.repository.interfaces.IUserRepository;
import org.example.service.ServicesException;
import org.example.service.interfaces.IGeneDiseaseDrugCompoundService;
import org.example.service.interfaces.IUserService;

import java.util.HashMap;
import java.util.List;

/**
 * Service layer for gene, disease, drug, and compound operations.
 */
public class GeneDiseaseDrugCompundService implements IGeneDiseaseDrugCompoundService {
    private final IGeneDiseaseDrugCompoundRepository GeneDiseaseDrugCompoundRepository;
    private final IUserRepository userRepository;
    public GeneDiseaseDrugCompundService(IGeneDiseaseDrugCompoundRepository repository, IUserRepository userRepository) {
        this.GeneDiseaseDrugCompoundRepository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean existGene(String geneName) throws ServicesException {
        Gene gene = new Gene(geneName);
        try {
            PathwayComponentsValidator.validateGene(gene);
            return GeneDiseaseDrugCompoundRepository.existGene(gene);
        } catch (ValidationException ve) {
            throw new ServicesException("Gene validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error checking existence of gene: " + geneName, re);
        }
    }

    @Override
    public boolean existDrug(String drugName) throws ServicesException {
        Drug drug = new Drug(drugName);
        try {
            PathwayComponentsValidator.validateDrug(drug);
            return GeneDiseaseDrugCompoundRepository.existDrug(drug);
        } catch (ValidationException ve) {
            throw new ServicesException("Drug validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error checking existence of drug: " + drugName, re);
        }
    }

    @Override
    public boolean existDisease(String diseaseName) throws ServicesException {
        Disease disease = new Disease(diseaseName);
        try {
            PathwayComponentsValidator.validateDisease(disease);
            return GeneDiseaseDrugCompoundRepository.existDisease(disease);
        } catch (ValidationException ve) {
            throw new ServicesException("Disease validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error checking existence of disease: " + diseaseName, re);
        }
    }

    @Override
    public boolean existCompound(String compoundName) throws ServicesException {
        Compound compound = new Compound(compoundName);
        try {
            PathwayComponentsValidator.validateCompound(compound);
            return GeneDiseaseDrugCompoundRepository.existCompound(compound);
        } catch (ValidationException ve) {
            throw new ServicesException("Compound validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error checking existence of compound: " + compoundName, re);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutGene(String geneName) throws ServicesException {
        Gene gene = new Gene(geneName);
        try {
            PathwayComponentsValidator.validateGene(gene);
            return GeneDiseaseDrugCompoundRepository.retrieveInfoAboutGene(gene);
        } catch (ValidationException ve) {
            throw new ServicesException("Gene validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error retrieving gene info for: " + geneName, re);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutDisease(String diseaseName) throws ServicesException {
        Disease disease = new Disease(diseaseName);
        try {
            PathwayComponentsValidator.validateDisease(disease);
            return GeneDiseaseDrugCompoundRepository.retrieveInfoAboutDisease(disease);
        } catch (ValidationException ve) {
            throw new ServicesException("Disease validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error retrieving disease info for: " + diseaseName, re);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutCompound(String compoundName) throws ServicesException {
        Compound compound = new Compound(compoundName);
        try {
            PathwayComponentsValidator.validateCompound(compound);
            return GeneDiseaseDrugCompoundRepository.retrieveInfoAboutCompound(compound);
        } catch (ValidationException ve) {
            throw new ServicesException("Compound validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error retrieving compound info for: " + compoundName, re);
        }
    }

    @Override
    public HashMap<String, String> retrieveInfoAboutDrug(String drugName) throws ServicesException {
        Drug drug = new Drug(drugName);
        try {
            PathwayComponentsValidator.validateDrug(drug);
            return GeneDiseaseDrugCompoundRepository.retrieveInfoAboutDrug(drug);
        } catch (ValidationException ve) {
            throw new ServicesException("Drug validation failed", ve);
        } catch (RepositoryException re) {
            throw new ServicesException("Error retrieving drug info for: " + drugName, re);
        }
    }

    @Override
    public List<String> getGeneNameSuggestion(String genePrefix) throws ServicesException {
        Gene gene = new Gene(genePrefix);
        try {
            return GeneDiseaseDrugCompoundRepository.getGeneNameSuggestion(gene);
        } catch (RepositoryException re) {
            throw new ServicesException("Error fetching gene suggestions for prefix: " + genePrefix, re);
        }
    }

    @Override
    public List<String> getCompoundNameSuggestion(String compoundPrefix) throws ServicesException {
        Compound compound = new Compound(compoundPrefix);
        try {
            return GeneDiseaseDrugCompoundRepository.getCompoundNameSuggestion(compound);
        } catch (RepositoryException re) {
            throw new ServicesException("Error fetching compound suggestions for prefix: " + compoundPrefix, re);
        }
    }

    @Override
    public List<String> getDrugNameSuggestion(String drugPrefix) throws ServicesException {
        Drug drug = new Drug(drugPrefix);
        try {
            return GeneDiseaseDrugCompoundRepository.getDrugNameSuggestion(drug);
        } catch (RepositoryException re) {
            throw new ServicesException("Error fetching drug suggestions for prefix: " + drugPrefix, re);
        }
    }
}
