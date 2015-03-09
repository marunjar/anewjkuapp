package org.voidsink.anewjkuapp.kusss;

import android.text.TextUtils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Term implements Comparable<Term> {

    private static final Pattern termPattern = Pattern.compile("\\d{4}[WwSs]");

    public enum TermType {
        SUMMER("S"), WINTER("W");

        private final String value;

        private TermType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static TermType parseTermType(String text) throws ParseException {
            text = text.trim().toLowerCase();

            switch (text) {
                case "w":
                    return WINTER;
                case "s":
                    return SUMMER;
                default:
                    throw new ParseException("value is no valid char", 0);
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

    public static Term parseTerm(String termStr) throws ParseException {
        if (TextUtils.isEmpty(termStr) || termStr.length() > 5) {
            throw new ParseException("String is no valid term", 0);
        }

        Matcher matcher = termPattern.matcher(termStr);
        if (!matcher.find()) {
            throw new ParseException("String is no valid term", 0);
        }

        int year = Integer.parseInt(matcher.group().substring(0, 4));
        TermType type = TermType.parseTermType(matcher.group().substring(4));

        return new Term(year, type);
    }

    public boolean isEmpty() {
        return this.year < 0 || this.type == null;
    }

}
