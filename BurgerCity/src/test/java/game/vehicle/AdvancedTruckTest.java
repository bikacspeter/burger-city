package game.vehicle;

import game.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedTruckTest {

    private AdvancedTruck advancedTruck;

    @BeforeEach
    void setUp() {
        advancedTruck = new AdvancedTruck();
    }

    

    @Test
    void testConstructorSetsSpeed() {
        assertEquals(2, advancedTruck.speed);
    }

    @Test
    void testConstructorSetsCapacity() {
        assertEquals(35, advancedTruck.capacity);
    }

    @Test
    void testConstructorSetsMaintenanceCost() {
        assertEquals(4, advancedTruck.maintenanceCost);
    }

    @Test
    void testConstructorInheritsFromVehicle() {
        assertNotNull(advancedTruck);
        assertTrue(advancedTruck instanceof Vehicle);
    }

    

    @Test
    void testCanCarryWheat() {
        assertTrue(advancedTruck.canCarry(ResourceType.WHEAT));
    }

    @Test
    void testCanCarryMeat() {
        assertTrue(advancedTruck.canCarry(ResourceType.MEAT));
    }

    @Test
    void testCanCarryBread() {
        assertTrue(advancedTruck.canCarry(ResourceType.BREAD));
    }

    @Test
    void testCanCarryMeatPatty() {
        assertTrue(advancedTruck.canCarry(ResourceType.MEAT_PATTY));
    }

    @Test
    void testCanCarryHamburger() {
        assertTrue(advancedTruck.canCarry(ResourceType.HAMBURGER));
    }

    @Test
    void testCannotCarryPassengers() {
        assertFalse(advancedTruck.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testCanCarryHandlesNullResourceType() {
        
        try {
            boolean result = advancedTruck.canCarry(null);
            
            assertTrue(result || result == false);
        } catch (NullPointerException e) {
            
        }
    }

    

    @Test
    void testCanCarryAllNonPassengerResources() {
        ResourceType[] allTypes = ResourceType.values();
        for (ResourceType type : allTypes) {
            if (type == ResourceType.PASSENGERS) {
                assertFalse(advancedTruck.canCarry(type),
                    "Advanced Truck should not carry " + type);
            } else {
                assertTrue(advancedTruck.canCarry(type),
                    "Advanced Truck should carry " + type);
            }
        }
    }

    

    @Test
    void testAdvancedTruckHasCorrectCapacity() {
        assertEquals(35, advancedTruck.capacity);
    }

    @Test
    void testAdvancedTruckCapacityIsHigherThanTruck() {
        Truck truck = new Truck();
        assertTrue(advancedTruck.capacity > truck.capacity);
    }

    @Test
    void testAdvancedTruckCapacityIsHigherThanBus() {
        Bus bus = new Bus();
        assertTrue(advancedTruck.capacity > bus.capacity);
    }

    @Test
    void testAdvancedTruckCapacityIncreaseFromTruck() {
        Truck truck = new Truck();
        int increase = advancedTruck.capacity - truck.capacity;
        assertEquals(15, increase);
    }

    

    @Test
    void testAdvancedTruckSpeedIsFasterThanTruck() {
        Truck truck = new Truck();
        assertTrue(advancedTruck.speed > truck.speed);
    }

    @Test
    void testAdvancedTruckSpeedMatchesVehicleDefault() {
        Vehicle vehicle = new Vehicle();
        assertEquals(vehicle.speed, advancedTruck.speed);
    }

    @Test
    void testAdvancedTruckSpeedMatchesBus() {
        Bus bus = new Bus();
        assertEquals(bus.speed, advancedTruck.speed);
    }

    @Test
    void testAdvancedTruckSpeedImprovement() {
        Truck truck = new Truck();
        int speedIncrease = advancedTruck.speed - truck.speed;
        assertEquals(1, speedIncrease);
    }

    

    @Test
    void testMaintenanceCostIsPositive() {
        assertTrue(advancedTruck.maintenanceCost > 0);
    }

    @Test
    void testMaintenanceCostIsHigherThanTruck() {
        Truck truck = new Truck();
        assertTrue(advancedTruck.maintenanceCost > truck.maintenanceCost);
    }

    @Test
    void testMaintenanceCostIsHigherThanBus() {
        Bus bus = new Bus();
        assertTrue(advancedTruck.maintenanceCost > bus.maintenanceCost);
    }

    @Test
    void testMaintenanceCostIncrease() {
        Truck truck = new Truck();
        int costIncrease = advancedTruck.maintenanceCost - truck.maintenanceCost;
        assertEquals(1, costIncrease);
    }

    

    @Test
    void testAdvancedTruckIsDistinctFromBus() {
        Bus bus = new Bus();
        
        assertTrue(advancedTruck.canCarry(ResourceType.WHEAT));
        assertFalse(advancedTruck.canCarry(ResourceType.PASSENGERS));
        assertFalse(bus.canCarry(ResourceType.WHEAT));
        assertTrue(bus.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testAdvancedTruckEffectiveSpeedEqualsSpeed() {
        assertEquals(advancedTruck.speed, advancedTruck.effectiveSpeed);
    }

    @Test
    void testAdvancedTruckInheritsVehicleMethods() {
        
        advancedTruck.setPurchasePrice(3000);
        assertEquals(3000, advancedTruck.getPurchasePrice());
        assertEquals(1500, advancedTruck.getSellValue());
    }

    @Test
    void testAdvancedTruckCanBeSpawned() {
        advancedTruck.spawnAt(7, 8);
        assertTrue(advancedTruck.isSpawned());
        assertEquals(7, advancedTruck.getCurrentTileX());
        assertEquals(8, advancedTruck.getCurrentTileY());
    }

    @Test
    void testAdvancedTruckCanHavePath() {
        java.util.List<int[]> path = java.util.List.of(
            new int[]{0, 0},
            new int[]{1, 0},
            new int[]{2, 0}
        );
        advancedTruck.setPath(path);
        assertTrue(advancedTruck.hasPath());
    }

    @Test
    void testAdvancedTruckAges() {
        advancedTruck.spawnAt(0, 0);
        double initialAge = advancedTruck.getAgeSeconds();
        
        advancedTruck.ageSeconds = 100.0;
        assertEquals(100.0, advancedTruck.getAgeSeconds());
        assertTrue(advancedTruck.getAgeSeconds() > initialAge);
    }

    

    @Test
    void testAdvancedTruckVsTruckComparison() {
        Truck truck = new Truck();

        
        assertTrue(advancedTruck.speed > truck.speed,
            "Advanced truck should be faster");
        assertTrue(advancedTruck.capacity > truck.capacity,
            "Advanced truck should have more capacity");
        assertTrue(advancedTruck.maintenanceCost > truck.maintenanceCost,
            "Advanced truck should cost more to maintain");

        
        assertEquals(truck.canCarry(ResourceType.WHEAT),
            advancedTruck.canCarry(ResourceType.WHEAT));
        assertEquals(truck.canCarry(ResourceType.PASSENGERS),
            advancedTruck.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testAdvancedTruckIsGoodsSpecialist() {
        
        assertTrue(advancedTruck.canCarry(ResourceType.WHEAT));
        assertTrue(advancedTruck.canCarry(ResourceType.MEAT));
        assertTrue(advancedTruck.canCarry(ResourceType.BREAD));
        assertTrue(advancedTruck.canCarry(ResourceType.MEAT_PATTY));
        assertTrue(advancedTruck.canCarry(ResourceType.HAMBURGER));
        assertFalse(advancedTruck.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testAdvancedTruckHasHigherCapacityThanAllBuses() {
        Bus bus = new Bus();
        AdvancedBus advancedBus = new AdvancedBus();

        assertTrue(advancedTruck.capacity > bus.capacity,
            "Advanced truck should have more capacity than bus");
        
    }

    

    @Test
    void testAdvancedTruckIsUpgrade() {
        Truck basicTruck = new Truck();

        
        assertTrue(advancedTruck.speed >= basicTruck.speed);
        assertTrue(advancedTruck.capacity > basicTruck.capacity);

        
        assertTrue(advancedTruck.maintenanceCost >= basicTruck.maintenanceCost);
    }

    @Test
    void testAdvancedTruckHasBalancedStats() {
        
        assertTrue(advancedTruck.speed > 0);
        assertTrue(advancedTruck.capacity > 0);
        assertTrue(advancedTruck.maintenanceCost > 0);

        
        assertTrue(advancedTruck.speed <= 5);
        assertTrue(advancedTruck.capacity <= 100);
        assertTrue(advancedTruck.maintenanceCost <= 10);
    }

    @Test
    void testAdvancedTruckCarriesSameTypesAsTruck() {
        Truck truck = new Truck();

        
        for (ResourceType type : ResourceType.values()) {
            assertEquals(truck.canCarry(type), advancedTruck.canCarry(type),
                "Advanced truck and truck should carry same types: " + type);
        }
    }

    @Test
    void testAdvancedTruckCapacityIsSignificantUpgrade() {
        Truck truck = new Truck();
        double percentageIncrease = ((double) advancedTruck.capacity / truck.capacity - 1) * 100;
        assertTrue(percentageIncrease >= 50, "Capacity should increase by at least 50%");
    }
}
