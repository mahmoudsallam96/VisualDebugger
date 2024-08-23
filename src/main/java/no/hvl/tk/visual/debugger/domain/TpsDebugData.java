package no.hvl.tk.visual.debugger.domain;

import java.util.Map;

public class TpsDebugData {
    private Map<String, String> routeImageById;

    public TpsDebugData() {

    }

    public TpsDebugData(Map<String, String> routeImageById) {
        this.routeImageById = routeImageById;
    }


    public Map<String, String> getRouteImageById() {
        return routeImageById;
    }

    public void setRouteImageById(Map<String, String> routeImageById) {
        this.routeImageById = routeImageById;
    }

    @Override
    public String toString() {
        return "RouteImageById{" +
                "routeImageById=" + routeImageById +
                '}';
    }
}
