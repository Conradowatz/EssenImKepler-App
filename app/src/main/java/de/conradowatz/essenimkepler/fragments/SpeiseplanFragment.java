package de.conradowatz.essenimkepler.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.conradowatz.essenimkepler.MyApplication;
import de.conradowatz.essenimkepler.R;
import de.conradowatz.essenimkepler.activities.MainActivity;
import de.conradowatz.essenimkepler.tools.MultiTagAdapter;
import de.conradowatz.essenimkepler.variables.Essen;
import de.conradowatz.essenimkepler.variables.EssenTag;


public class SpeiseplanFragment extends Fragment {

    private View contentView;
    private RecyclerView tagRecycler;


    public SpeiseplanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Anaytics
        MyApplication analytics = (MyApplication) getActivity().getApplication();
        analytics.fireScreenHit("Speiseplan");

        contentView = inflater.inflate(R.layout.fragment_speiseplan, container, false);
        tagRecycler = (RecyclerView) contentView.findViewById(R.id.speisePlan_recyclerView);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.essenAPI == null) return contentView;

        displayInfo(mainActivity.essenAPI.getEssenTagList());

        return contentView;
    }

    public void displayInfo(ArrayList<EssenTag> essenListe) {

        ArrayList<EssenTag> speisePlan = new ArrayList<>();

        //Verzicht aussortieren
        for (EssenTag tag: essenListe) {
            ArrayList<Essen> selectedEssen = new ArrayList<>();
            for (Essen essen : tag.getEssenList()) {
                if (!essen.getDesc().startsWith("Verzicht")) {
                    selectedEssen.add(essen);
                }
            }
            speisePlan.add(new EssenTag(tag.getDatum(), selectedEssen, tag.getSelected()));
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
