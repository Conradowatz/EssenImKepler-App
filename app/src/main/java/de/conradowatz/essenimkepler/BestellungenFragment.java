package de.conradowatz.essenimkepler;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class BestellungenFragment extends Fragment {

    private View contentView;
    private RecyclerView tagRecycler;


    public BestellungenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_bestellungen, container, false);
        tagRecycler = (RecyclerView) contentView.findViewById(R.id.bestellungen_recyclerView);

        //Recyclerview vorbereiten
        tagRecycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        tagRecycler.setLayoutManager(mLayoutManager);
        MultiTagAdapter mAdapter = new MultiTagAdapter(getActivity(), new ArrayList<EssenTag>());
        tagRecycler.setAdapter(mAdapter);

        displayInfo(((MainActivity)getActivity()).essenListe);

        return contentView;
    }

    public void displayInfo(List<EssenTag> essenListe) {

        List<EssenTag> bestellungsListe = new ArrayList<>();

        //nur selektierte Essen aussortieren
        for (EssenTag tag: essenListe) {
            List<Essen> selectedEssen = new ArrayList<>();
            selectedEssen.add(tag.essens.get(tag.selected));
            bestellungsListe.add(new EssenTag(tag.datum, selectedEssen, tag.selected));
        }

        //Recycler mit Essen vollstopfen
        MultiTagAdapter mAdapter = new MultiTagAdapter(getActivity(), bestellungsListe);
        tagRecycler.setAdapter(mAdapter);


    }


}
