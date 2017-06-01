package com.example.minghan.expense;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class ChartFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private BarChart chart;

    private OnFragmentInteractionListener mListener;

    public ChartFragment() {}

    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        chart = (BarChart) view.findViewById(R.id.chart);

        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);

        final ArrayList<Expenses> expenses = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<String> tempLabels = new ArrayList<>();
        final List<BarEntry> entries = new ArrayList<>();

        ExpensesDB db = new ExpensesDB(getActivity());
        Cursor cursor = db.getCats();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        if(cursor.moveToFirst()){
            final int[] i = {0};
            do{
                String s = cursor.getString(cursor.getColumnIndex("cat_name"));
                labels.add(s);
            }while (cursor.moveToNext());

            for(int y=0; y < labels.size(); y++){
                final int finalY = y;

                Query reference = database.getReference(FirebaseInstanceId.getInstance().getId()+"/expenses")
                        .orderByChild("category").equalTo(labels.get(y));

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        expenses.clear();
                        double total = 0;
                        Expenses history = new Expenses();
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            for(DataSnapshot historySnapshot : dataSnapshot.getChildren()){
                                history = historySnapshot.getValue(Expenses.class);
                                total = total + history.getAmount();
                            }
                            tempLabels.add(history.getCategory());
                            entries.add(new BarEntry(i[0], (float)total));
                            i[0]++;
//                            Toast.makeText(getActivity(), labels.size()+"labels", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(getActivity(), finalY+"", Toast.LENGTH_SHORT).show();
                            if(finalY == (labels.size()-1)){
                                BarDataSet barDataSet = new BarDataSet(entries, "");
                                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                                BarData barData = new BarData(barDataSet);
                                IAxisValueFormatter formatter = new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        return tempLabels.get((int) value);
                                    }
                                };
                                XAxis xAxis = chart.getXAxis();
                                xAxis.setGranularity(1f);
                                xAxis.setValueFormatter(formatter);
                                barData.setBarWidth(0.9f);
                                chart.setData(barData);
                                chart.setFitBars(true);
                                chart.animateY(4200);
                                chart.getLegend().setEnabled(false);
                                chart.setDrawGridBackground(false);
                                chart.invalidate();
                                Description description = new Description();
                                description.setText("");
                                chart.setDescription(description);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
