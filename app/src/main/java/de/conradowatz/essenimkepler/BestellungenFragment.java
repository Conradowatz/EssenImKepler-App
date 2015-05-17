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
    private boolean hasInfo = false;


    public BestellungenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("LOGY", "Bestellungen onCreate");

        contentView = inflater.inflate(R.layout.fragment_bestellungen, container, false);
        tagRecycler = (RecyclerView) contentView.findViewById(R.id.bestellungen_recyclerView);

        //Recyclerview vorbereiten
        tagRecycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        tagRecycler.setLayoutManager(mLayoutManager);
        MultiTagAdapter mAdapter = new MultiTagAdapter(getActivity(), new ArrayList<EssenTag>());
        tagRecycler.setAdapter(mAdapter);

        if ((savedInstanceState!=null)||(hasInfo)) {
            displayInfo(((MainActivity)getActivity()).html);
        }

        return contentView;
    }

    public void displayInfo(String html) {

        hasInfo = true;

        Log.d("LOGY", "Bestellungen display");

        List<EssenTag> essenListe = EssenAPI.parseHTML(html);
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
