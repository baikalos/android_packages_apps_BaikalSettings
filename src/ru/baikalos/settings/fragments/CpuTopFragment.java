package ru.baikalos.settings.fragments;

import android.baikalos.ICpuTopService;
import android.baikalos.CpuProcEntry;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.List;

import androidx.annotation.Keep;

@Keep
public class CpuTopFragment extends SettingsPreferenceFragment {

    private ICpuTopService mService;
    private RecyclerView mRecycler;
    private CpuTopAdapter mAdapter;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mRunning;

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            load();
            mHandler.postDelayed(this, 1500);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        IBinder b = ServiceManager.getService("cpu_top");
        mService = ICpuTopService.Stub.asInterface(b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cpu_top_fragment, container, false);

        mRecycler = v.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new CpuTopAdapter();
        mRecycler.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.baikalos_cpu_top_title);

        mRunning = true;
        mUpdater.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunning = false;
        mHandler.removeCallbacks(mUpdater);
    }

    private void load() {
        try {
            List<CpuProcEntry> list = mService.getCpuTop();
            mAdapter.submit(list);
        } catch (RemoteException ignored) {}
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
