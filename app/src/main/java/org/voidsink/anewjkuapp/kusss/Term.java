/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.kusss;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Term implements Comparable<Term> {

    private final int year;
    private final TermType type;
    private boolean loaded = false;

    private static final Pattern termPattern = Pattern.compile("\\d{4}[WwSs]");

    public static Term fromDate(Date date) {
        if (date == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // set to start of summer semester 1.3.
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        if (date.before(cal.getTime())) {
            return new Term(cal.get(Calendar.YEAR) - 1, TermType.WINTER);
        }

        // set to start of winter semester 1.10.
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        if (date.before(cal.getTime())) {
            return new Term(cal.get(Calendar.YEAR), TermType.SUMMER);
        }

        return new Term(cal.get(Calendar.YEAR), TermType.WINTER);
    }

    public enum TermType {
        SUMMER("S"), WINTER("W");

        private final String value;

        TermType(String value) {
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }

        public static TermType parseTermType(String text) throws ParseException {
            String normalized = text.trim().toLowerCase();

            switch (normalized) {
                case "w":
                    return WINTER;
                case "s":
                    return SUMMER;
                default:
                    throw new ParseException("value is no valid char", 0);
            }
        }
    }

    private Term(int year, @NonNull TermType type) {
        this.year = year;
        this.type = type;
    }

    @Override
    public int compareTo(@NonNull Term o) {
        if (this.year < o.getYear()) {
            return -1;
        } else if (this.year > o.getYear()) {
            return 1;
        }

        return this.type.compareTo(o.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Term)) return false;
        Term t = (Term) o;
        return this.compareTo(t) == 0;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(this.year, this.type);
        } else {
            int result = 1;

            result = 31 * result + this.year;
            result = 31 * result + this.type.hashCode();

            return result;
        }
    }

    private int getYear() {
        return year;
    }

    private TermType getType() {
        return type;
    }

    public Date getStart() {
        final Calendar cal = Calendar.getInstance();

        switch (type) {
            case WINTER:
                cal.set(year, Calendar.OCTOBER, 1);
                break;
            case SUMMER:
                cal.set(year, Calendar.MARCH, 1);
                break;
            default:
                throw new IllegalArgumentException("value is no valid char");
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public Date getEnd() {
        final Calendar cal = Calendar.getInstance();

        switch (type) {
            case WINTER:
                cal.set(year + 1, Calendar.FEBRUARY, 1, 23, 59, 59);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case SUMMER:
                cal.set(year, Calendar.SEPTEMBER, 30, 23, 59, 59);
                break;
            default:
                throw new IllegalArgumentException("value is no valid char");
        }

        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));

        return cal.getTime();
    }


    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMAN, "%d%s", year, type.toString());
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
        return this.year < 0;
    }

}
