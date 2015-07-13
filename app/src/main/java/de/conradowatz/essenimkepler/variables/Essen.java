package de.conradowatz.essenimkepler.variables;


public class Essen {

    private String desc;
    private String price;

    public Essen(String price, String desc) {
        this.price = price;
        this.desc = desc;
    }

    public Essen() {

    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
