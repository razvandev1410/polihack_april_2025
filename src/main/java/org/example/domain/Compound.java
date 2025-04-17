package org.example.domain;

import com.google.gson.annotations.SerializedName;

public class Compound extends Entity<String>{
    @SerializedName("description")
    private String eliases;

    public Compound(String id, String eliases) {
        setId(id);
        this.eliases = eliases;
    }


    public String getEliases() {
        return eliases;
    }

    public void setEliases(String eliases) {
        this.eliases = eliases;
    }
}
