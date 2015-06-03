package de.conradowatz.essenimkepler;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;


public class SpeiseplanFragment extends Fragment {

    private View contentView;
    private RecyclerView tagRecycler;


    public SpeiseplanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
                MyApplication.TrackerName.APP_TRACKER);
        // Set screen name.
        t.setScreenName("Speiseplan");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        contentView = inflater.inflate(R.layout.fragment_speiseplan, container, false);
        tagRecycler = (RecyclerView) contentView.findViewById(R.id.speisePlan_recyclerView);

        displayInfo(((MainActivity)getActivity()).essenListe);

        return contentView;
    }

    public void displayInfo(List<EssenTag> essenListe) {

        List<EssenTag> speisePlan = new ArrayList<>();

        //Verzicht aussortieren
        for (EssenTag tag: essenListe) {
            List<Essen> selectedEssen = new ArrayList<>();
            for (Essen essen : tag.essens) {
                if (!essen.desc.startsWith("Verzicht")) {
                    selectedEssen.add(essen);
                }
            }
            speisePlan.add(new EssenTag(tag.datum, selectedEssen, tag.selected));
        }

        //Recycler mit Essen vollstopfen
        tagRecycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        tagRecycler.setLayoutManager(mLayoutManager);
        MultiTagAdapter mAdapter = new MultiTagAdapter(getActivity(), speisePlan);
        tagRecycler.setAdapter(mAdapter);


    }


}
