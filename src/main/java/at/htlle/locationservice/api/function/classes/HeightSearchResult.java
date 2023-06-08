package at.htlle.locationservice.api.function.classes;

import java.util.Optional;

public class HeightSearchResult {
    private Location location;
    Optional<Double> altitude;

    private String direction;

    public HeightSearchResult(Location location, Optional<Double> altitude, String direction) {
        this.altitude = altitude;
        this.direction = direction;
        this.location = location;
    }

    public Optional<Double> getAltitude() {
        return altitude;
    }

    public Location getLocation() {
        return location;
    }

    public String getDirection() {
        return direction;
    }
}
