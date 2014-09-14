package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.StatCardGrade;
import org.voidsink.anewjkuapp.StatCardLva;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by paul on 14.09.2014.
 */

@SuppressLint("ValidFragment")
public class StatFragmentDetail extends BaseFragment {

    private final List<Lva> mLvas;
    private final List<String> mTerms;
    private final List<ExamGrade> mGrades;

    private View mView;
    private StatCardLva mStatCardLva;
    private StatCardGrade mStatCardGrade;
    private StatCardGrade mStatCardGradeWeighted;

    public StatFragmentDetail() {
        this("", new ArrayList<Lva>(), new ArrayList<ExamGrade>());
    }

    public StatFragmentDetail(String term, List<Lva> lvas, List<ExamGrade> grades) {
        this(Arrays.asList(new String[]{term}), lvas, grades);
    }

    public StatFragmentDetail(List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        super();

        this.mTerms = terms;
        this.mLvas = lvas;
        this.mGrades = grades;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_stats, container, false);

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CardView cv = (CardView) mView.findViewById(R.id.stat_card_lva);
        mStatCardLva = new StatCardLva(getContext(), this.mTerms, this.mLvas, this.mGrades);
        mStatCardLva.init();
        cv.setCard(mStatCardLva);

        cv = (CardView) mView.findViewById(R.id.stat_card_grade);
        mStatCardGrade = new StatCardGrade(getContext(), this.mTerms, this.mGrades, false);
        mStatCardGrade.init();
        cv.setCard(mStatCardGrade);

        cv = (CardView) mView.findViewById(R.id.stat_card_grade_weighted);
        mStatCardGradeWeighted = new StatCardGrade(getContext(), this.mTerms, this.mGrades, true);
        mStatCardGradeWeighted.init();
        cv.setCard(mStatCardGradeWeighted);
    }
}
