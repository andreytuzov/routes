package ru.railway.dc.routes.request.model;

/**
 * Created by SQL on 14.01.2017.
 */

public class Station {

    private static int temp = 0;
    // Чтобы один класс отличался от другого
    private int t = ++temp;

    private long id;
    private String name;
    private String region;

    public Station(long id, String name, String region) {
        this.id = id;
        this.name = name;
        this.region = region;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Station other = (Station) obj;
        if (this.t != other.t) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return t;
    }
}
