package de.conradowatz.essenimkepler.variables;


import java.util.ArrayList;

public class EssenTag {

    private ArrayList<Essen> essenList;
    private int selected;
    private String datum;

    public EssenTag(String datum, ArrayList<Essen> essenList, int selected) {
        this.essenList = essenList;
        this.selected = selected;
        this.datum = datum;
    }

    public EssenTag() {

        essenList = new ArrayList<>();
    }

    public ArrayList<Essen> getEssenList() {
        return essenList;
    }

    public void setEssenList(ArrayList<Essen> essenList) {
        this.essenList = essenList;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }
}
