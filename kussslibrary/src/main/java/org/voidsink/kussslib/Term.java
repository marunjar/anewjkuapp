package org.voidsink.kussslib;

public class Term implements Comparable<Term> {


    public enum TermType {
        SUMMER("S"), WINTER("W");

        private final String value;

        private TermType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static TermType parseTermType(String text) {
            text = text.trim().toLowerCase();
            if (text.equals("w")) {
                return WINTER;
            } else {
                return SUMMER;
            }
        }
    }

    ;

    private final int year;
    private final TermType type;
    private boolean loaded = false;

    public Term(int year, TermType type) {
        this.year = year;
        this.type = type;
    }

    public Term(String termStr) {
        this(Integer.parseInt(termStr.substring(0, 4)), TermType.parseTermType(termStr.substring(4)));
    }

    @Override
    public int compareTo(Term o) {
        if (o == null) {
            return -1;
        }

        if (this.year < o.getYear()) {
            return -1;
        } else if (this.year > o.getYear()) {
            return 1;
        }

        return this.type.compareTo(o.getType());
    }

    public int getYear() {
        return year;
    }

    public TermType getType() {
        return type;
    }

    public String toString() {
        return String.format("%d%s", year, type.toString());
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean mLoaded) {
        this.loaded = mLoaded;
    }

}
