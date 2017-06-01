package com.example.minghan.expense;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
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

/**
 * Created by MingHan on 6/1/2017.
 */

public class RVAdapterExpenses extends RecyclerView.Adapter<RVAdapterExpenses.MyViewHolder> {

    private Context context;
    private ArrayList<Expenses> expenses;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        holder.tvAmount.setText("RM"+formatter.format(expenses.get(position).getAmount())+"");
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        holder.tvTime.setText(sfd.format(new Date(expenses.get(position).getTimestamp())));
        holder.tvCategory.setText(expenses.get(position).getCategory());
        holder.tvID.setText(expenses.get(position).getId());

        holder.layItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog(holder.tvID.getText().toString(), expenses.get(position).getAmount(), position);
            }
        });
    }

    private void setupDialog(final String key, final double exp, final int position) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final Dialog dialogEt = new Dialog(context);
        dialogEt.setContentView(R.layout.modal_edit_exp);
        final Spinner spinner = (Spinner) dialogEt.findViewById(R.id.edit_category);
        ExpensesDB expensesDB = new ExpensesDB(context);
        final ArrayList<String> categories = new ArrayList<String>();
        Cursor cursor = expensesDB.getCats();
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndex("cat_name")));
            } while (cursor.moveToNext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        } else {
            Toast.makeText(context, "Please create a category", Toast.LENGTH_SHORT).show();
        }

        Button btnAdd = (Button) dialogEt.findViewById(R.id.btnAdd);
        Button btnDelete = (Button) dialogEt.findViewById(R.id.btnDelete);
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
//        dialogEt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        NumberFormat formatter = new DecimalFormat("#0.00");
        etAmount.setText(formatter.format(exp)+"");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etAmount.getText().toString().trim().equals("") && spinner.getSelectedItem() != null) {
                    DatabaseReference reference = database.getReference(FirebaseInstanceId.getInstance().getId() + "/expenses");
                    final DatabaseReference reference2 = database.getReference(FirebaseInstanceId.getInstance().getId() + "/amount");
                    final Expenses expenses = new Expenses();

                    expenses.setAmount(Double.parseDouble(etAmount.getText().toString()));
                    expenses.setCategory(spinner.getSelectedItem().toString());
                    reference.child(key).setValue(expenses);
                    reference.child(key).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    final double[] amount = {0};
                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                amount[0] = dataSnapshot.getValue(Double.class);
                            } catch (Exception e) {
                                Log.d("Firebase Error", e.getMessage());
                            }
                            amount[0] = amount[0] - exp + expenses.getAmount();
                            reference2.setValue(amount[0]);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    dialogEt.dismiss();
                } else {
                    Toast.makeText(context, "Please provide your expenses amount and category", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Delete");
                alert.setMessage("Are you sure you want to delete?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DatabaseReference databaseReference = database.getReference(FirebaseInstanceId.getInstance().getId() + "/expenses");
                        final DatabaseReference reference2 = database.getReference(FirebaseInstanceId.getInstance().getId() + "/amount");
                        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                double amount = dataSnapshot.getValue(Double.class);
                                amount = amount - Double.parseDouble(etAmount.getText().toString());
                                reference2.setValue(amount);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        databaseReference.child(key).removeValue();
                        expenses.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
        dialogEt.show();
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tvAmount, tvCategory, tvTime, tvID;
        public LinearLayout layItem;
        public MyViewHolder(View view){
            super(view);
            tvAmount = (TextView)view.findViewById(R.id.tvAmount);
            tvCategory = (TextView)view.findViewById(R.id.tvCat);
            tvTime = (TextView)view.findViewById(R.id.tvTime);
            tvID = (TextView)view.findViewById(R.id.tvID);
            layItem = (LinearLayout)view.findViewById(R.id.layItem);
        }
    }

    public RVAdapterExpenses(Context context, ArrayList<Expenses> expenses){
        this.context = context;
        this.expenses = expenses;
    }
}
