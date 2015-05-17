package de.conradowatz.essenimkepler;


import java.util.ArrayList;
import java.util.List;

public class EssenTag {

    public List<Essen> essens;
    public int selected;
    public String datum;

    public EssenTag(String datum, List<Essen> essens, int selected) {
        this.essens = essens;
        this.selected = selected;
        this.datum = datum;
    }

    public EssenTag() {

        essens = new ArrayList<Essen>();
    }

}
