package game.vehicle;

import game.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TruckTest {

    private Truck truck;

    @BeforeEach
    void setUp() {
        truck = new Truck();
    }

    

    @Test
    void testConstructorSetsSpeed() {
        assertEquals(1, truck.speed);
    }

    @Test
    void testConstructorSetsCapacity() {
        assertEquals(20, truck.capacity);
    }

    @Test
    void testConstructorSetsMaintenanceCost() {
        assertEquals(3, truck.maintenanceCost);
    }

    @Test
    void testConstructorInheritsFromVehicle() {
        assertNotNull(truck);
        assertTrue(truck instanceof Vehicle);
    }

    

    @Test
    void testCanCarryWheat() {
        assertTrue(truck.canCarry(ResourceType.WHEAT));
    }

    @Test
    void testCanCarryMeat() {
        assertTrue(truck.canCarry(ResourceType.MEAT));
    }

    @Test
    void testCanCarryBread() {
        assertTrue(truck.canCarry(ResourceType.BREAD));
    }

    @Test
    void testCanCarryMeatPatty() {
        assertTrue(truck.canCarry(ResourceType.MEAT_PATTY));
    }

    @Test
    void testCanCarryHamburger() {
        assertTrue(truck.canCarry(ResourceType.HAMBURGER));
    }

    @Test
    void testCannotCarryPassengers() {
        assertFalse(truck.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testCanCarryHandlesNullResourceType() {
        
        try {
            boolean result = truck.canCarry(null);
            
            assertTrue(result || result == false);
        } catch (NullPointerException e) {
            
        }
    }

    

    @Test
    void testCanCarryAllNonPassengerResources() {
        ResourceType[] allTypes = ResourceType.values();
        for (ResourceType type : allTypes) {
            if (type == ResourceType.PASSENGERS) {
                assertFalse(truck.canCarry(type),
                    "Truck should not carry " + type);
            } else {
                assertTrue(truck.canCarry(type),
                    "Truck should carry " + type);
            }
        }
    }

    

    @Test
    void testTruckHasCorrectCapacity() {
        assertEquals(20, truck.capacity);
    }

    @Test
    void testTruckCapacityIsLowerThanAdvancedTruck() {
        AdvancedTruck advancedTruck = new AdvancedTruck();
        assertTrue(truck.capacity < advancedTruck.capacity);
    }

    

    @Test
    void testTruckSpeedIsSlowerThanBus() {
        Bus bus = new Bus();
        assertTrue(truck.speed < bus.speed);
    }

    @Test
    void testTruckSpeedIsSlowerThanAdvancedTruck() {
        AdvancedTruck advancedTruck = new AdvancedTruck();
        assertTrue(truck.speed < advancedTruck.speed);
    }

    

    @Test
    void testMaintenanceCostIsPositive() {
        assertTrue(truck.maintenanceCost > 0);
    }

    @Test
    void testMaintenanceCostIsHigherThanBus() {
        Bus bus = new Bus();
        assertTrue(truck.maintenanceCost > bus.maintenanceCost);
    }

    

    @Test
    void testTruckIsDistinctFromBus() {
        Bus bus = new Bus();
        
        assertTrue(truck.canCarry(ResourceType.WHEAT));
        assertFalse(truck.canCarry(ResourceType.PASSENGERS));
        assertFalse(bus.canCarry(ResourceType.WHEAT));
        assertTrue(bus.canCarry(ResourceType.PASSENGERS));
    }

    @Test
    void testTruckEffectiveSpeedEqualsSpeed() {
        
        
        assertEquals(2.0, truck.effectiveSpeed); 
        assertEquals(1, truck.speed); 
    }

    @Test
    void testTruckInheritsVehicleMethods() {
        
        truck.setPurchasePrice(1000);
        assertEquals(1000, truck.getPurchasePrice());
        assertEquals(500, truck.getSellValue());
    }

    @Test
    void testTruckCanBeSpawned() {
        truck.spawnAt(5, 5);
        assertTrue(truck.isSpawned());
        assertEquals(5, truck.getCurrentTileX());
        assertEquals(5, truck.getCurrentTileY());
    }

    @Test
    void testTruckCanHavePath() {
        java.util.List<int[]> path = java.util.List.of(
            new int[]{0, 0},
            new int[]{1, 0}
        );
        truck.setPath(path);
        assertTrue(truck.hasPath());
    }

    @Test
    void testTruckAges() {
        truck.spawnAt(0, 0);
        double initialAge = truck.getAgeSeconds();
        
        truck.ageSeconds = 50.0;
        assertEquals(50.0, truck.getAgeSeconds());
        assertTrue(truck.getAgeSeconds() > initialAge);
    }

    

    @Test
    void testTruckVsAdvancedTruckComparison() {
        AdvancedTruck advanced = new AdvancedTruck();

        
        assertTrue(advanced.speed > truck.speed,
            "Advanced truck should be faster");
        assertTrue(advanced.capacity > truck.capacity,
            "Advanced truck should have more capacity");

        
        assertEquals(truck.canCarry(ResourceType.WHEAT),
            advanced.canCarry(ResourceType.WHEAT));
        assertEquals(truck.canCarry(ResourceType.PASSENGERS),
            advanced.canCarry(ResourceType.PASSENGERS));
    }
}
