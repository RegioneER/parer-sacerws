package it.eng.parer.exception;

public class ParerErrorCategory {

    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String USER_ERROR = "USER_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    public static SacerWsErrorCategory asEnum(String val) {
        return SacerWsErrorCategory.valueOf(val);
    }

    // Enum
    public enum SacerWsErrorCategory {

        INTERNAL_ERROR, USER_ERROR, VALIDATION_ERROR
    }
}
