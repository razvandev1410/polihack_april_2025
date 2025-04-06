package org.example.domain;

public class Gene extends Entity<Long>{
    private String geneName;
    private Integer geneId;
    private String description;
    private String summary;

    public Gene(String geneName) {
        this.geneName = geneName;
    }
    public Gene(Long id, String description, String summary) {
        setId(id);
        this.description = description;
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public Integer getGeneId() {
        return geneId;
    }

    public void setGeneId(Integer geneId) {
        this.geneId = geneId;
    }
}
