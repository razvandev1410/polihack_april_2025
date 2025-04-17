package org.example.domain;

import com.google.gson.annotations.SerializedName;

public class Disease extends Entity<String>{
    @SerializedName("description")
    private String aliases;

    public Disease(String id, String aliases) {
        setId(id);
        this.aliases = aliases;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }
}
