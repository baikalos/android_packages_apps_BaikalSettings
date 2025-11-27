package ru.baikalos.settings.fragments;

import android.baikalos.CpuProcEntry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;

@Keep
public class CpuTopAdapter extends RecyclerView.Adapter<CpuTopAdapter.Holder> {

    private final List<CpuProcEntry> mData = new ArrayList<>();

    public void submit(List<CpuProcEntry> list) {
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cpu_top_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder h, int pos) {
        CpuProcEntry e = mData.get(pos);
        boolean kernel = e.uid < 0;
        h.name.setText(kernel ? "[kernel] " + e.name : e.name);
        h.pid.setText("PID " + e.pid);
        h.cpu.setText("CPU " + e.cpuTime);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name;
        TextView pid;
        TextView cpu;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            pid  = v.findViewById(R.id.pid);
            cpu  = v.findViewById(R.id.cpu);
        }
    }
}
