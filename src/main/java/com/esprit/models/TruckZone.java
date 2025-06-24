package com.esprit.models;

public enum TruckZone {
    TUNIS_NORTH("Tunis Nord", "Zone nord de Tunis: La Marsa, Carthage, Sidi Bou Said",
            new double[]{36.8982, 10.3177}),  // Centered around La Marsa

    TUNIS_CENTRAL("Tunis Centre", "Centre-ville de Tunis et zones environnantes",
            new double[]{36.8065, 10.1815}),  // Centered around Avenue Habib Bourguiba

    TUNIS_SOUTH("Tunis Sud", "Zone sud de Tunis: Ben Arous, Rades, Hammam Lif",
            new double[]{36.7454, 10.2245}),  // Centered around Ben Arous

    ARIANA("Ariana", "Gouvernorat de l'Ariana et environs",
            new double[]{36.8625, 10.1927}),  // Centered around Ariana

    MANOUBA("Manouba", "Zone de la Manouba et régions voisines",
            new double[]{36.8398, 10.0986}),  // Centered around Manouba

    COASTAL_NORTH("Zone Côtière Nord", "Gammarth, La Marsa, Sidi Bou Said",
            new double[]{36.8824, 10.3238}),  // Centered around Gammarth

    COASTAL_SOUTH("Zone Côtière Sud", "Rades, Ezzahra, Hammam Lif",
            new double[]{36.7367, 10.3054}),  // Centered around Rades

    INDUSTRIAL_ZONE("Zone Industrielle", "Zones industrielles de Ben Arous et Mghira",
            new double[]{36.7652, 10.2095}),  // Centered around Ben Arous industrial area

    MEDINA("Médina", "Médina de Tunis et zones historiques",
            new double[]{36.7973, 10.1706}),  // Centered around Medina

    SUBURBAN("Périphérie", "Zones périphériques et nouvelles extensions",
            new double[]{36.8489, 10.2041}),  // General suburban area

    UNKNOWN("Zone Inconnue", "Zone non définie",
            new double[]{36.8065, 10.1815});  // Default to Tunis center

    private final String displayName;
    private final String description;
    private final double[] centerCoordinates; // [latitude, longitude]
    private static final double MAX_ZONE_RADIUS = 5.0; // Maximum radius in kilometers

    TruckZone(String displayName, String description, double[] centerCoordinates) {
        this.displayName = displayName;
        this.description = description;
        this.centerCoordinates = centerCoordinates;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double[] getCenterCoordinates() {
        return centerCoordinates;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public boolean isLocationInZone(double latitude, double longitude) {
        return calculateDistance(latitude, longitude,
                this.centerCoordinates[0], this.centerCoordinates[1]) <= MAX_ZONE_RADIUS;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static TruckZone getBestZoneForLocation(double latitude, double longitude) {
        TruckZone bestZone = UNKNOWN;
        double shortestDistance = Double.MAX_VALUE;

        for (TruckZone zone : values()) {
            if (zone == UNKNOWN) continue;

            double distance = zone.calculateDistance(latitude, longitude,
                    zone.centerCoordinates[0], zone.centerCoordinates[1]);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                bestZone = zone;
            }
        }

        return bestZone;
    }

    public static TruckZone fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return UNKNOWN;
        }

        for (TruckZone zone : values()) {
            if (zone.getDisplayName().equals(displayName)) {
                return zone;
            }
        }
        System.out.println("Warning: Unknown zone '" + displayName + "', defaulting to UNKNOWN");
        return UNKNOWN;
    }
}