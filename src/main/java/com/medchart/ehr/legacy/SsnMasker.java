package com.medchart.ehr.legacy;

public final class SsnMasker {

    private static final String MASK_PREFIX = "***-**-";

    private SsnMasker() {
    }

    public static String mask(Object ssn) {
        if (ssn == null) {
            return "";
        }
        String digits = ssn.toString().replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return MASK_PREFIX + "****";
        }
        return MASK_PREFIX + digits.substring(digits.length() - 4);
    }
}
