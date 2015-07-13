package de.conradowatz.essenimkepler.tools;


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.conradowatz.essenimkepler.R;
import de.conradowatz.essenimkepler.variables.Essen;
import de.conradowatz.essenimkepler.variables.EssenTag;

public class MultiTagAdapter extends RecyclerView.Adapter<MultiTagAdapter.ViewHolder> {

    private static int VIEWTYPE_CARD = 0;
    private static int VIEWTYPE_DIVIDER = 1;
    private ArrayList<EssenTag> essenListe;
    private ArrayList<Integer> realEssens = new ArrayList<>();
    private Context context;

    public MultiTagAdapter(Context context, ArrayList<EssenTag> essenListe) {

        this.context = context;

        //Wochendivider berechnen
        ArrayList<EssenTag> tmpEssenListe = new ArrayList<>();
        for (int i = 0; i<essenListe.size(); i++) {
            EssenTag thisTag = essenListe.get(i);

            String date = thisTag.getDatum().split(",")[1];
            String datePrev = date;
            if (i>0) {
                datePrev = essenListe.get(i - 1).getDatum().split(",")[1];
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
    public MultiTagAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==VIEWTYPE_DIVIDER) {
            View d = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_divider, parent, false);
            return new ViewHolder(d);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_card_multi_layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        EssenTag essenTag = essenListe.get(position);
        if (essenTag.getEssenList() == null) {
            return;
        }

        //Datum setzen
        holder.dateText.setText(essenTag.getDatum());

        //Wenn der heutige Tag -> highlighten, anzeigen
        String date = essenTag.getDatum().split(",")[1];
        Date stringDate = new SimpleDateFormat("dd.MM.yyyy").parse(date, new ParsePosition(0));
        Calendar currentCalendar = Calendar.getInstance();
        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(stringDate);
        if (currentCalendar.get(Calendar.DAY_OF_YEAR)==dayCalendar.get(Calendar.DAY_OF_YEAR)) {
            holder.highlight.setVisibility(View.VISIBLE);
            holder.specailDayText.setText("HEUTE");
            holder.specailDayText.setVisibility(View.VISIBLE);

        } else if (currentCalendar.get(Calendar.DAY_OF_YEAR)+1==dayCalendar.get(Calendar.DAY_OF_YEAR)) {
            holder.specailDayText.setText("MORGEN");
            holder.specailDayText.setVisibility(View.VISIBLE);
        } else {
            holder.specailDayText.setVisibility(View.INVISIBLE);
            holder.highlight.setVisibility(View.INVISIBLE);
        }

        addMealstoLayout(essenTag.getEssenList(), holder.mealLayout);
    }

    @Override
    public int getItemViewType(int position) {
        if (essenListe.get(position).getDatum().equals("DIVIDER")) {
            return VIEWTYPE_DIVIDER;
        }
        return VIEWTYPE_CARD;
    }

    @Override
    public int getItemCount() {
        return essenListe.size();
    }

    private void addMealstoLayout(ArrayList<Essen> essens, LinearLayout layout) {

        layout.removeAllViews();

        for (Essen essen : essens) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View v = inflater.inflate(R.layout.meal_row_layout, null);

            TextView descText = (TextView) v.findViewById(R.id.meal_row_desc_textView);
            TextView priceText = (TextView) v.findViewById(R.id.meal_row_price_textView);

            String desc = essen.getDesc().replaceAll(" \\((.*?)\\)", "");
            if (desc.startsWith("Verzicht")) {
                descText.setText("Kein Essen");
                priceText.setText("");
            } else {
                descText.setText(desc);
                priceText.setText(essen.getPrice() + " €");
            }

            layout.addView(v);

        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText;
        public LinearLayout mealLayout;
        public View highlight;
        public RelativeLayout layout;
        public TextView specailDayText;

        public ViewHolder(View v) {
            super(v);
            highlight = v.findViewById(R.id.tag_multi_highlight);
            layout = (RelativeLayout) v.findViewById(R.id.tag_multi_layout);
            dateText = (TextView) v.findViewById(R.id.tag_multi_date_textView);
            specailDayText = (TextView) v.findViewById(R.id.tag_multi_specialDay_textView);
            mealLayout = (LinearLayout) v.findViewById(R.id.tag_multi_meal_linearLayout);
        }
    }
}
