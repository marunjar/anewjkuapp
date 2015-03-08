package org.voidsink.kussslib;

public enum Grade {
    G1(1, true, true), G2(2, true, true), G3(3, true, true), G4(4, true, true), G5(5, false, true),
    GET(1, true, false), GB(1, true, false), GAB(1, true, false);

    // TODO add "nicht teilgenommen" NT(5, false, false)
    
    private final boolean isPositive;
    private final boolean isNumber;
    private int value;

    private Grade(int value, boolean isPositive, boolean isNumber) {
        this.value = value;
        this.isPositive = isPositive;
        this.isNumber = isNumber;
    }
    
    public int getValue() {
        return value;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public boolean isPositive() {
        return isPositive;
    }
    
    
    public static Grade parseGrade(String text) {
        text = text.trim().toLowerCase();
        if (text.equals("sehr gut")) {
            return G1;
        } else if (text.equals("gut")) {
            return G2;
        } else if (text.equals("befriedigend")) {
            return G3;
        } else if (text.equals("genügend")) {
            return G4;
        } else if (text.equals("nicht genügend")) {
            return G5;
        } else if (text.equals("mit erfolg teilgenommen")) {
            return GET;
        } else if (text.equals("bestanden")) {
            return GB;
        } else if (text.equals("mit auszeichnung bestanden")) {
            return GAB;
        } else {
            return null;
        }
    }

    public static Grade parseGradeType(int ordinal) {
        return Grade.values()[ordinal];
    }
}

