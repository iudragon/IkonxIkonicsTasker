package dragon.bakuman.iu.ikonxikonicstasker.Fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dragon.bakuman.iu.ikonxikonicstasker.Activities.PaymentActivity;
import dragon.bakuman.iu.ikonxikonicstasker.Adapters.TrayAdapter;
import dragon.bakuman.iu.ikonxikonicstasker.AppDatabase;
import dragon.bakuman.iu.ikonxikonicstasker.Objects.Tray;
import dragon.bakuman.iu.ikonxikonicstasker.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrayFragment extends Fragment {

    private AppDatabase db;
    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;

    public TrayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tray, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = AppDatabase.getAppDatabase(getContext());
        listTray();

        trayList = new ArrayList<>();
        adapter = new TrayAdapter(this.getActivity(), trayList);

        ListView listView = getActivity().findViewById(R.id.tray_list);
        listView.setAdapter(adapter);

        Button buttonAddPayment = getActivity().findViewById(R.id.button_add_payment);
        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PaymentActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void listTray() {

        new AsyncTask<Void, Void, List<Tray>>() {

            @Override
            protected List<Tray> doInBackground(Void... voids) {
                return db.trayDao().getAll();
            }

            @Override
            protected void onPostExecute(List<Tray> trays) {
                super.onPostExecute(trays);
                if (!trays.isEmpty()) {
                    trayList.clear();
                    trayList.addAll(trays);
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }
}
