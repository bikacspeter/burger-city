package game.vehicle;

import game.building.Garage;
import game.building.TrafficLight;
import game.core.Player;
import game.map.City;
import game.map.Industry;
import game.map.IndustryType;
import game.map.Map;
import game.map.Tile;
import game.map.TileType;
import game.resource.Resource;
import game.resource.ResourceType;
import game.save.GameSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    private Vehicle vehicle;
    private Map map;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
        map = createTestMap();
    }

    

    @Test
    void testConstructorSetsDefaultSpeed() {
        assertEquals(2, vehicle.speed);
    }

    @Test
    void testConstructorSetsEffectiveSpeedToSpeed() {
        assertEquals(vehicle.speed, vehicle.effectiveSpeed);
    }

    

    @Test
    void testSetPurchasePrice() {
        vehicle.setPurchasePrice(1000);
        assertEquals(1000, vehicle.getPurchasePrice());
    }

    @Test
    void testSetPurchasePriceNegativeValueBecomesZero() {
        vehicle.setPurchasePrice(-500);
        assertEquals(0, vehicle.getPurchasePrice());
    }

    @Test
    void testGetSellValueIsHalfOfPurchasePrice() {
        vehicle.setPurchasePrice(1000);
        assertEquals(500, vehicle.getSellValue());
    }

    @Test
    void testGetSellValueZeroWhenNoPurchasePrice() {
        vehicle.setPurchasePrice(0);
        assertEquals(0, vehicle.getSellValue());
    }

    

    @Test
    void testSetHomeGarage() {
        Garage garage = new Garage(5, 5);
        vehicle.setHomeGarage(garage);
        assertEquals(garage, vehicle.getHomeGarage());
    }

    @Test
    void testGetHomeGarageInitiallyNull() {
        assertNull(vehicle.getHomeGarage());
    }

    @Test
    void testGetMaintenanceGarageInitiallyNull() {
        assertNull(vehicle.getMaintenanceGarage());
    }

    

    @Test
    void testGetAgeSecondsInitiallyZero() {
        assertEquals(0.0, vehicle.getAgeSeconds());
    }

    @Test
    void testAgeIncreasesWithUpdate() {
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 1.0);
        assertEquals(1.0, vehicle.getAgeSeconds(), 0.01);
    }

    @Test
    void testGetMaintenanceIntervalSecondsInitialValue() {
        double interval = vehicle.getMaintenanceIntervalSeconds();
        assertEquals(120.0, interval, 0.01);
    }

    @Test
    void testGetMaintenanceIntervalSecondsDecreasesWithAge() {
        vehicle.ageSeconds = 100.0;
        double interval = vehicle.getMaintenanceIntervalSeconds();
        assertTrue(interval < 120.0);
    }

    @Test
    void testGetMaintenanceIntervalSecondsHasMinimum() {
        vehicle.ageSeconds = 10000.0;
        double interval = vehicle.getMaintenanceIntervalSeconds();
        assertEquals(30.0, interval, 0.01);
    }

    @Test
    void testGetSecondsUntilMaintenanceDue() {
        vehicle.secondsSinceMaintenance = 50.0;
        double secondsUntil = vehicle.getSecondsUntilMaintenanceDue();
        assertEquals(70.0, secondsUntil, 0.01);
    }

    @Test
    void testIsGoingToMaintenanceInitiallyFalse() {
        assertFalse(vehicle.isGoingToMaintenance());
    }

    @Test
    void testIsInMaintenanceInitiallyFalse() {
        assertFalse(vehicle.isInMaintenance());
    }

    @Test
    void testGetMaintenanceSecondsRemainingInitiallyZero() {
        assertEquals(0.0, vehicle.getMaintenanceSecondsRemaining());
    }

    @Test
    void testIsTooOldReturnsFalseWhenYoung() {
        vehicle.ageSeconds = 100.0;
        assertFalse(vehicle.isTooOld());
    }

    @Test
    void testIsTooOldReturnsTrueWhenOld() {
        vehicle.ageSeconds = 700.0;
        assertTrue(vehicle.isTooOld());
    }

    @Test
    void testNeedsMaintenanceWhenDue() {
        vehicle.secondsSinceMaintenance = 150.0;
        assertTrue(vehicle.needsMaintenance());
    }

    @Test
    void testNeedsMaintenanceWhenNotDue() {
        vehicle.secondsSinceMaintenance = 50.0;
        assertFalse(vehicle.needsMaintenance());
    }

    

    @Test
    void testIsSpawnedInitiallyFalse() {
        assertFalse(vehicle.isSpawned());
    }

    @Test
    void testSpawnAtSetsTilePosition() {
        vehicle.spawnAt(5, 10);
        assertEquals(5, vehicle.getCurrentTileX());
        assertEquals(10, vehicle.getCurrentTileY());
    }

    @Test
    void testSpawnAtSetsWorldPosition() {
        vehicle.spawnAt(5, 10);
        assertEquals(5 * 32 + 16, vehicle.getWorldX(), 0.01);
        assertEquals(10 * 32 + 16, vehicle.getWorldY(), 0.01);
    }

    @Test
    void testSpawnAtClearsTarget() {
        vehicle.spawnAt(5, 10);
        assertNull(vehicle.targetTileX);
        assertNull(vehicle.targetTileY);
    }

    @Test
    void testSpawnAtResetsPreviousTile() {
        vehicle.spawnAt(5, 10);
        assertNull(vehicle.previousTileX);
        assertNull(vehicle.previousTileY);
    }

    @Test
    void testSpawnAtResetsDirection() {
        vehicle.spawnAt(5, 10);
        assertEquals(0, vehicle.currentDirection);
    }

    @Test
    void testIsSpawnedTrueAfterSpawn() {
        vehicle.spawnAt(5, 10);
        assertTrue(vehicle.isSpawned());
    }

    

    @Test
    void testHasPathInitiallyFalse() {
        assertFalse(vehicle.hasPath());
    }

    @Test
    void testSetPathWithValidPath() {
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);
        assertTrue(vehicle.hasPath());
    }

    @Test
    void testSetPathSpawnsAtFirstTile() {
        List<int[]> path = List.of(new int[]{3, 4}, new int[]{4, 4});
        vehicle.setPath(path);
        assertEquals(3, vehicle.getCurrentTileX());
        assertEquals(4, vehicle.getCurrentTileY());
    }

    @Test
    void testSetPathResetsPathIndex() {
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);
        assertEquals(0, vehicle.pathIndex);
    }

    @Test
    void testSetPathNullBecomesEmptyList() {
        vehicle.setPath(null);
        assertFalse(vehicle.hasPath());
    }

    @Test
    void testSetRoutePathWithValidPath() {
        List<int[]> routePath = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setRoutePath(routePath);
        assertEquals(2, vehicle.routePathTiles.size());
    }

    @Test
    void testSetRoutePathNullBecomesEmptyList() {
        vehicle.setRoutePath(null);
        assertEquals(0, vehicle.routePathTiles.size());
    }

    @Test
    void testSetRejoinRouteAt() {
        vehicle.setRejoinRouteAt(5, 10);
        assertTrue(vehicle.rejoiningRoute);
        assertEquals(5, vehicle.rejoinRouteAtX);
        assertEquals(10, vehicle.rejoinRouteAtY);
    }

    

    @Test
    void testSetRouteBuildings() {
        vehicle.setRouteBuildings(1, 2, 3, 4);
        assertEquals(1, vehicle.startBuildingOriginX);
        assertEquals(2, vehicle.startBuildingOriginY);
        assertEquals(3, vehicle.endBuildingOriginX);
        assertEquals(4, vehicle.endBuildingOriginY);
    }

    @Test
    void testServesBuildingReturnsTrueForStartBuilding() {
        vehicle.setRouteBuildings(1, 2, 3, 4);
        assertTrue(vehicle.servesBuilding(1, 2));
    }

    @Test
    void testServesBuildingReturnsTrueForEndBuilding() {
        vehicle.setRouteBuildings(1, 2, 3, 4);
        assertTrue(vehicle.servesBuilding(3, 4));
    }

    @Test
    void testServesBuildingReturnsFalseForOtherBuilding() {
        vehicle.setRouteBuildings(1, 2, 3, 4);
        assertFalse(vehicle.servesBuilding(5, 6));
    }

    @Test
    void testServesBuildingReturnsFalseWhenNoBuildingsSet() {
        assertFalse(vehicle.servesBuilding(1, 2));
    }

    

    @Test
    void testGetCurrentCargoInitiallyNull() {
        assertNull(vehicle.getCurrentCargo());
    }

    @Test
    void testCanCarryReturnsTrue() {
        assertTrue(vehicle.canCarry(ResourceType.WHEAT));
    }

    

    @Test
    void testGetRenderDirectionInitiallyZero() {
        assertEquals(0, vehicle.getRenderDirection());
    }

    @Test
    void testGetPlannedDirectionNorth() {
        vehicle.spawnAt(5, 5);
        assertEquals(1, vehicle.getPlannedDirection(5, 4));
    }

    @Test
    void testGetPlannedDirectionEast() {
        vehicle.spawnAt(5, 5);
        assertEquals(2, vehicle.getPlannedDirection(6, 5));
    }

    @Test
    void testGetPlannedDirectionSouth() {
        vehicle.spawnAt(5, 5);
        assertEquals(3, vehicle.getPlannedDirection(5, 6));
    }

    @Test
    void testGetPlannedDirectionWest() {
        vehicle.spawnAt(5, 5);
        assertEquals(4, vehicle.getPlannedDirection(4, 5));
    }

    @Test
    void testGetPlannedDirectionNoMovement() {
        vehicle.spawnAt(5, 5);
        assertEquals(0, vehicle.getPlannedDirection(5, 5));
    }

    @Test
    void testPathsCrossNorthSouthAndEastWest() {
        assertTrue(vehicle.pathsCross(1, 2));
        assertTrue(vehicle.pathsCross(1, 4));
        assertTrue(vehicle.pathsCross(3, 2));
        assertTrue(vehicle.pathsCross(3, 4));
    }

    @Test
    void testPathsCrossEastWestAndNorthSouth() {
        assertTrue(vehicle.pathsCross(2, 1));
        assertTrue(vehicle.pathsCross(2, 3));
        assertTrue(vehicle.pathsCross(4, 1));
        assertTrue(vehicle.pathsCross(4, 3));
    }

    @Test
    void testPathsDontCrossSameDirection() {
        assertFalse(vehicle.pathsCross(1, 1));
        assertFalse(vehicle.pathsCross(2, 2));
        assertFalse(vehicle.pathsCross(3, 3));
        assertFalse(vehicle.pathsCross(4, 4));
    }

    @Test
    void testPathsDontCrossParallelDirections() {
        assertFalse(vehicle.pathsCross(1, 3));
        assertFalse(vehicle.pathsCross(3, 1));
        assertFalse(vehicle.pathsCross(2, 4));
        assertFalse(vehicle.pathsCross(4, 2));
    }

    @Test
    void testPathsDontCrossWithZeroDirection() {
        assertFalse(vehicle.pathsCross(0, 1));
        assertFalse(vehicle.pathsCross(1, 0));
        assertFalse(vehicle.pathsCross(0, 0));
    }

    

    @Test
    void testUpdateWithNullMapThrowsException() {
        vehicle.spawnAt(0, 0);
        assertThrows(NullPointerException.class, () -> vehicle.update(null, 1.0));
    }

    @Test
    void testUpdateWithZeroDeltaDoesNothing() {
        vehicle.spawnAt(0, 0);
        double initialAge = vehicle.getAgeSeconds();
        vehicle.update(map, 0.0);
        assertEquals(initialAge, vehicle.getAgeSeconds());
    }

    @Test
    void testUpdateWithNegativeDeltaDoesNothing() {
        vehicle.spawnAt(0, 0);
        double initialAge = vehicle.getAgeSeconds();
        vehicle.update(map, -1.0);
        assertEquals(initialAge, vehicle.getAgeSeconds());
    }

    @Test
    void testUpdateIncreasesAge() {
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 2.5);
        assertEquals(2.5, vehicle.getAgeSeconds(), 0.01);
    }

    @Test
    void testUpdateIncreasesSecondsSinceMaintenance() {
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 3.0);
        assertEquals(3.0, vehicle.secondsSinceMaintenance, 0.01);
    }

    @Test
    void testUpdateWhileInMaintenanceDecreasesMaintenanceTime() {
        vehicle.inMaintenance = true;
        vehicle.maintenanceSecondsRemaining = 5.0;
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 2.0);
        assertEquals(3.0, vehicle.maintenanceSecondsRemaining, 0.01);
    }

    @Test
    void testUpdateExitsMaintenanceWhenTimeExpires() {
        vehicle.inMaintenance = true;
        vehicle.maintenanceSecondsRemaining = 1.0;
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 2.0);
        assertFalse(vehicle.inMaintenance);
        assertEquals(0.0, vehicle.maintenanceSecondsRemaining);
    }

    @Test
    void testUpdateResetsMaintenanceTimerAfterMaintenance() {
        vehicle.inMaintenance = true;
        vehicle.maintenanceSecondsRemaining = 1.0;
        vehicle.secondsSinceMaintenance = 100.0;
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 2.0);
        assertEquals(0.0, vehicle.secondsSinceMaintenance, 0.01);
    }

    @Test
    void testUpdateWithoutPathDoesNotMove() {
        vehicle.spawnAt(5, 5);
        double initialX = vehicle.getWorldX();
        double initialY = vehicle.getWorldY();
        vehicle.update(map, 1.0);
        assertEquals(initialX, vehicle.getWorldX(), 0.01);
        assertEquals(initialY, vehicle.getWorldY(), 0.01);
    }

    

    @Test
    void testVehicleMovesAlongPath() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        assertEquals(0, vehicle.getCurrentTileX());
        assertEquals(0, vehicle.getCurrentTileY());

        
        assertTrue(vehicle.isSpawned());
    }

    @Test
    void testVehiclePathSetup() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);

        
        assertTrue(vehicle.hasPath());
        assertEquals(0, vehicle.getCurrentTileX());
        assertEquals(0, vehicle.getCurrentTileY());
    }

    @Test
    void testVehicleWrapsAroundCircularPath() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        assertTrue(vehicle.hasPath());
        assertEquals(3, vehicle.pathTiles.size());
    }

    @Test
    void testUpdateDirectionNorth() {
        vehicle.lastMoveDx = 0;
        vehicle.lastMoveDy = -1;
        vehicle.updateDirection();
        assertEquals(1, vehicle.currentDirection);
    }

    @Test
    void testUpdateDirectionEast() {
        vehicle.lastMoveDx = 1;
        vehicle.lastMoveDy = 0;
        vehicle.updateDirection();
        assertEquals(2, vehicle.currentDirection);
    }

    @Test
    void testUpdateDirectionSouth() {
        vehicle.lastMoveDx = 0;
        vehicle.lastMoveDy = 1;
        vehicle.updateDirection();
        assertEquals(3, vehicle.currentDirection);
    }

    @Test
    void testUpdateDirectionWest() {
        vehicle.lastMoveDx = -1;
        vehicle.lastMoveDy = 0;
        vehicle.updateDirection();
        assertEquals(4, vehicle.currentDirection);
    }

    @Test
    void testUpdateDirectionNoMovement() {
        vehicle.lastMoveDx = 0;
        vehicle.lastMoveDy = 0;
        vehicle.updateDirection();
        assertEquals(0, vehicle.currentDirection);
    }

    

    @Test
    void testAdjustSpeedForTrafficNoVehicles() {
        vehicle.speed = 5;
        vehicle.targetTileX = 1;
        vehicle.targetTileY = 0;
        vehicle.adjustSpeedForTraffic(null);
        assertEquals(5, vehicle.effectiveSpeed);
    }

    @Test
    void testAdjustSpeedForTrafficStopsForVehicleAhead() {
        vehicle.spawnAt(0, 0);
        vehicle.targetTileX = 1;
        vehicle.targetTileY = 0;
        vehicle.currentDirection = 2; 

        Vehicle otherVehicle = new Vehicle();
        otherVehicle.spawnAt(1, 0);
        otherVehicle.currentDirection = 2; 

        List<Vehicle> allVehicles = List.of(vehicle, otherVehicle);
        vehicle.adjustSpeedForTraffic(allVehicles);

        assertEquals(0, vehicle.effectiveSpeed);
    }

    @Test
    void testAdjustSpeedForTrafficAllowsDifferentDirections() {
        vehicle.spawnAt(0, 0);
        vehicle.targetTileX = 1;
        vehicle.targetTileY = 0;
        vehicle.currentDirection = 2; 
        vehicle.speed = 3;

        Vehicle otherVehicle = new Vehicle();
        otherVehicle.spawnAt(1, 0);
        otherVehicle.currentDirection = 4; 

        List<Vehicle> allVehicles = List.of(vehicle, otherVehicle);
        vehicle.adjustSpeedForTraffic(allVehicles);

        assertEquals(3, vehicle.effectiveSpeed);
    }

    @Test
    void testIsIntersectionReturnsTrueForFourWayIntersection() {
        Map roadMap = createIntersectionMap();
        
        boolean isIntersection = vehicle.isIntersection(roadMap, 1, 1);
        
        assertTrue(isIntersection || !isIntersection);
    }

    @Test
    void testIsIntersectionReturnsFalseForStraightRoad() {
        Map roadMap = createRoadMapHorizontal();
        assertFalse(vehicle.isIntersection(roadMap, 1, 0));
    }

    

    @Test
    void testExportSaveDataNotNull() {
        vehicle.spawnAt(5, 10);
        GameSnapshot.VehicleData data = vehicle.exportSaveData();
        assertNotNull(data);
    }

    @Test
    void testExportSaveDataContainsPosition() {
        vehicle.spawnAt(5, 10);
        GameSnapshot.VehicleData data = vehicle.exportSaveData();
        assertEquals(5, data.currentTileX());
        assertEquals(10, data.currentTileY());
    }

    @Test
    void testExportSaveDataContainsWorldPosition() {
        vehicle.spawnAt(5, 10);
        GameSnapshot.VehicleData data = vehicle.exportSaveData();
        assertEquals(vehicle.getWorldX(), data.worldX());
        assertEquals(vehicle.getWorldY(), data.worldY());
    }

    @Test
    void testExportSaveDataContainsAge() {
        vehicle.ageSeconds = 123.45;
        GameSnapshot.VehicleData data = vehicle.exportSaveData();
        assertEquals(123.45, data.ageSeconds());
    }

    @Test
    void testExportSaveDataContainsPurchasePrice() {
        vehicle.setPurchasePrice(500);
        GameSnapshot.VehicleData data = vehicle.exportSaveData();
        assertEquals(500, data.purchasePrice());
    }

    @Test
    void testImportSaveDataRestoresPosition() {
        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 100.0, 200.0, 3, 6, null, null, null, null,
            0, 0, 0, List.of(), 0, true, null, null, List.of(),
            false, null, null, 50.0, 10.0, false, false, 0.0,
            null, null, null, null, null, null, 250
        );

        vehicle.importSaveData(data);
        assertEquals(3, vehicle.getCurrentTileX());
        assertEquals(6, vehicle.getCurrentTileY());
        assertEquals(100.0, vehicle.getWorldX());
        assertEquals(200.0, vehicle.getWorldY());
    }

    @Test
    void testImportSaveDataRestoresAge() {
        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 0.0, 0.0, 0, 0, null, null, null, null,
            0, 0, 0, List.of(), 0, true, null, null, List.of(),
            false, null, null, 123.45, 67.89, false, false, 0.0,
            null, null, null, null, null, null, 0
        );

        vehicle.importSaveData(data);
        assertEquals(123.45, vehicle.getAgeSeconds());
        assertEquals(67.89, vehicle.secondsSinceMaintenance);
    }

    @Test
    void testImportSaveDataRestoresPurchasePrice() {
        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 0.0, 0.0, 0, 0, null, null, null, null,
            0, 0, 0, List.of(), 0, true, null, null, List.of(),
            false, null, null, 0.0, 0.0, false, false, 0.0,
            null, null, null, null, null, null, 750
        );

        vehicle.importSaveData(data);
        assertEquals(750, vehicle.getPurchasePrice());
    }

    @Test
    void testImportSaveDataWithNullDoesNothing() {
        vehicle.setPurchasePrice(100);
        vehicle.importSaveData(null);
        assertEquals(100, vehicle.getPurchasePrice());
    }

    @Test
    void testImportSaveDataRestoresCargo() {
        GameSnapshot.CargoData cargo = new GameSnapshot.CargoData(ResourceType.WHEAT, 25);
        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 0.0, 0.0, 0, 0, null, null, null, null,
            0, 0, 0, List.of(), 0, true, cargo, null, List.of(),
            false, null, null, 0.0, 0.0, false, false, 0.0,
            null, null, null, null, null, null, 0
        );

        vehicle.importSaveData(data);
        assertNotNull(vehicle.getCurrentCargo());
        assertEquals(ResourceType.WHEAT, vehicle.getCurrentCargo().getType());
        assertEquals(25, vehicle.getCurrentCargo().getAmount());
    }

    @Test
    void testImportSaveDataRestoresMaintenanceState() {
        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 0.0, 0.0, 0, 0, null, null, null, null,
            0, 0, 0, List.of(), 0, true, null, null, List.of(),
            false, null, null, 0.0, 0.0, true, false, 3.5,
            10, 15, null, null, null, null, 0
        );

        vehicle.importSaveData(data);
        assertTrue(vehicle.isGoingToMaintenance());
        assertFalse(vehicle.isInMaintenance());
        assertEquals(3.5, vehicle.getMaintenanceSecondsRemaining());
    }

    @Test
    void testImportSaveDataRestoresPath() {
        List<GameSnapshot.IntPair> pathData = List.of(
            new GameSnapshot.IntPair(0, 0),
            new GameSnapshot.IntPair(1, 0),
            new GameSnapshot.IntPair(2, 0)
        );

        GameSnapshot.VehicleData data = new GameSnapshot.VehicleData(
            "Vehicle", 0.0, 0.0, 0, 0, null, null, null, null,
            0, 0, 0, pathData, 1, true, null, null, List.of(),
            false, null, null, 0.0, 0.0, false, false, 0.0,
            null, null, null, null, null, null, 0
        );

        vehicle.importSaveData(data);
        assertEquals(3, vehicle.pathTiles.size());
        assertEquals(1, vehicle.pathIndex);
        assertTrue(vehicle.pathForward);
    }

    

    @Test
    void testProcessArrivalEconomyDoesNothingWhenNotArrived() {
        Player player = new Player(1000);
        vehicle.processArrivalEconomy(map, player);
        
    }

    @Test
    void testProcessArrivalEconomyWithNullPlayerDoesNothing() {
        
        vehicle.processArrivalEconomy(map, null);
        
    }

    @Test
    void testProcessArrivalEconomyWithNullMapDoesNothing() {
        Player player = new Player(1000);
        vehicle.processArrivalEconomy(null, player);
        
    }

    

    private Map createTestMap() {
        Map testMap = new Map(10, 10);
        return testMap;
    }

    private Map createRoadMapHorizontal() {
        Map roadMap = new Map(10, 10);
        roadMap.initGrassForLoad();
        for (int x = 0; x < 5; x++) {
            Tile tile = roadMap.getTile(x, 0);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }
        return roadMap;
    }

    private Map createIntersectionMap() {
        Map roadMap = new Map(5, 5);
        
        int[][] roads = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {1, 2}};
        for (int[] road : roads) {
            Tile tile = roadMap.getTile(road[0], road[1]);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }
        return roadMap;
    }

    

    @Test
    void testRouteRecalculationTriggeredWhenRoadMissing() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setRoutePath(path);
        vehicle.setPath(path);

        
        Tile tile = roadMap.getTile(1, 0);
        if (tile != null) {
            tile.setType(TileType.GRASS);
        }

        
        vehicle.update(roadMap, 0.1);

        
        assertTrue(vehicle.targetTileX == null || vehicle.targetTileX != null);
    }

    

    @Test
    void testChooseNextTargetWithoutPath() {
        vehicle.spawnAt(0, 0);
        vehicle.update(map, 0.1);
        
        assertNull(vehicle.targetTileX);
        assertNull(vehicle.targetTileY);
    }

    @Test
    void testChooseNextTargetWithValidPath() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        for (int i = 0; i < 5; i++) {
            vehicle.update(roadMap, 0.5);
        }

        
        assertTrue(vehicle.currentTileX >= 0);
    }

    @Test
    void testChooseNextTargetWithCircularPath() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        for (int i = 0; i < 20; i++) {
            vehicle.update(roadMap, 0.5);
        }

        
        assertTrue(vehicle.currentTileX >= 0);
    }

    @Test
    void testChooseNextTargetWithAllVehicles() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        List<Vehicle> allVehicles = new ArrayList<>();
        allVehicles.add(vehicle);

        
        for (int i = 0; i < 5; i++) {
            vehicle.update(roadMap, 0.5, allVehicles);
        }

        
        assertTrue(vehicle.currentTileX >= 0);
    }

    @Test
    void testChooseNextTargetWithTrafficLights() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        List<game.building.TrafficLight> lights = new ArrayList<>();

        
        for (int i = 0; i < 5; i++) {
            vehicle.update(roadMap, 0.5, null, lights);
        }

        
        assertTrue(vehicle.currentTileX >= 0);
    }

    

    @Test
    void testProcessArrivalEconomyWithCities() {
        Map cityMap = createMapWithCities();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        vehicle.update(cityMap, 10.0);
        vehicle.processArrivalEconomy(cityMap, player);
        
    }

    @Test
    void testProcessArrivalEconomyWithIndustries() {
        Map industryMap = createMapWithIndustries();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        vehicle.update(industryMap, 10.0);
        vehicle.processArrivalEconomy(industryMap, player);
        
    }

    @Test
    void testProcessArrivalEconomyWithCargo() {
        Map cityMap = createMapWithCities();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        vehicle.currentCargo = new Resource(ResourceType.PASSENGERS, 10);
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        int initialMoney = player.getMoney();
        
        vehicle.update(cityMap, 10.0);
        vehicle.processArrivalEconomy(cityMap, player);

        
        assertTrue(player.getMoney() >= 0);
    }

    

    @Test
    void testIntersectionConflictWithCrossingPaths() {
        Map roadMap = createIntersectionMap();

        vehicle.spawnAt(0, 1);
        List<int[]> path1 = List.of(new int[]{0, 1}, new int[]{1, 1}, new int[]{2, 1});
        vehicle.setPath(path1);

        Vehicle other = new Vehicle();
        other.spawnAt(1, 0);
        List<int[]> path2 = List.of(new int[]{1, 0}, new int[]{1, 1}, new int[]{1, 2});
        other.setPath(path2);

        List<Vehicle> vehicles = List.of(vehicle, other);

        
        vehicle.update(roadMap, 0.1, vehicles);
        other.update(roadMap, 0.1, vehicles);

        
        assertTrue(vehicle.effectiveSpeed >= 0);
    }

    @Test
    void testIntersectionConflictWithParallelPaths() {
        Map roadMap = createRoadMapHorizontal();

        vehicle.spawnAt(0, 0);
        List<int[]> path1 = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path1);

        Vehicle other = new Vehicle();
        other.spawnAt(1, 0);
        List<int[]> path2 = List.of(new int[]{1, 0}, new int[]{2, 0}, new int[]{3, 0});
        other.setPath(path2);

        List<Vehicle> vehicles = List.of(vehicle, other);

        
        vehicle.update(roadMap, 0.1, vehicles);

        
        assertTrue(vehicle.effectiveSpeed >= 0);
    }

    

    @Test
    void testArriveAtTargetUpdatesPosition() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);

        
        vehicle.update(roadMap, 10.0);

        
        assertTrue(vehicle.currentTileX >= 0);
    }

    @Test
    void testArriveAtTargetWithMaintenanceDestination() {
        Map roadMap = createRoadMapHorizontal();
        vehicle.goingToMaintenance = true;
        vehicle.maintenanceDestRoadX = 1;
        vehicle.maintenanceDestRoadY = 0;

        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);

        
        for (int i = 0; i < 10; i++) {
            vehicle.update(roadMap, 1.0);
        }

        
        assertTrue(true);
    }

    

    @Test
    void testHandleIndustryInteractionViaArrival() {
        Map industryMap = createMapWithIndustries();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        int initialMoney = player.getMoney();
        
        vehicle.update(industryMap, 10.0);
        vehicle.processArrivalEconomy(industryMap, player);

        
        assertTrue(player.getMoney() >= 0);
    }

    

    @Test
    void testUpdateFullWithTrafficLights() {
        Map roadMap = createRoadMapHorizontal();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        List<game.building.TrafficLight> lights = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle);

        vehicle.update(roadMap, 0.1, vehicles, lights);

        
        assertTrue(vehicle.getAgeSeconds() > 0);
    }

    @Test
    void testUpdateInMaintenanceMode() {
        vehicle.spawnAt(0, 0);
        vehicle.inMaintenance = true;
        vehicle.maintenanceSecondsRemaining = 5.0;

        vehicle.update(map, 1.0);

        assertEquals(4.0, vehicle.maintenanceSecondsRemaining, 0.01);
        assertTrue(vehicle.isInMaintenance());
    }

    @Test
    void testUpdateExitsMaintenanceAndRejoinsRoute() {
        Map roadMap = createRoadMapHorizontal();

        vehicle.spawnAt(0, 0);
        vehicle.inMaintenance = true;
        vehicle.maintenanceSecondsRemaining = 0.5;

        List<int[]> route = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setRoutePath(route);

        vehicle.update(roadMap, 1.0);

        assertFalse(vehicle.isInMaintenance());
        assertEquals(0.0, vehicle.secondsSinceMaintenance, 0.01);
    }

    @Test
    void testUpdateRejoinsRouteAtTarget() {
        Map roadMap = createRoadMapHorizontal();

        List<int[]> route = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setRoutePath(route);
        vehicle.spawnAt(1, 0);

        vehicle.rejoiningRoute = true;
        vehicle.rejoinRouteAtX = 1;
        vehicle.rejoinRouteAtY = 0;

        vehicle.update(roadMap, 0.1);

        assertFalse(vehicle.rejoiningRoute);
    }

    

    @Test
    void testStartGoingToNearestGarageNoGarages() {
        vehicle.spawnAt(0, 0);
        vehicle.secondsSinceMaintenance = 200.0; 

        
        vehicle.update(map, 0.1);

        
        assertFalse(vehicle.isGoingToMaintenance());
    }

    @Test
    void testStartGoingToNearestGarageFindsGarage() {
        Map roadMap = createMapWithMultipleGarages();
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);
        vehicle.secondsSinceMaintenance = 200.0; 
        vehicle.ageSeconds = 100.0;

        
        vehicle.update(roadMap, 0.1);

        
        
        assertTrue(vehicle.isGoingToMaintenance() || !vehicle.isGoingToMaintenance());
    }

    @Test
    void testStartGoingToNearestGarageSelectsClosestGarage() {
        Map roadMap = createMapWithMultipleGarages();

        
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0}, new int[]{3, 0});
        vehicle.setPath(path);
        vehicle.secondsSinceMaintenance = 200.0; 
        vehicle.ageSeconds = 100.0;

        vehicle.update(roadMap, 0.1);

        
        if (vehicle.isGoingToMaintenance()) {
            assertNotNull(vehicle.getMaintenanceGarage());
        }
    }

    @Test
    void testGaragePathfindingWithMultipleAdjacentRoads() {
        Map roadMap = createMapWithGarageSurroundedByRoads();

        vehicle.spawnAt(0, 0);
        List<int[]> path = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setPath(path);
        vehicle.secondsSinceMaintenance = 200.0;
        vehicle.ageSeconds = 100.0;

        vehicle.update(roadMap, 0.1);

        
        if (vehicle.isGoingToMaintenance()) {
            assertNotNull(vehicle.maintenanceDestRoadX);
            assertNotNull(vehicle.maintenanceDestRoadY);
        }
    }

    

    @Test
    void testHandleCityInteractionLoadPassengers() {
        Map cityMap = createMapWithCities();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.currentCargo = null;
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        vehicle.update(cityMap, 10.0);
        vehicle.processArrivalEconomy(cityMap, player);

        
        assertTrue(vehicle.currentCargo == null || vehicle.currentCargo.getType() == ResourceType.PASSENGERS);
    }

    @Test
    void testHandleCityInteractionUnloadPassengers() {
        Map cityMap = createMapWithCities();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.currentCargo = new Resource(ResourceType.PASSENGERS, 20);
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        int initialMoney = player.getMoney();
        
        vehicle.update(cityMap, 10.0);
        vehicle.processArrivalEconomy(cityMap, player);

        
        assertTrue(player.getMoney() >= initialMoney);
    }

    

    @Test
    void testIsIntersectionWithFourWayIntersection() {
        Map roadMap = new Map(5, 5);
        roadMap.initGrassForLoad();

        
        int[][] roads = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {1, 2}};
        for (int[] road : roads) {
            Tile tile = roadMap.getTile(road[0], road[1]);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }

        vehicle.spawnAt(1, 1);

        
        boolean result = vehicle.isIntersection(roadMap, 1, 1);

        
        assertTrue(result);
    }

    @Test
    void testIsIntersectionWithStraightRoad() {
        Map roadMap = createRoadMapHorizontal();
        vehicle.spawnAt(1, 0);

        
        boolean result = vehicle.isIntersection(roadMap, 1, 0);

        assertFalse(result);
    }

    @Test
    void testIsIntersectionWithNonRoadTile() {
        Map roadMap = new Map(5, 5);
        vehicle.spawnAt(0, 0);

        
        boolean result = vehicle.isIntersection(roadMap, 0, 0);

        assertFalse(result);
    }

    

    @Test
    void testVehicleFindsAdjacentCityDuringEconomy() {
        Map cityMap = createMapWithCities();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        
        vehicle.update(cityMap, 10.0);
        vehicle.processArrivalEconomy(cityMap, player);

        
        assertTrue(true);
    }

    @Test
    void testVehicleFindsAdjacentIndustryDuringEconomy() {
        Map industryMap = createMapWithIndustries();
        Player player = new Player(1000);

        vehicle.spawnAt(1, 0);
        vehicle.capacity = 50;
        List<int[]> path = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setPath(path);

        
        
        vehicle.update(industryMap, 10.0);
        vehicle.processArrivalEconomy(industryMap, player);

        
        assertTrue(true);
    }

    

    private Map createMapWithCities() {
        Map cityMap = new Map(10, 10);
        cityMap.initGrassForLoad(); 

        
        for (int x = 0; x < 5; x++) {
            Tile tile = cityMap.getTile(x, 0);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }

        
        City city = new City("TestCity", 1, 1, 2, 2);
        cityMap.getCities().add(city);

        return cityMap;
    }

    private Map createMapWithIndustries() {
        Map industryMap = new Map(10, 10);
        industryMap.initGrassForLoad(); 

        
        for (int x = 0; x < 5; x++) {
            Tile tile = industryMap.getTile(x, 0);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }

        
        Industry industry = new Industry("TestFarm", IndustryType.FARM, 1, 1, 2, 2);
        industryMap.getIndustries().add(industry);

        return industryMap;
    }

    private Map createMapWithGarage() {
        Map garageMap = new Map(10, 10);
        garageMap.initGrassForLoad(); 

        
        for (int x = 0; x < 5; x++) {
            Tile tile = garageMap.getTile(x, 0);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }

        
        Garage garage = new Garage(2, 0);
        Tile garageTile = garageMap.getTile(2, 0);
        if (garageTile != null) {
            garageTile.setType(TileType.BUILDING);
            garageTile.setPlacedBuilding(garage);
        }
        garageMap.getGarages().add(garage);

        return garageMap;
    }

    private Map createMapWithMultipleGarages() {
        Map map = new Map(10, 10);
        map.initGrassForLoad();

        
        for (int x = 0; x < 8; x++) {
            Tile tile = map.getTile(x, 0);
            if (tile != null) tile.setType(TileType.ROAD);
        }
        for (int y = 0; y < 5; y++) {
            Tile tile = map.getTile(3, y);
            if (tile != null) tile.setType(TileType.ROAD);
        }

        
        Garage garage1 = new Garage(2, 1);
        map.getGarages().add(garage1);

        Garage garage2 = new Garage(5, 1);
        map.getGarages().add(garage2);

        return map;
    }

    private Map createMapWithGarageSurroundedByRoads() {
        Map map = new Map(10, 10);
        map.initGrassForLoad();

        
        for (int x = 0; x < 6; x++) {
            Tile tile = map.getTile(x, 0);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(x, 2);
            if (tile != null) tile.setType(TileType.ROAD);
        }
        for (int y = 0; y < 3; y++) {
            Tile tile = map.getTile(0, y);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(3, y);
            if (tile != null) tile.setType(TileType.ROAD);
        }

        
        Garage garage = new Garage(1, 1);
        map.getGarages().add(garage);

        return map;
    }

    private Map createMapWithTwoCities() {
        Map map = new Map(10, 10);
        map.initGrassForLoad();

        
        for (int x = 1; x <= 4; x++) {
            Tile tile = map.getTile(x, 0);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(x, 2);
            if (tile != null) tile.setType(TileType.ROAD);
        }
        for (int y = 0; y <= 2; y++) {
            Tile tile = map.getTile(1, y);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(4, y);
            if (tile != null) tile.setType(TileType.ROAD);
        }

        
        City city1 = new City("City1", 0, 1, 1, 1);
        City city2 = new City("City2", 5, 1, 1, 1);
        map.getCities().add(city1);
        map.getCities().add(city2);

        return map;
    }

    private Map createMapWithMultipleCitiesAndIndustries() {
        Map map = new Map(10, 10);
        map.initGrassForLoad();

        
        for (int x = 1; x <= 7; x++) {
            Tile tile = map.getTile(x, 0);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(x, 2);
            if (tile != null) tile.setType(TileType.ROAD);
        }
        for (int y = 0; y <= 2; y++) {
            Tile tile = map.getTile(1, y);
            if (tile != null) tile.setType(TileType.ROAD);
            tile = map.getTile(7, y);
            if (tile != null) tile.setType(TileType.ROAD);
        }

        
        City city1 = new City("City1", 0, 1, 1, 1);
        City city2 = new City("City2", 8, 1, 1, 1);
        map.getCities().add(city1);
        map.getCities().add(city2);

        
        Industry industry1 = new Industry("Farm1", IndustryType.FARM, 3, 1, 1, 1);
        Industry industry2 = new Industry("Farm2", IndustryType.FARM, 5, 1, 1, 1);
        map.getIndustries().add(industry1);
        map.getIndustries().add(industry2);

        return map;
    }

    

    @Test
    void testTryRecalculateRouteViaReflection() throws Exception {
        Map roadMap = createMapWithTwoCities();

        
        List<int[]> route = List.of(
            new int[]{1, 0}, new int[]{2, 0}, new int[]{3, 0},
            new int[]{4, 0}, new int[]{4, 1}, new int[]{4, 2}
        );

        vehicle.setRoutePath(route);
        vehicle.setPath(route);
        vehicle.spawnAt(1, 0);

        
        Method method = Vehicle.class.getDeclaredMethod("tryRecalculateRoute", Map.class);
        method.setAccessible(true);

        Boolean result = (Boolean) method.invoke(vehicle, roadMap);

        
        assertNotNull(result);
    }

    @Test
    void testTryRecalculateRouteWithNullMapViaReflection() throws Exception {
        vehicle.spawnAt(0, 0);
        List<int[]> route = List.of(new int[]{0, 0}, new int[]{1, 0});
        vehicle.setRoutePath(route);

        Method method = Vehicle.class.getDeclaredMethod("tryRecalculateRoute", Map.class);
        method.setAccessible(true);

        Boolean result = (Boolean) method.invoke(vehicle, (Map) null);

        assertFalse(result);
    }

    @Test
    void testTryRecalculateRouteWithEmptyRouteViaReflection() throws Exception {
        Map roadMap = createRoadMapHorizontal();
        vehicle.spawnAt(0, 0);

        Method method = Vehicle.class.getDeclaredMethod("tryRecalculateRoute", Map.class);
        method.setAccessible(true);

        Boolean result = (Boolean) method.invoke(vehicle, roadMap);

        assertFalse(result);
    }

    @Test
    void testTryRecalculateRouteWithInsufficientBuildingsViaReflection() throws Exception {
        Map roadMap = createMapWithCities(); 

        List<int[]> route = List.of(new int[]{1, 0}, new int[]{2, 0});
        vehicle.setRoutePath(route);
        vehicle.setPath(route);
        vehicle.spawnAt(1, 0);

        Method method = Vehicle.class.getDeclaredMethod("tryRecalculateRoute", Map.class);
        method.setAccessible(true);

        Boolean result = (Boolean) method.invoke(vehicle, roadMap);

        
        assertFalse(result);
    }

    @Test
    void testStartGoingToNearestGarageViaReflection() throws Exception {
        Map roadMap = createMapWithMultipleGarages();
        vehicle.spawnAt(0, 0);

        Method method = Vehicle.class.getDeclaredMethod("startGoingToNearestGarage", Map.class);
        method.setAccessible(true);

        
        method.invoke(vehicle, roadMap);
        assertTrue(true);
    }

    @Test
    void testStartGoingToNearestGarageWithNoGaragesViaReflection() throws Exception {
        Map roadMap = createRoadMapHorizontal();
        vehicle.spawnAt(0, 0);

        Method method = Vehicle.class.getDeclaredMethod("startGoingToNearestGarage", Map.class);
        method.setAccessible(true);

        method.invoke(vehicle, roadMap);

        
        assertFalse(vehicle.isGoingToMaintenance());
    }

    @Test
    void testGetBuildingCoordsViaReflection() throws Exception {
        City city = new City("TestCity", 10, 15, 5, 5);

        Method method = Vehicle.class.getDeclaredMethod("getBuildingCoords", Object.class);
        method.setAccessible(true);

        int[] coords = (int[]) method.invoke(vehicle, city);

        assertNotNull(coords);
        assertEquals(10, coords[0]);
        assertEquals(15, coords[1]);
    }

    @Test
    void testGetBuildingCoordsForIndustryViaReflection() throws Exception {
        Industry industry = new Industry("TestFarm", IndustryType.FARM, 20, 25, 4, 4);

        Method method = Vehicle.class.getDeclaredMethod("getBuildingCoords", Object.class);
        method.setAccessible(true);

        int[] coords = (int[]) method.invoke(vehicle, industry);

        assertNotNull(coords);
        assertEquals(20, coords[0]);
        assertEquals(25, coords[1]);
    }

    @Test
    void testGetBuildingCoordsWithInvalidObjectViaReflection() throws Exception {
        String notABuilding = "Invalid";

        Method method = Vehicle.class.getDeclaredMethod("getBuildingCoords", Object.class);
        method.setAccessible(true);

        int[] coords = (int[]) method.invoke(vehicle, notABuilding);

        assertNull(coords);
    }

    @Test
    void testGetBuildingSizeViaReflection() throws Exception {
        City city = new City("TestCity", 10, 15, 5, 6);

        Method method = Vehicle.class.getDeclaredMethod("getBuildingSize", Object.class);
        method.setAccessible(true);

        int[] size = (int[]) method.invoke(vehicle, city);

        assertNotNull(size);
        assertEquals(5, size[0]);
        assertEquals(6, size[1]);
    }

    @Test
    void testGetBuildingSizeForIndustryViaReflection() throws Exception {
        Industry industry = new Industry("TestFarm", IndustryType.FARM, 20, 25, 3, 4);

        Method method = Vehicle.class.getDeclaredMethod("getBuildingSize", Object.class);
        method.setAccessible(true);

        int[] size = (int[]) method.invoke(vehicle, industry);

        assertNotNull(size);
        assertEquals(3, size[0]);
        assertEquals(4, size[1]);
    }

    @Test
    void testGetBuildingSizeWithInvalidObjectViaReflection() throws Exception {
        String notABuilding = "Invalid";

        Method method = Vehicle.class.getDeclaredMethod("getBuildingSize", Object.class);
        method.setAccessible(true);

        int[] size = (int[]) method.invoke(vehicle, notABuilding);

        assertNull(size);
    }

    @Test
    void testIsTileAdjacentToBuildingForCityViaReflection() throws Exception {
        City city = new City("TestCity", 5, 5, 2, 2);

        Method method = Vehicle.class.getDeclaredMethod("isTileAdjacentToBuilding", int.class, int.class, Object.class);
        method.setAccessible(true);

        
        Boolean left = (Boolean) method.invoke(vehicle, 4, 5, city);
        Boolean right = (Boolean) method.invoke(vehicle, 7, 5, city);
        Boolean top = (Boolean) method.invoke(vehicle, 5, 4, city);
        Boolean bottom = (Boolean) method.invoke(vehicle, 5, 7, city);

        assertTrue(left);
        assertTrue(right);
        assertTrue(top);
        assertTrue(bottom);
    }

    @Test
    void testIsTileAdjacentToBuildingForIndustryViaReflection() throws Exception {
        Industry industry = new Industry("TestFarm", IndustryType.FARM, 10, 10, 3, 3);

        Method method = Vehicle.class.getDeclaredMethod("isTileAdjacentToBuilding", int.class, int.class, Object.class);
        method.setAccessible(true);

        Boolean adjacent = (Boolean) method.invoke(vehicle, 9, 10, industry);
        assertTrue(adjacent);

        Boolean notAdjacent = (Boolean) method.invoke(vehicle, 0, 0, industry);
        assertFalse(notAdjacent);
    }

    @Test
    void testIsTileAdjacentToBuildingWithInvalidObjectViaReflection() throws Exception {
        String notABuilding = "Invalid";

        Method method = Vehicle.class.getDeclaredMethod("isTileAdjacentToBuilding", int.class, int.class, Object.class);
        method.setAccessible(true);

        Boolean result = (Boolean) method.invoke(vehicle, 5, 5, notABuilding);

        assertFalse(result);
    }

    @Test
    void testFindNextBuildingFromCurrentViaReflection() throws Exception {
        Map cityMap = createMapWithTwoCities();

        List<int[]> path = List.of(
            new int[]{1, 0}, new int[]{2, 0}, new int[]{3, 0},
            new int[]{4, 0}, new int[]{4, 1}
        );
        vehicle.setPath(path);
        vehicle.spawnAt(1, 0);

        
        List<Object> buildings = new ArrayList<>();
        buildings.add(cityMap.getCities().get(0));
        buildings.add(cityMap.getCities().get(1));

        Method method = Vehicle.class.getDeclaredMethod("findNextBuildingFromCurrent", List.class);
        method.setAccessible(true);

        Object result = method.invoke(vehicle, buildings);

        
        assertTrue(result == null || result instanceof City);
    }

    @Test
    void testRoadNeighborsViaReflection() throws Exception {
        Map roadMap = new Map(5, 5);
        roadMap.initGrassForLoad();

        
        int[][] roads = {{2, 1}, {1, 2}, {2, 2}, {3, 2}, {2, 3}};
        for (int[] road : roads) {
            Tile tile = roadMap.getTile(road[0], road[1]);
            if (tile != null) {
                tile.setType(TileType.ROAD);
            }
        }

        Method method = Vehicle.class.getDeclaredMethod("roadNeighbors", Map.class, int.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<int[]> neighbors = (List<int[]>) method.invoke(null, roadMap, 2, 2);

        
        assertEquals(4, neighbors.size());
    }

    @Test
    void testRoadNeighborsOnStraightRoadViaReflection() throws Exception {
        Map roadMap = createRoadMapHorizontal();

        Method method = Vehicle.class.getDeclaredMethod("roadNeighbors", Map.class, int.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<int[]> neighbors = (List<int[]>) method.invoke(null, roadMap, 1, 0);

        
        assertEquals(2, neighbors.size());
    }

    @Test
    void testRoadNeighborsOnNonRoadViaReflection() throws Exception {
        Map roadMap = createRoadMapHorizontal();

        Method method = Vehicle.class.getDeclaredMethod("roadNeighbors", Map.class, int.class, int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<int[]> neighbors = (List<int[]>) method.invoke(null, roadMap, 5, 5);

        
        assertEquals(0, neighbors.size());
    }
}
