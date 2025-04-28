package org.example.domain;

import com.google.gson.annotations.SerializedName;

public class Disease extends Entity<String>{
    @SerializedName("description")
    private String aliases;
    private String name;

    public Disease(String id, String aliases) {
        setId(id);
        this.aliases = aliases;
    }

    public Disease(String name) {
        this.name = name;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
