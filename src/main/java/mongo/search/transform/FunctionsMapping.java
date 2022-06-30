package mongo.search.transform;

public enum FunctionsMapping {
    EQ("eq","is"),
    NE("ne"),
    AND("and","andOperator"),
    OR("or","orOperator"),
    GT("gt"),
    LT("lt"),
    LE("lte"),
    GE("gte"),
    IN("in"),
    NIN("nin");

    private String filtersFunction;
    private String criteraFunction;

    private FunctionsMapping(String filters, String critera){
        this.filtersFunction = filters;
        this.criteraFunction = critera;
    }

    private FunctionsMapping(String function){
        this.criteraFunction = function;
        this.filtersFunction = function;
    }

    public String getFiltersFunction() {
        return filtersFunction;
    }

    public String getCriteraFunction() {
        return criteraFunction;
    }
}
