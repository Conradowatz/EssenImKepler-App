package de.conradowatz.essenimkepler;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SingleTagAdapter extends RecyclerView.Adapter<SingleTagAdapter.ViewHolder> {

    private List<EssenTag> essenListe;
    private List<Integer> realEssens = new ArrayList<>();

    private static int VIEWTYPE_CARD = 0;
    private static int VIEWTYPE_DIVIDER = 1;

    public SingleTagAdapter(List<EssenTag> essenListe) {

        //Wochendivider berechnen
        List<EssenTag> tmpEssenListe = new ArrayList<>();
        for (int i = 0; i<essenListe.size(); i++) {
            EssenTag thisTag = essenListe.get(i);

            String date = thisTag.datum.split(",")[1];
            String datePrev = date;
            if (i>0) {
                datePrev = essenListe.get(i - 1).datum.split(",")[1];
            }
            Date nowDate = new SimpleDateFormat("dd.MM.yyyy").parse(date, new ParsePosition(0));
            Date prevDate = new SimpleDateFormat("dd.MM.yyyy").parse(datePrev, new ParsePosition(0));
            Calendar nowCalendar = Calendar.getInstance();
            Calendar prevCalendar = Calendar.getInstance();
            nowCalendar.setTime(nowDate);
            prevCalendar.setTime(prevDate);
            if (nowCalendar.get(Calendar.WEEK_OF_YEAR)>prevCalendar.get(Calendar.WEEK_OF_YEAR)) {
                tmpEssenListe.add(new EssenTag("DIVIDER", null, -1));
                realEssens.add(-1);
            }
            tmpEssenListe.add(thisTag);
            realEssens.add(i);
        }

        this.essenListe = tmpEssenListe;
    }


    @Override
    public SingleTagAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==VIEWTYPE_DIVIDER) {
            View d = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_divider, parent, false);
            return new ViewHolder(d);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_card_single_layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SingleTagAdapter.ViewHolder holder, int position) {
        EssenTag essenTag = essenListe.get(position);
        if (essenTag.essens==null) {
            return;
        }
        Essen essen = essenTag.essens.get(0);

        //jede zweite Karte dunkler machen
        /*if (realEssens.get(position)%2==1) {
            holder.layout.setBackgroundColor(holder.layout.getContext().getResources().getColor(R.color.yellowish));
        } else {
            holder.layout.setBackgroundColor(holder.layout.getContext().getResources().getColor(R.color.turquoisish));
        }*/

        //Datum setzen
        holder.dateText.setText(essenTag.datum);

        //Wenn der heutige Tag -> highlighten
        String date = essenTag.datum.split(",")[1];
        Date stringDate = new SimpleDateFormat("dd.MM.yyyy").parse(date, new ParsePosition(0));
        Calendar currentCalendar = Calendar.getInstance();
        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(stringDate);
        if (currentCalendar.get(Calendar.DAY_OF_YEAR)==dayCalendar.get(Calendar.DAY_OF_YEAR)) {
            holder.highlight.setVisibility(View.VISIBLE);
            holder.specailDayText.setText("HEUTE");
            holder.specailDayText.setVisibility(View.VISIBLE);

        } else {
            holder.specailDayText.setVisibility(View.INVISIBLE);
        }
        if (currentCalendar.get(Calendar.DAY_OF_YEAR)+1==dayCalendar.get(Calendar.DAY_OF_YEAR)) {
            holder.specailDayText.setText("MORGEN");
            holder.specailDayText.setVisibility(View.VISIBLE);
        } else {
            holder.specailDayText.setVisibility(View.INVISIBLE);
        }

        //beschreibung setzen, Klammern entfernen, Preis bei Verzicht verstecken
        String desc = essen.desc.replaceAll(" \\((.*?)\\)", "");
        if (desc.startsWith("Verzicht")) {
            holder.descText.setText("Kein Essen");
            holder.priceText.setText("");
        } else {
            holder.descText.setText(desc);
            holder.priceText.setText(essen.price + " €");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (essenListe.get(position).datum.equals("DIVIDER")) {
            return VIEWTYPE_DIVIDER;
        }
        return VIEWTYPE_CARD;
    }

    @Override
    public int getItemCount() {
        return essenListe.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText;
        public TextView descText;
        public TextView priceText;
        public View highlight;
        public RelativeLayout layout;
        public TextView specailDayText;

        public ViewHolder(View v) {
            super(v);
            highlight =  v.findViewById(R.id.tag_single_highlight);
            layout = (RelativeLayout) v.findViewById(R.id.tag_single_layout);
            dateText = (TextView) v.findViewById(R.id.tag_single_date_textView);
            descText = (TextView) v.findViewById(R.id.tag_single_desc_textView);
            specailDayText = (TextView) v.findViewById(R.id.tag_single_specialDay_textView);
            priceText = (TextView) v.findViewById(R.id.tag_single_price_textView);
        }
    }

}
