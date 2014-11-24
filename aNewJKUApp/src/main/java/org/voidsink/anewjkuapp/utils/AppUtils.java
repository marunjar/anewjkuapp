package org.voidsink.anewjkuapp.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import org.voidsink.anewjkuapp.ImportPoiTask;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.fragment.MapFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.GradeType;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.kusss.Studies;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class AppUtils {

    private static final String DEFAULT_POI_FILE_NAME = "JKU.gpx";
    private static final String TAG = AppUtils.class.getSimpleName();

    public static void doOnNewVersion(Context context) {
        int mLastVersion = PreferenceWrapper.getLastVersion(context);
        int mCurrentVersion = PreferenceWrapper.getCurrentVersion(context);

        if (mLastVersion != mCurrentVersion
                || mLastVersion == PreferenceWrapper.PREF_LAST_VERSION_NONE) {
            boolean errorOccured = false;

            try {
                if (!initPreferences(context)) {
                    errorOccured = true;
                }
                if (!importDefaultPois(context)) {
                    errorOccured = true;
                }
                if (!copyDefaultMap(context)) {
                    errorOccured = true;
                }
                if (shouldRemoveOldAccount(mLastVersion, mCurrentVersion)) {
                    if (!removeAccount(context)) {
                        errorOccured = true;
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "doOnNewVersion failed", e);
                Analytics.sendException(context, e, false);
                errorOccured = true;
            }
            if (!errorOccured) {
                PreferenceWrapper.setLastVersion(context, mCurrentVersion);
            }
        }
    }

    private static boolean removeAccount(Context context) {
        Account account = getAccount(context);
        if (account != null) {
            AccountManager.get(context).removeAccount(account, null, null);
            Log.d(TAG, "account removed");
        }
        return true;
    }

    private static boolean shouldRemoveOldAccount(int lastVersion,
                                                  int currentVersion) {
        // calendar names changed with 100017, remove account for avoiding
        // corrupted data
        return (lastVersion < 100017 && currentVersion >= 100017);
    }

    private static boolean initPreferences(Context context) {
        try {
            PreferenceManager.setDefaultValues(context, R.xml.preference_app,
                    true);
            PreferenceManager.setDefaultValues(context,
                    R.xml.preference_dashclock_extension_mensa, true);
            PreferenceManager.setDefaultValues(context, R.xml.preference_kusss,
                    true);
        } catch (Exception e) {
            Log.e(TAG, "initPreferences", e);
            return false;
        }
        return true;
    }

    private static boolean copyDefaultMap(Context context) {
        try {
            // write file to sd for mapsforge
            OutputStream mapFileWriter = new BufferedOutputStream(
                    context.openFileOutput(MapFragment.MAP_FILE_NAME,
                            Context.MODE_PRIVATE));
            InputStream assetData = new BufferedInputStream(context.getAssets()
                    .open(MapFragment.MAP_FILE_NAME));

            byte[] buffer = new byte[1024];
            int len = assetData.read(buffer);
            while (len != -1) {
                mapFileWriter.write(buffer, 0, len);
                len = assetData.read(buffer);
            }
            mapFileWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyDefaultMap", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "copyDefaultMap", e);
            return false;
        }
        return true;
    }

    private static boolean importDefaultPois(Context context) {
        // import JKU Pois
        try {
            // write file to sd for import
            OutputStream mapFileWriter = new BufferedOutputStream(
                    context.openFileOutput(DEFAULT_POI_FILE_NAME,
                            Context.MODE_PRIVATE));
            InputStream assetData = new BufferedInputStream(context.getAssets()
                    .open(DEFAULT_POI_FILE_NAME));

            byte[] buffer = new byte[1024];
            int len = assetData.read(buffer);
            while (len != -1) {
                mapFileWriter.write(buffer, 0, len);
                len = assetData.read(buffer);
            }
            mapFileWriter.close();

            // import file
            new ImportPoiTask(context, new File(context.getFilesDir(),
                    DEFAULT_POI_FILE_NAME), true).execute();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "importDefaultPois", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "importDefaultPois", e);
            return false;
        }
        return true;
    }

    public static double getECTS(LvaState state, List<LvaWithGrade> lvas) {
        double sum = 0;
        for (LvaWithGrade lva : lvas) {
            if (state == LvaState.ALL || state == lva.getState()) {
                sum += lva.getLva().getEcts();
            }
        }
        return sum;
    }

    public static void sortLVAs(List<Lva> lvas) {
        Collections.sort(lvas, new Comparator<Lva>() {

            @Override
            public int compare(Lva lhs, Lva rhs) {
                int value = lhs.getTitle().compareTo(rhs.getTitle());
                if (value == 0) {
                    value = lhs.getTerm().compareTo(rhs.getTerm());
                }
                return value;
            }
        });
    }

    public static void sortLVAsWithGrade(List<LvaWithGrade> lvas) {
        Collections.sort(lvas, new Comparator<LvaWithGrade>() {

            @Override
            public int compare(LvaWithGrade lhs, LvaWithGrade rhs) {
                int value = lhs.getState().compareTo(rhs.getState());
                if (value == 0) {
                    value = lhs.getLva().getTitle()
                            .compareTo(rhs.getLva().getTitle());
                }
                if (value == 0) {
                    value = lhs.getLva().getTerm()
                            .compareTo(rhs.getLva().getTerm());
                }
                return value;
            }
        });

    }

    public static void sortGrades(List<ExamGrade> grades) {
        Collections.sort(grades, new Comparator<ExamGrade>() {

            @Override
            public int compare(ExamGrade lhs, ExamGrade rhs) {
                int value = lhs.getGradeType().compareTo(rhs.getGradeType());
                if (value == 0) {
                    value = rhs.getDate().compareTo(lhs.getDate());
                }
                if (value == 0) {
                    value = rhs.getTerm().compareTo(lhs.getTerm());
                }
                if (value == 0) {
                    value = lhs.getTitle().compareTo(rhs.getTitle());
                }
                return value;
            }
        });
    }

    public static void removeDuplicates(List<LvaWithGrade> mLvas) {
        // remove done duplicates
        int i = 0;
        while (i < mLvas.size()) {
            if (mLvas.get(i).getState() == LvaState.DONE) {
                Lva lva = mLvas.get(i).getLva();
                int j = i + 1;

                while (j < mLvas.size()) {
                    Lva nextLva = mLvas.get(j).getLva();
                    if (lva.getCode().equals(nextLva.getCode())
                            && lva.getTitle().equals(nextLva.getTitle())) {
                        mLvas.remove(j);
                        Log.d("removeDuplicates",
                                "remove from done " + nextLva.getCode() + " "
                                        + nextLva.getTitle());
                    } else {
                        j++;
                    }
                }
            }
            i++;
        }

        // remove all other duplicates
        i = 0;
        while (i < mLvas.size()) {
            if (mLvas.get(i).getState() != LvaState.DONE) {
                Lva lva = mLvas.get(i).getLva();
                int j = i + 1;

                while (j < mLvas.size()) {
                    Lva nextLva = mLvas.get(j).getLva();
                    if (lva.getCode().equals(nextLva.getCode())
                            && lva.getTitle().equals(nextLva.getTitle())) {
                        mLvas.remove(j);
                        Log.d("removeDuplicates",
                                "remove from other " + nextLva.getCode() + " "
                                        + nextLva.getTitle());
                    } else {
                        j++;
                    }
                }
            }
            i++;
        }

    }

    public static void removeDuplicates(List<LvaWithGrade> mDoneLvas,
                                        List<LvaWithGrade> mOpenLvas) {

        // Log.i("removeDuplicates", "---------");
        // for (LvaWithGrade lva : mDoneLvas) {
        // Log.i("removeDuplicates", "done: " + lva.getLva().getCode() + " "
        // + lva.getLva().getTitle());
        // }
        // for (LvaWithGrade lva : mOpenLvas) {
        // Log.i("removeDuplicates", "open: " + lva.getLva().getCode() + " "
        // + lva.getLva().getTitle());
        // }

        int i = 0;
        while (i < mDoneLvas.size()) {
            Lva lva = mDoneLvas.get(i).getLva();
            int j = i + 1;

            while (j < mDoneLvas.size()) {
                Lva nextLva = mDoneLvas.get(j).getLva();
                if (lva.getCode().equals(nextLva.getCode())
                        && lva.getTitle().equals(nextLva.getTitle())) {
                    mDoneLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from done " + nextLva.getCode() + " "
                                    + nextLva.getTitle());
                } else {
                    j++;
                }
            }

            j = 0;
            while (j < mOpenLvas.size()) {
                Lva nextLva = mOpenLvas.get(j).getLva();
                if (lva.getCode().equals(nextLva.getCode())
                        && lva.getTitle().equals(nextLva.getTitle())) {
                    mOpenLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from open " + nextLva.getCode() + " "
                                    + nextLva.getTitle());
                } else {
                    j++;
                }
            }

            i++;
        }

        i = 0;
        while (i < mOpenLvas.size()) {
            Lva lva = mOpenLvas.get(i).getLva();
            int j = i + 1;

            while (j < mOpenLvas.size()) {
                Lva nextLva = mOpenLvas.get(j).getLva();
                if (lva.getCode().equals(nextLva.getCode())
                        && lva.getTitle().equals(nextLva.getTitle())) {
                    mOpenLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from open " + nextLva.getCode() + " "
                                    + nextLva.getTitle());
                } else {
                    j++;
                }
            }
            i++;
        }
    }

    public static double getAvgGrade(List<ExamGrade> grades,
                                     boolean ectsWeighting, GradeType type, boolean positiveOnly) {
        double sum = 0;
        double count = 0;

        if (grades != null) {
            for (ExamGrade grade : grades) {
                if (type == GradeType.ALL || type == grade.getGradeType()) {
                    if (!positiveOnly || grade.getGrade().isPositive()) {
                        if (!ectsWeighting) {
                            sum += grade.getGrade().getValue();
                            count++;
                        } else {
                            sum += grade.getEcts() * grade.getGrade().getValue();
                            count += grade.getEcts();
                        }
                    }
                }
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return sum / count;
        }
    }

    public static Account getAccount(Context context) {
        // get first account
        Account[] accounts = AccountManager.get(context).getAccountsByType(
                KusssAuthenticator.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            return null;
        }
        return accounts[0];
    }

    public static String getAccountName(Context context, Account account) {
        if (account == null) {
            return null;
        }
        return account.name;
    }

    public static String getAccountPassword(Context context, Account account) {
        if (account == null) {
            return null;
        }
        return AccountManager.get(context).getPassword(account);
    }

    @SuppressLint("NewApi")
    public static String getAccountAuthToken(Context context, Account account) {
        if (account == null) {
            return null;
        }

        AccountManager am = AccountManager.get(context);
        AccountManagerFuture<Bundle> response = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            response = am.getAuthToken(account,
                    KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY, null, true,
                    null, null);
        } else {
            response = am.getAuthToken(account,
                    KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY, true, null,
                    null);
        }

        try {
            return response.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        } catch (OperationCanceledException | AuthenticatorException
                | IOException e) {
            Log.e(TAG, "getAccountAuthToken", e);
            return null;
        }
    }

    public static void addSerieToPieChart(PieChart chart, String category,
                                          double value, int color) {
        if (value > 0) {
            EmbossMaskFilter emf = new EmbossMaskFilter(
                    new float[]{1, 1, 1}, 0.7f, 11.5f, 2f);
            Segment segment = new Segment(category, value);
            SegmentFormatter formatter = new SegmentFormatter(color,
                    Color.TRANSPARENT, Color.TRANSPARENT, Color.DKGRAY);
            formatter.getFillPaint().setMaskFilter(emf);

            chart.addSegment(segment, formatter);
        }
    }

    public static double getGradePercent(List<ExamGrade> grades, Grade grade, boolean ectsWeighting) {
        if (grades.size() == 0) return 0;

        double count = 0;
        double sum = 0;
        double value;

        for (ExamGrade examGrade : grades) {
            if (!ectsWeighting) {
                value = 1;
            } else {
                value = examGrade.getEcts();
            }

            if (examGrade.getGrade().equals(grade)) {
                count += value;
            }
            sum += value;
        }

        return count / sum * 100;
    }

    public static List<LvaWithGrade> getLvasWithGrades(List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        List<LvaWithGrade> result = new ArrayList<LvaWithGrade>();

        for (Lva lva : lvas) {
            if (terms.contains(lva.getTerm())) {
                ExamGrade grade = findGrade(grades, lva);
                result.add(new LvaWithGrade(lva, grade));
            }
        }

        AppUtils.removeDuplicates(result);

        AppUtils.sortLVAsWithGrade(result);

        return result;
    }

    private static ExamGrade findGrade(List<ExamGrade> grades, Lva lva) {
        ExamGrade finalGrade = null;

        for (ExamGrade grade : grades) {
            if (grade.getCode().equals(lva.getCode())) {
                if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
                    finalGrade = grade;
                }
            }
        }
        if (finalGrade == null) {
            Log.d(TAG, "findByLvaNr: " + lva.getLvaNr() + "/" + lva.getTitle());
            for (ExamGrade grade : grades) {
                if (grade.getLvaNr().equals(lva.getLvaNr())) {
                    if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
                        finalGrade = grade;
                    }
                }
            }
        }

        return finalGrade;
    }

    private static void addIfRecent(List<ExamGrade> grades, ExamGrade grade) {
        int i = 0;
        while (i < grades.size()) {
            ExamGrade g = grades.get(i);
            // check only grades for same lva and term
            if (g.getCode().equals(grade.getCode())
                    && g.getLvaNr().equals(grade.getLvaNr())) {
                // keep only recent (best and newest) grade
                if (g.getDate().before(grade.getDate())) {
                    // remove last grade
                    grades.remove(i);
                } else {
                    // break without adding
                    return;
                }
            } else {
                i++;
            }
        }
        // finally add grade
        grades.add(grade);
    }

    public static List<ExamGrade> filterGrades(List<String> terms, List<ExamGrade> grades) {
        List<ExamGrade> result = new ArrayList<ExamGrade>();
        if (grades != null) {
            for (ExamGrade grade : grades) {
                if (terms == null || (terms.indexOf(grade.getTerm()) >= 0)) {
                    addIfRecent(result, grade);
                }
            }
        }
        AppUtils.sortGrades(result);

        return result;
    }

    public static void sortStudies(List<Studies> mStudies) {
        Collections.sort(mStudies, new Comparator<Studies>() {

            @Override
            public int compare(Studies lhs, Studies rhs) {
                int value = lhs.getUni().compareToIgnoreCase(rhs.getUni());
                if (value == 0) {
                    value = lhs.getDtStart().compareTo(rhs.getDtStart());
                }
                if (value == 0) {
                    value = lhs.getSkz().compareTo(rhs.getSkz());
                }
                return value;
            }
        });

    }
}
