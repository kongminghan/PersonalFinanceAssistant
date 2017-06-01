package com.example.minghan.expense;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by MingHan on 6/1/2017.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Category> cats;

    @Override
    public CategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryAdapter.MyViewHolder holder, final int position) {
        holder.tvID.setText(cats.get(position).getId());
        holder.cardView.setCardBackgroundColor(getMatColor("500"));
        holder.tvCategory.setText(cats.get(position).getCat_name());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogEt = new Dialog(context);
                dialogEt.setContentView(R.layout.modal_edit_category);

                Button btnUpdate = (Button) dialogEt.findViewById(R.id.btnUpdate);
                Button btnDelete = (Button) dialogEt.findViewById(R.id.btnDelete);
                final EditText etCat = (EditText) dialogEt.findViewById(R.id.etCat);

                etCat.setText(cats.get(position).getCat_name());

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialogEt.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;
                dialogEt.getWindow().setAttributes(lp);

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!etCat.getText().toString().trim().equals("")){
                            ExpensesDB db = new ExpensesDB(context);
                            Category category = new Category(etCat.getText().toString(), holder.tvID.getText().toString());
                            db.updateCat(category);
                            db.close();
                            cats.remove(position);
                            cats.add(category);
                            notifyDataSetChanged();
                            dialogEt.dismiss();
                        }else{
                            Toast.makeText(context, "Please provide a valid category", Toast.LENGTH_SHORT).show();
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
                                ExpensesDB db = new ExpensesDB(context);
                                Category category = new Category(etCat.getText().toString(), holder.tvID.getText().toString());
                                db.deleteCat(category);
                                db.close();
                                cats.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                                dialog.dismiss();
                                dialogEt.dismiss();
                            }
                        });

                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialogEt.dismiss();
                            }
                        });
                        alert.show();
                    }
                });
                dialogEt.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cats.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView tvCategory, tvID;
        public CardView cardView;
        public MyViewHolder(View view){
            super(view);
            cardView = (CardView) view.findViewById(R.id.cardView);
            tvCategory = (TextView)view.findViewById(R.id.tvCategory);
            tvID = (TextView)view.findViewById(R.id.tvID);
        }
    }

    public CategoryAdapter(Context context, ArrayList<Category> cats){
        this.context = context;
        this.cats = cats;
    }

    private int getMatColor(String typeColor)
    {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0)
        {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }
}
