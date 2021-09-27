package vcs.citydb.wfs.util.json;

import java.util.ArrayList;
import java.util.List;

public class ExceptionInfo {
    private String code;
    private List<String> description = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void addDescription(String description) {
        this.description.add(description);
    }

    public List<String> getDescription() {
        return description;
    }
}
