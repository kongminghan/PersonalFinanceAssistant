package com.example.minghan.expense;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    private FloatingActionButton fabCat;
    private FloatingActionButton fabExp;
    private FloatingActionButton fabIncome;
    private FloatingActionMenu fabMenu;
    private Spinner spinner;
    private TextView tvAmount;
    private FirebaseDatabase database;
    private ArrayList<Expenses> expensess;
    private RecyclerView recyclerView;

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        fabMenu = (FloatingActionMenu)view.findViewById(R.id.menu);
        fabExp = (FloatingActionButton)view.findViewById(R.id.fabAdd);
        fabCat = (FloatingActionButton)view.findViewById(R.id.fabCat);
//        fabIncome = (FloatingActionButton)view.findViewById(R.id.fabIncome);
        tvAmount = (TextView)view.findViewById(R.id.tvAmount);
        recyclerView = (RecyclerView)view.findViewById(R.id.rvTransaction);
        final LinearLayout layEmpty = (LinearLayout)view.findViewById(R.id.layEmpty);

        expensess = new ArrayList<>();
        final RVAdapterExpenses adapter = new RVAdapterExpenses(getActivity(), expensess);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        database = FirebaseDatabase.getInstance();

        DatabaseReference reference = database.getReference(FirebaseInstanceId.getInstance().getId()+"/expenses");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expensess.clear();
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    for(DataSnapshot historySnapshot : dataSnapshot.getChildren()){
                        Expenses history = historySnapshot.getValue(Expenses.class);
                        history.setId(historySnapshot.getKey());
                        expensess.add(history);

                        if(expensess.size()==0){
                            recyclerView.setVisibility(View.GONE);
                            layEmpty.setVisibility(View.VISIBLE);
                        }else{
                            layEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    recyclerView.setVisibility(View.GONE);
                    layEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

//        fabIncome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        fabExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog();
            }
        });

        fabCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupCatDialog();
            }
        });
        updateAmount();
        return view;
    }

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void setupDialog() {
        final Dialog dialogEt = new Dialog(getActivity());
        dialogEt.setContentView(R.layout.modal_add_exp);
        spinner = (Spinner) dialogEt.findViewById(R.id.category);

        new Thread(new Runnable() {
            @Override
            public void run() {

                ExpensesDB expensesDB = new ExpensesDB(getActivity());
                final ArrayList<String> categories = new ArrayList<String>();
                Cursor cursor = expensesDB.getCats();
                if(cursor.moveToFirst()){
                    do{
                        categories.add(cursor.getString(cursor.getColumnIndex("cat_name")));
                    }while (cursor.moveToNext());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        }
                    });
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Please create a category", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

        Button btnAdd = (Button) dialogEt.findViewById(R.id.btnAdd);
        final EditText etAmount = (EditText) dialogEt.findViewById(R.id.etAmount);
        TextView tvDate = (TextView) dialogEt.findViewById(R.id.tvDate);

        final Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        final String dateString = sdf.format(date);
        tvDate.setText(dateString);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogEt.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogEt.getWindow().setAttributes(lp);
        dialogEt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etAmount.getText().toString().trim().equals("") && spinner.getSelectedItem() != null){
                    DatabaseReference reference = database.getReference(FirebaseInstanceId.getInstance().getId()+"/expenses");
                    final DatabaseReference reference2 = database.getReference(FirebaseInstanceId.getInstance().getId()+"/amount");
                    final Expenses expenses = new Expenses();

                    expenses.setAmount(Double.parseDouble(etAmount.getText().toString()));
                    expenses.setCategory(spinner.getSelectedItem().toString());
                    String key = reference.push().getKey();
                    reference.child(key).setValue(expenses);
                    reference.child(key).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    final double[] amount = {0};
                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                amount[0] = dataSnapshot.getValue(Double.class);
                            }catch (Exception e){
                                Log.d("Firebase Error", e.getMessage());
                            }
                            amount[0] = amount[0] + expenses.getAmount();
                            reference2.setValue(amount[0]);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dialogEt.dismiss();
                }else{
                    Toast.makeText(getActivity(), "Please provide your expenses amount and category", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogEt.show();
    }

    private void setupCatDialog() {
        final Dialog dialogEt = new Dialog(getActivity());
        dialogEt.setContentView(R.layout.modal_add_cat);

        Button btnAdd = (Button) dialogEt.findViewById(R.id.btnAdd);
        final EditText etCat = (EditText) dialogEt.findViewById(R.id.etCat);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogEt.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogEt.getWindow().setAttributes(lp);
        dialogEt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etCat.getText().toString().trim().equals("")){
                    final String s = etCat.getText().toString();

                    database.getReference();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ExpensesDB db = new ExpensesDB(getActivity());
                            db.addCat(s);
                            db.close();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogEt.dismiss();
                                }
                            });
                        }
                    }).start();
                }else{
                    Toast.makeText(getActivity(), "Please provide your expenses amount and category", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogEt.show();
    }

    private void updateAmount(){
        final double[] amount = {0};
        DatabaseReference reference = database.getReference(FirebaseInstanceId.getInstance().getId()+"/amount");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    amount[0] = dataSnapshot.getValue(Double.class);
                    NumberFormat formatter = new DecimalFormat("#0.00");
                    tvAmount.setText(formatter.format(amount[0])+"");
                }catch(Exception e){
                    Log.d("FBERROR", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

}
