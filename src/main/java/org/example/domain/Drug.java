package org.example.domain;

import com.google.gson.annotations.SerializedName;

public class Drug extends Entity<String>{
    @SerializedName("description")
    private String aliases;

    public Drug(String ID, String aliases) {
        setId(ID);
        this.aliases = aliases;
    }


    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }
}
