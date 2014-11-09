package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.GradeCard;
import org.voidsink.anewjkuapp.GradeCardArrayAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.view.StickyCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

@SuppressLint("ValidFragment")
public class GradeDetailFragment extends BaseFragment {

    public static final String TAG = GradeDetailFragment.class.getSimpleName();

    private GradeCardArrayAdapter mAdapter;

    private List<ExamGrade> mGrades;

    public GradeDetailFragment(List<String> terms, List<ExamGrade> grades) {
        this.mGrades = AppUtils.filterGrades(terms, grades);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_grade_detail, container,
                false);

        final StickyCardListView mListView = (StickyCardListView) view.findViewById(R.id.grade_card_list);

        List<Card> gradeCards = new ArrayList<>();
        for (ExamGrade g : this.mGrades) {
            gradeCards.add(new GradeCard(getContext(), g));
        }
        mAdapter = new GradeCardArrayAdapter(getContext(), gradeCards);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grade, menu);
    }
}
