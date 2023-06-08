package at.htlle.locationservice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import at.htlle.locationservice.api.function.classes.Greeting;
import at.htlle.locationservice.api.function.classes.HeightSearchResult;
import at.htlle.locationservice.api.function.classes.Location;
import at.htlle.locationservice.api.function.classes.SrtmFile;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {

    List<Location> knownLocations = new ArrayList<>();

    @PostConstruct
    void initList() {
        knownLocations.add(new Location("Leoben", 47.383333, 15.1));
        knownLocations.add(new Location("Bruck", 47.416667, 15.266667));
        knownLocations.add(new Location("Kapfenberg", 47.433333, 15.316667));
        knownLocations.add(new Location("Mariazell", 47.769722, 15.316667));
        knownLocations.add(new Location("Graz", 47.066667, 15.45));
        knownLocations.add(new Location("Vienna", 48.2082, 16.3738));
        knownLocations.add(new Location("Linz", 48.3064, 14.2858));
        knownLocations.add(new Location("Graz", 47.0707, 15.4395));
        knownLocations.add(new Location("Salzburg", 47.8095, 13.0550));
        knownLocations.add(new Location("Innsbruck", 47.2682, 11.3923));
        knownLocations.add(new Location("Klagenfurt", 46.6249, 14.3050));
        knownLocations.add(new Location("Villach", 46.6111, 13.8558));
        knownLocations.add(new Location("Wels", 48.1575, 14.0289));
        knownLocations.add(new Location("St. Pölten", 48.2047, 15.6256));
        knownLocations.add(new Location("Dornbirn", 47.4125, 9.7417));
        knownLocations.add(new Location("Wiener Neustadt", 47.8151, 16.2465));
        knownLocations.add(new Location("Bregenz", 47.5031, 9.7471));
        knownLocations.add(new Location("Eisenstadt", 47.8450, 16.5336));
        knownLocations.add(new Location("Leonding", 48.2606, 14.2406));
        knownLocations.add(new Location("Traun", 48.2203, 14.2333));
        knownLocations.add(new Location("Amstetten", 48.1219, 14.8747));
        knownLocations.add(new Location("Klosterneuburg", 48.3053, 16.3256));
        knownLocations.add(new Location("Schwechat", 48.1381, 16.4708));
        knownLocations.add(new Location("Ternitz", 47.7275, 16.0361));
        knownLocations.add(new Location("Baden bei Wien", 48.0069, 16.2308));
    }

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @GetMapping("/locationByName")
    public Location location(@RequestParam(value = "name", defaultValue = "Leoben") String name) {
        for (Location location: knownLocations) {
            if (location.getName().equals(name)) {
                return location;
            }
        }

        return new Location("Unknown Location", 0.0, 0.0);
    }

    @GetMapping("/locationByCoords")
    public Location location(@RequestParam(value="lat", defaultValue = "0") double lat, @RequestParam(value = "lang", defaultValue = "0") double lang) {
        Location nearestLocation = null;
        Location startLocation = new Location("start", lat, lang);
        double minDistance = Double.POSITIVE_INFINITY;
        for (Location location : knownLocations) {
            double distance = location.distanceTo(startLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearestLocation = location;
            }
        }

        return nearestLocation;
    }

    @GetMapping("/heightByCoords")
    public HeightSearchResult heightByCoords(@RequestParam(value="lat", defaultValue = "0") double lat, @RequestParam(value = "lang", defaultValue = "0") double lang) {

        Location nearestLocation = location(lat, lang);
        Location startLocation = new Location("start", lat, lang);

        //find direction
        if (nearestLocation == null) {
            nearestLocation = new Location("the plateau of Leng", 0.00, 0.00);
        }
        Double azimuth = nearestLocation.directionTo(startLocation);
        String direction = "";
        if (azimuth >= 337.5 || azimuth < 22.5) {
            direction = "Nördlich";
        } else if (azimuth >= 22.5 && azimuth < 67.5) {
            direction = "Nordöstlich";
        } else if (azimuth >= 67.5 && azimuth < 112.5) {
            direction = "Östlich";
        } else if (azimuth >= 112.5 && azimuth < 157.5) {
            direction = "Südöstlich";
        } else if (azimuth >= 157.5 && azimuth < 202.5) {
            direction = "Südlich";
        } else if (azimuth >= 202.5 && azimuth < 247.5) {
            direction = "Südwestlich";
        } else if (azimuth >= 247.5 && azimuth < 292.5) {
            direction = "Westlich";
        } else if (azimuth >= 292.5 && azimuth < 337.5) {
            direction = "Nordwestlich";
        } else {
            direction = "Unbekannte Richtung";
        }

        Double distance = startLocation.distanceTo(nearestLocation) * 100;

        startLocation.setName(String.format("%.2f", distance) + "km " + direction + " von " + nearestLocation.getName());

        SrtmFile file = new SrtmFile(new File("src/main/resources/srtm_40_03.asc"));
        Optional<Double> altitude = file.getAltitudeForLocation(startLocation);


        return new HeightSearchResult(startLocation, altitude, direction);
    }

    @GetMapping("/getAltitudeProfile")
    public List<Double> getAltitudeProfile(
            @RequestParam(value="lat1") double lat1,
            @RequestParam(value="lang1") double lang1,
            @RequestParam(value="lat2") double lat2,
            @RequestParam(value="lang2") double lang2,
            @RequestParam(value="points", defaultValue = "10") int inbetweenPoints) {
        Location firstLocation = new Location("Start", lat1, lang2);
        Location secondLocation = new Location("End", lat2, lang2);

        List<Location> intermediateLocations = firstLocation.calculateIntermediateLocations(secondLocation, inbetweenPoints);
        List<Double> elevations = new ArrayList<>();
        for (Location location: intermediateLocations) {
            File file = new File("src/main/resources/srtm_40_03.asc");
            SrtmFile srtmFile = new SrtmFile(file);
            Optional<Double> altitude = srtmFile.getAltitudeForLocation(location);
            elevations.add(altitude.get());
        }

        return elevations;
    }

    @GetMapping("/knownLocations")
    public List<Location> knownLocations() {
        return knownLocations;
    }
}
