package it.cnr.iit.ck.features;

public class AppCategory {

    private final String packageName;
    private final String category;

    public AppCategory(String packageName, String category) {
        this.packageName = packageName;
        this.category = category;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getCategory() {
        return category;
    }
}
