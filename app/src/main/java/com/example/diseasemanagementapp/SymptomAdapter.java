package com.example.diseasemanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    private List<Symptom> symptomList;

    public SymptomAdapter(List<Symptom> symptomList) {
        this.symptomList = symptomList;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        Symptom symptom = symptomList.get(position);
        holder.tvSymptomName.setText(symptom.getName());
        holder.tvIntensity.setText(String.valueOf(symptom.getIntensity()));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        holder.tvSymptomDate.setText(sdf.format(new Date(symptom.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return symptomList.size();
    }

    static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymptomName, tvSymptomDate, tvIntensity;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymptomName = itemView.findViewById(R.id.tv_symptom_name_item);
            tvSymptomDate = itemView.findViewById(R.id.tv_symptom_date_item);
            tvIntensity = itemView.findViewById(R.id.tv_intensity_item);
        }
    }
}