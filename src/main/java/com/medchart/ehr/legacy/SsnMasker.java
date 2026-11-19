package com.medchart.ehr.legacy;

public final class SsnMasker {

    private static final String FULL_MASK = "***-**-****";

    private SsnMasker() {
    }

    public static String mask(Object ssn) {
        if (ssn == null) {
            return "";
        }
        String digits = ssn.toString().replaceAll("\\D", "");
        if (digits.length() < 4) {
            return FULL_MASK;
        }
        return "***-**-" + digits.substring(digits.length() - 4);
    }
}
