package game.map;

import game.resource.ResourceInventory;
import game.resource.ResourceType;

import java.util.Map;
import java.util.Random;

public class Industry {

    private String name;
    private IndustryType industryType;
    private int originX;
    private int originY;
    private int width;
    private int height;

    private final IndustryProfile profile;
    private final ResourceInventory storage;

    
    private double productivity;
    private double unitRemainder;
    private final Random rng;

    
    public Industry(String name, IndustryType industryType, int originX, int originY, int width, int height) {
        this.name = name;
        this.industryType = industryType;
        this.originX = originX;
        this.originY = originY;
        this.width = Math.max(width, 2);
        this.height = Math.max(height, 2);

        this.profile = profileFor(industryType);
        this.storage = new ResourceInventory();
        this.productivity = 1.0;
        this.unitRemainder = 0.0;
        this.rng = new Random((originX * 73856093L) ^ (originY * 19349663L) ^ (long) name.hashCode());
    }

    public String getName() { return name; }
    public IndustryType getIndustryType() { return industryType; }
    public int getOriginX() { return originX; }
    public int getOriginY() { return originY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public IndustryProfile getProfile() { return profile; }
    public ResourceInventory getStorage() { return storage; }
    public double getProductivity() { return productivity; }

    public boolean consumes(ResourceType type) {
        if (type == null) return false;
        return profile.getInputsPerUnit().containsKey(type);
    }

    





    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0) return;
        if (profile.getBaseUnitsPerSecond() <= 0) {
            updateProductivity(deltaSeconds);
            return;
        }

        updateProductivity(deltaSeconds);

        unitRemainder += profile.getBaseUnitsPerSecond() * productivity * deltaSeconds;
        int requestedUnits = (int) Math.floor(unitRemainder);
        if (requestedUnits <= 0) return;

        int maxUnitsByInputs = maxUnitsByInputs(profile.getInputsPerUnit(), storage);
        int units = Math.min(requestedUnits, maxUnitsByInputs);
        if (units <= 0) return;

        
        for (var e : profile.getInputsPerUnit().entrySet()) {
            int need = e.getValue() * units;
            storage.removeUpTo(e.getKey(), need);
        }

        
        for (var e : profile.getOutputsPerUnit().entrySet()) {
            int out = e.getValue() * units;
            storage.add(e.getKey(), out);
        }

        unitRemainder -= units;
    }

    


    public int takeFromStorage(ResourceType type, int maxAmount) {
        return storage.removeUpTo(type, maxAmount);
    }

    


    public void deliverToStorage(ResourceType type, int amount) {
        storage.add(type, amount);
    }

    public boolean occupies(int x, int y) {
        return x >= originX && x < originX + width
            && y >= originY && y < originY + height;
    }

    private void updateProductivity(double deltaSeconds) {
        
        double pull = (1.0 - productivity) * (0.03 * deltaSeconds);
        double noise = (rng.nextDouble() * 2.0 - 1.0) * (0.02 * deltaSeconds);
        productivity = clamp(productivity + pull + noise, 0.4, 1.6);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int maxUnitsByInputs(Map<ResourceType, Integer> inputsPerUnit, ResourceInventory inv) {
        if (inputsPerUnit.isEmpty()) return Integer.MAX_VALUE;
        int max = Integer.MAX_VALUE;
        for (var e : inputsPerUnit.entrySet()) {
            int need = e.getValue();
            if (need <= 0) continue;
            int have = inv.get(e.getKey());
            max = Math.min(max, have / need);
            if (max == 0) return 0;
        }
        return max;
    }

    private static IndustryProfile profileFor(IndustryType type) {
        if (type == null) type = IndustryType.FACTORY;

        
        return switch (type) {
            case FARM -> new IndustryProfile(
                    Map.of(),
                    Map.of(ResourceType.WHEAT, 1),
                    0.35
            );
            case RANCH -> new IndustryProfile(
                    Map.of(),
                    Map.of(ResourceType.MEAT, 1),
                    0.25
            );
            case BAKERY -> new IndustryProfile(
                    Map.of(ResourceType.WHEAT, 2),
                    Map.of(ResourceType.BREAD, 1),
                    0.22
            );
            case PATTY_PLANT -> new IndustryProfile(
                    Map.of(ResourceType.MEAT, 1),
                    Map.of(ResourceType.MEAT_PATTY, 1),
                    0.25
            );
            case BURGER_FACTORY -> new IndustryProfile(
                    Map.of(ResourceType.BREAD, 1, ResourceType.MEAT_PATTY, 1),
                    Map.of(ResourceType.HAMBURGER, 1),
                    0.18
            );
            case FACTORY -> new IndustryProfile(Map.of(), Map.of(), 0.0);
        };
    }
}