package game.vehicle;

import game.route.Route;
import game.core.Player;
import game.core.ResourcePrices;
import game.resource.Resource;
import game.resource.ResourceType;
import game.building.Garage;
import game.map.City;
import game.map.Industry;
import game.map.Map;
import game.map.Tile;
import game.map.TileType;
import game.save.GameSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vehicle {

    public static final int TILE_SIZE_PX = 32;

    protected int speed;
    protected int capacity;
    protected int maintenanceCost;
    protected double ageSeconds;
    protected double secondsSinceMaintenance;

    
    protected boolean goingToMaintenance = false;
    protected boolean inMaintenance = false;
    protected double maintenanceSecondsRemaining = 0;
    protected Garage maintenanceGarage;
    protected Integer maintenanceDestRoadX;
    protected Integer maintenanceDestRoadY;

    
    protected List<int[]> routePathTiles = List.of();
    protected boolean rejoiningRoute = false;
    protected Integer rejoinRouteAtX;
    protected Integer rejoinRouteAtY;

    protected int purchasePrice = 0;

    protected Route route;
    protected Resource currentCargo;
    protected Garage garage;

    
    protected Integer startBuildingOriginX;
    protected Integer startBuildingOriginY;
    protected Integer endBuildingOriginX;
    protected Integer endBuildingOriginY;

    
    protected double worldX;
    protected double worldY;

    protected int currentTileX;
    protected int currentTileY;

    protected Integer targetTileX;
    protected Integer targetTileY;

    protected Integer previousTileX;
    protected Integer previousTileY;

    protected int lastMoveDx;
    protected int lastMoveDy;

    
    protected List<int[]> pathTiles = List.of();
    protected int pathIndex = 0;
    protected boolean pathForward = true;

    private boolean arrivedThisUpdate = false;
    private boolean maintenanceRequested = false;

    
    protected int currentDirection = 0;

    
    private int lastRenderDirection = 0;
    
    protected double effectiveSpeed;
    
    protected Integer intersectionClaimX = null;
    protected Integer intersectionClaimY = null;

    public Vehicle() {
        
        this.speed = 2;
        this.effectiveSpeed = this.speed;
    }

    public void setPurchasePrice(int purchasePrice) {
        this.purchasePrice = Math.max(0, purchasePrice);
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public void setHomeGarage(Garage garage) {
        this.garage = garage;
    }

    public Garage getHomeGarage() {
        return garage;
    }

    public Garage getMaintenanceGarage() {
        return maintenanceGarage;
    }

    public double getAgeSeconds() {
        return ageSeconds;
    }

    public double getMaintenanceIntervalSeconds() {
        
        
        double base = 120.0;
        double min = 30.0;
        double interval = base - (ageSeconds * 0.20);
        return Math.max(min, interval);
    }

    public double getSecondsUntilMaintenanceDue() {
        return Math.max(0.0, getMaintenanceIntervalSeconds() - secondsSinceMaintenance);
    }

    public boolean isGoingToMaintenance() {
        return goingToMaintenance;
    }

    public boolean isInMaintenance() {
        return inMaintenance;
    }

    public double getMaintenanceSecondsRemaining() {
        return maintenanceSecondsRemaining;
    }

    public boolean isTooOld() {
        return ageSeconds >= 600.0; 
    }

    public int getSellValue() {
        if (purchasePrice <= 0) return 0;
        
        return purchasePrice / 2;
    }

    


    public void setRoutePath(List<int[]> routePathTiles) {
        this.routePathTiles = (routePathTiles == null) ? List.of() : routePathTiles;
    }

    


    public void setRejoinRouteAt(int x, int y) {
        this.rejoiningRoute = true;
        this.rejoinRouteAtX = x;
        this.rejoinRouteAtY = y;
    }

    private static List<GameSnapshot.IntPair> toIntPairs(List<int[]> tiles) {
        List<GameSnapshot.IntPair> pts = new ArrayList<>();
        if (tiles == null) return pts;
        for (int[] p : tiles) {
            if (p == null || p.length < 2) continue;
            pts.add(new GameSnapshot.IntPair(p[0], p[1]));
        }
        return pts;
    }

    public GameSnapshot.VehicleData exportSaveData() {
        List<GameSnapshot.IntPair> pts = toIntPairs(pathTiles);
        List<GameSnapshot.IntPair> routePts = toIntPairs(routePathTiles);

        GameSnapshot.CargoData cargoData = null;
        if (currentCargo != null && !currentCargo.isEmpty()) {
            cargoData = new GameSnapshot.CargoData(currentCargo.getType(), currentCargo.getAmount());
        }

        GameSnapshot.RouteBuildingsData rb = null;
        if (startBuildingOriginX != null && startBuildingOriginY != null && endBuildingOriginX != null && endBuildingOriginY != null) {
            rb = new GameSnapshot.RouteBuildingsData(startBuildingOriginX, startBuildingOriginY, endBuildingOriginX, endBuildingOriginY);
        }

        Integer homeGarageX = (garage == null) ? null : garage.getX();
        Integer homeGarageY = (garage == null) ? null : garage.getY();
        Integer maintenanceGarageX = (maintenanceGarage == null) ? null : maintenanceGarage.getX();
        Integer maintenanceGarageY = (maintenanceGarage == null) ? null : maintenanceGarage.getY();

        return new GameSnapshot.VehicleData(
                getClass().getSimpleName(),
                worldX,
                worldY,
                currentTileX,
                currentTileY,
                targetTileX,
                targetTileY,
                previousTileX,
                previousTileY,
                lastMoveDx,
                lastMoveDy,
                currentDirection,
                pts,
                pathIndex,
                pathForward,
                cargoData,
                rb,
                routePts,
                rejoiningRoute,
                rejoinRouteAtX,
                rejoinRouteAtY,
                ageSeconds,
                secondsSinceMaintenance,
                goingToMaintenance,
                inMaintenance,
                maintenanceSecondsRemaining,
                maintenanceDestRoadX,
                maintenanceDestRoadY,
                homeGarageX,
                homeGarageY,
                maintenanceGarageX,
                maintenanceGarageY,
                purchasePrice
        );
    }

    public void importSaveData(GameSnapshot.VehicleData data) {
        importSaveData(data, null);
    }

    public void importSaveData(GameSnapshot.VehicleData data, Map map) {
        if (data == null) return;

        this.worldX = data.worldX();
        this.worldY = data.worldY();
        this.currentTileX = data.currentTileX();
        this.currentTileY = data.currentTileY();
        this.targetTileX = data.targetTileX();
        this.targetTileY = data.targetTileY();
        this.previousTileX = data.previousTileX();
        this.previousTileY = data.previousTileY();
        this.lastMoveDx = data.lastMoveDx();
        this.lastMoveDy = data.lastMoveDy();
        this.currentDirection = data.currentDirection();

        
        List<int[]> newPath = new ArrayList<>();
        if (data.pathTiles() != null) {
            for (GameSnapshot.IntPair p : data.pathTiles()) {
                if (p == null) continue;
                newPath.add(new int[]{p.x(), p.y()});
            }
        }
        this.pathTiles = newPath;
        this.pathIndex = Math.max(0, data.pathIndex());
        this.pathForward = data.pathForward();

        
        List<int[]> newRoute = new ArrayList<>();
        if (data.routePathTiles() != null) {
            for (GameSnapshot.IntPair p : data.routePathTiles()) {
                if (p == null) continue;
                newRoute.add(new int[]{p.x(), p.y()});
            }
        }
        this.routePathTiles = newRoute;

        
        this.rejoiningRoute = data.rejoiningRoute();
        this.rejoinRouteAtX = data.rejoinRouteAtX();
        this.rejoinRouteAtY = data.rejoinRouteAtY();

        
        this.purchasePrice = Math.max(0, data.purchasePrice());
        this.ageSeconds = Math.max(0.0, data.ageSeconds());
        this.secondsSinceMaintenance = Math.max(0.0, data.secondsSinceMaintenance());
        this.goingToMaintenance = data.goingToMaintenance();
        this.inMaintenance = data.inMaintenance();
        this.maintenanceSecondsRemaining = Math.max(0.0, data.maintenanceSecondsRemaining());
        this.maintenanceDestRoadX = data.maintenanceDestRoadX();
        this.maintenanceDestRoadY = data.maintenanceDestRoadY();

        
        if (data.cargo() != null && data.cargo().type() != null && data.cargo().amount() > 0) {
            this.currentCargo = new Resource(data.cargo().type(), data.cargo().amount());
        } else {
            this.currentCargo = null;
        }

        
        if (data.routeBuildings() != null) {
            this.startBuildingOriginX = data.routeBuildings().startOriginX();
            this.startBuildingOriginY = data.routeBuildings().startOriginY();
            this.endBuildingOriginX = data.routeBuildings().endOriginX();
            this.endBuildingOriginY = data.routeBuildings().endOriginY();
        } else {
            this.startBuildingOriginX = null;
            this.startBuildingOriginY = null;
            this.endBuildingOriginX = null;
            this.endBuildingOriginY = null;
        }

        
        this.garage = null;
        this.maintenanceGarage = null;
        if (map != null) {
            if (data.homeGarageX() != null && data.homeGarageY() != null) {
                Tile t = map.getTile(data.homeGarageX(), data.homeGarageY());
                if (t != null && t.getPlacedBuilding() instanceof Garage g) {
                    this.garage = g;
                }
            }
            if (data.maintenanceGarageX() != null && data.maintenanceGarageY() != null) {
                Tile t = map.getTile(data.maintenanceGarageX(), data.maintenanceGarageY());
                if (t != null && t.getPlacedBuilding() instanceof Garage g) {
                    this.maintenanceGarage = g;
                }
            }
        }

        
        this.arrivedThisUpdate = false;
        this.effectiveSpeed = this.speed;
        this.intersectionClaimX = null;
        this.intersectionClaimY = null;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public int getCurrentTileX() {
        return currentTileX;
    }

    public int getCurrentTileY() {
        return currentTileY;
    }

    



    public int getRenderDirection() {
        int planned = 0;
        if (targetTileX != null && targetTileY != null) {
            planned = getPlannedDirection(targetTileX, targetTileY);
        }

        int dir = (planned != 0) ? planned : currentDirection;
        if (dir != 0) {
            lastRenderDirection = dir;
            return dir;
        }

        return lastRenderDirection;
    }

    public boolean isSpawned() {
        return targetTileX != null || (worldX != 0 || worldY != 0);
    }

    public void spawnAt(int tileX, int tileY) {
        this.currentTileX = tileX;
        this.currentTileY = tileY;
        this.targetTileX = null;
        this.targetTileY = null;
        this.previousTileX = null;
        this.previousTileY = null;
        this.lastMoveDx = 0;
        this.lastMoveDy = 0;
        this.currentDirection = 0;
        this.lastRenderDirection = 0;
        this.intersectionClaimX = null;
        this.intersectionClaimY = null;
        this.worldX = tileCenterX(tileX);
        this.worldY = tileCenterY(tileY);
    }

    



    public void setPath(List<int[]> pathTiles) {
        this.pathTiles = (pathTiles == null) ? List.of() : pathTiles;
        this.pathIndex = 0;
        this.pathForward = true;
        this.targetTileX = null;
        this.targetTileY = null;
        this.previousTileX = null;
        this.previousTileY = null;
        this.lastMoveDx = 0;
        this.lastMoveDy = 0;

        if (!this.pathTiles.isEmpty()) {
            int[] first = this.pathTiles.get(0);
            spawnAt(first[0], first[1]);
        }
    }

    public boolean hasPath() {
        return pathTiles != null && !pathTiles.isEmpty();
    }

    






    public void setRouteBuildings(int startOriginX, int startOriginY, int endOriginX, int endOriginY) {
        this.startBuildingOriginX = startOriginX;
        this.startBuildingOriginY = startOriginY;
        this.endBuildingOriginX = endOriginX;
        this.endBuildingOriginY = endOriginY;
    }

    


    public boolean servesBuilding(int originX, int originY) {
        if (startBuildingOriginX != null && startBuildingOriginY != null) {
            if (startBuildingOriginX == originX && startBuildingOriginY == originY) {
                return true;
            }
        }
        if (endBuildingOriginX != null && endBuildingOriginY != null) {
            if (endBuildingOriginX == originX && endBuildingOriginY == originY) {
                return true;
            }
        }
        return false;
    }

    public Resource getCurrentCargo() {
        return currentCargo;
    }

    




    public void update(Map map, double deltaSeconds) {
        update(map, deltaSeconds, null, null);
    }

    





    public void update(Map map, double deltaSeconds, List<Vehicle> allVehicles) {
        update(map, deltaSeconds, allVehicles, null);
    }

    






    public void update(Map map, double deltaSeconds, List<Vehicle> allVehicles, List<game.building.TrafficLight> trafficLights) {
        Objects.requireNonNull(map, "map");
        if (deltaSeconds <= 0) return;

        
        ageSeconds += deltaSeconds;
        secondsSinceMaintenance += deltaSeconds;

        
        if (inMaintenance) {
            maintenanceSecondsRemaining -= deltaSeconds;
            if (maintenanceSecondsRemaining <= 0) {
                inMaintenance = false;
                maintenanceSecondsRemaining = 0;
                secondsSinceMaintenance = 0;
                
                if (routePathTiles != null && !routePathTiles.isEmpty()) {
                    int[] join = routePathTiles.get(0);
                    List<int[]> toJoin = map.findRoadPathBetweenRoadTiles(currentTileX, currentTileY, join[0], join[1]);
                    if (!toJoin.isEmpty()) {
                        rejoiningRoute = true;
                        rejoinRouteAtX = join[0];
                        rejoinRouteAtY = join[1];
                        setPath(toJoin);
                    } else {
                        
                        switchToRouteAtCurrentTile();
                    }
                }
            }
            return;
        }

        
        if (maintenanceRequested) {
            maintenanceRequested = false;
            startGoingToNearestGarage(map);
            if (goingToMaintenance) return;
        }

        
        if (rejoiningRoute
                && rejoinRouteAtX != null && rejoinRouteAtY != null
                && currentTileX == rejoinRouteAtX && currentTileY == rejoinRouteAtY) {
            rejoiningRoute = false;
            rejoinRouteAtX = null;
            rejoinRouteAtY = null;
            switchToRouteAtCurrentTile();
        }

        
        if (!hasPath()) {
            
            maybeStartMaintenance(map);
            return;
        }

        
        if (targetTileX == null || targetTileY == null) {
            chooseNextTarget(map, allVehicles, trafficLights);
            return;
        }

        
        adjustSpeedForTraffic(allVehicles);

        double targetX = tileCenterX(targetTileX);
        double targetY = tileCenterY(targetTileY);
        double dx = targetX - worldX;
        double dy = targetY - worldY;
        double dist = Math.hypot(dx, dy);

        
        if (dist < 0.01) {
            arriveAtTarget(map, allVehicles, trafficLights);
            return;
        }

        double pixelsPerSecond = effectiveSpeed * (double) TILE_SIZE_PX;
        double step = pixelsPerSecond * deltaSeconds;
        if (step >= dist) {
            worldX = targetX;
            worldY = targetY;
            arriveAtTarget(map, allVehicles, trafficLights);
            return;
        }

        worldX += (dx / dist) * step;
        worldY += (dy / dist) * step;
    }

    private void maybeStartMaintenance(Map map) {
        if (map == null) return;
        if (goingToMaintenance || inMaintenance || rejoiningRoute) return;
        if (getSecondsUntilMaintenanceDue() > 0) return;
        startGoingToNearestGarage(map);
    }

    private void startGoingToNearestGarage(Map map) {
        List<Garage> garages = map.getGarages();
        if (garages == null || garages.isEmpty()) return;

        List<int[]> bestPath = List.of();
        Garage bestGarage = null;
        Integer bestRX = null;
        Integer bestRY = null;

        for (Garage g : garages) {
            if (g == null) continue;
            List<int[]> roads = map.adjacentRoadTilesForArea(g.getX(), g.getY(), 1, 1);
            for (int[] r : roads) {
                if (r == null || r.length < 2) continue;
                List<int[]> p = map.findRoadPathBetweenRoadTiles(currentTileX, currentTileY, r[0], r[1]);
                if (p.isEmpty()) continue;
                if (bestPath.isEmpty() || p.size() < bestPath.size()) {
                    bestPath = p;
                    bestGarage = g;
                    bestRX = r[0];
                    bestRY = r[1];
                }
            }
        }

        if (bestGarage == null || bestPath.isEmpty()) return;

        maintenanceGarage = bestGarage;
        maintenanceDestRoadX = bestRX;
        maintenanceDestRoadY = bestRY;
        goingToMaintenance = true;

        
        setPath(bestPath);
    }

    private void switchToRouteAtCurrentTile() {
        if (routePathTiles == null || routePathTiles.isEmpty()) return;

        this.pathTiles = routePathTiles;
        int idx = indexOfTile(routePathTiles, currentTileX, currentTileY);
        this.pathIndex = Math.max(0, idx);
        this.pathForward = true;
        this.targetTileX = null;
        this.targetTileY = null;
    }

    



    protected void adjustSpeedForTraffic(List<Vehicle> allVehicles) {
        effectiveSpeed = speed; 

        if (allVehicles == null || targetTileX == null || targetTileY == null) return;

        
        int myPlannedDirection = getPlannedDirection(targetTileX, targetTileY);
        if (myPlannedDirection == 0) myPlannedDirection = currentDirection;

        for (Vehicle other : allVehicles) {
            if (other == this) continue;
            if (!other.isSpawned()) continue;

            
            int otherDirection = other.currentDirection;
            if (otherDirection == 0 && other.targetTileX != null && other.targetTileY != null) {
                otherDirection = other.getPlannedDirection(other.targetTileX, other.targetTileY);
            }

            
            if (other.currentTileX == targetTileX && other.currentTileY == targetTileY) {
                if (otherDirection == myPlannedDirection && otherDirection != 0) {
                    effectiveSpeed = 0; 
                    return;
                }
            }
        }
    }

    



    public void processArrivalEconomy(Map map, Player player) {
        if (!arrivedThisUpdate) return;
        arrivedThisUpdate = false;
        if (map == null || player == null) return;

        if (!hasPath() || pathTiles.isEmpty()) return;

        
        City adjacentCity = findAdjacentCity(map, currentTileX, currentTileY);
        Industry adjacentIndustry = findAdjacentIndustry(map, currentTileX, currentTileY);

        
        if (adjacentCity == null && adjacentIndustry == null) return;

        
        City nextCity = findNextCityOnRoute(map);
        Industry nextIndustry = findNextIndustryOnRoute(map);

        
        
        
        
        if (currentCargo == null || currentCargo.isEmpty()) {
            if (adjacentIndustry != null) handleIndustryInteraction(adjacentIndustry, player, nextCity, nextIndustry);
            if (adjacentCity != null) handleCityInteraction(adjacentCity, player);
            return;
        }

        ResourceType cargoType = currentCargo.getType();
        if (cargoType == ResourceType.PASSENGERS) {
            if (adjacentCity != null) handleCityInteraction(adjacentCity, player);
            return;
        }

        if (adjacentIndustry != null && adjacentIndustry.consumes(cargoType)) {
            handleIndustryInteraction(adjacentIndustry, player, nextCity, nextIndustry);
        } else if (adjacentCity != null) {
            handleCityInteraction(adjacentCity, player);
        } else if (adjacentIndustry != null) {
            
            handleIndustryInteraction(adjacentIndustry, player, nextCity, nextIndustry);
        }
    }

    protected void arriveAtTarget(Map map) {
        arriveAtTarget(map, null, null);
    }

    protected void arriveAtTarget(Map map, List<Vehicle> allVehicles) {
        arriveAtTarget(map, allVehicles, null);
    }

    protected void arriveAtTarget(Map map, List<Vehicle> allVehicles, List<game.building.TrafficLight> trafficLights) {
        if (targetTileX == null || targetTileY == null) return;
        previousTileX = currentTileX;
        previousTileY = currentTileY;

        int newTileX = targetTileX;
        int newTileY = targetTileY;
        lastMoveDx = Integer.compare(newTileX, currentTileX);
        lastMoveDy = Integer.compare(newTileY, currentTileY);

        currentTileX = newTileX;
        currentTileY = newTileY;
        targetTileX = null;
        targetTileY = null;

        
        updateDirection();

        
        if (intersectionClaimX != null && intersectionClaimY != null) {
            if (currentTileX != intersectionClaimX || currentTileY != intersectionClaimY) {
                
                intersectionClaimX = null;
                intersectionClaimY = null;
            }
        }

        arrivedThisUpdate = true;

        
        if (goingToMaintenance
                && maintenanceDestRoadX != null && maintenanceDestRoadY != null
                && currentTileX == maintenanceDestRoadX && currentTileY == maintenanceDestRoadY) {
            goingToMaintenance = false;
            inMaintenance = true;
            maintenanceSecondsRemaining = 5.0;

            
            this.pathTiles = List.of();
            this.targetTileX = null;
            this.targetTileY = null;
            return;
        }

        
        if (getSecondsUntilMaintenanceDue() <= 0 && !goingToMaintenance && !rejoiningRoute) {
            maintenanceRequested = true;
        }
        chooseNextTarget(map, allVehicles, trafficLights);
    }

    protected void updateDirection() {
        if (lastMoveDx == 0 && lastMoveDy == -1) currentDirection = 1; 
        else if (lastMoveDx == 1 && lastMoveDy == 0) currentDirection = 2; 
        else if (lastMoveDx == 0 && lastMoveDy == 1) currentDirection = 3; 
        else if (lastMoveDx == -1 && lastMoveDy == 0) currentDirection = 4; 
        else currentDirection = 0; 
    }

    protected void chooseNextTarget(Map map) {
        chooseNextTarget(map, null, null);
    }

    protected void chooseNextTarget(Map map, List<Vehicle> allVehicles) {
        chooseNextTarget(map, allVehicles, null);
    }

    protected void chooseNextTarget(Map map, List<Vehicle> allVehicles, List<game.building.TrafficLight> trafficLights) {
        if (!hasPath()) {
            targetTileX = null;
            targetTileY = null;
            return;
        }

        
        if (pathIndex < 0) pathIndex = 0;
        if (pathIndex >= pathTiles.size()) pathIndex = pathTiles.size() - 1;

        
        int[] expected = pathTiles.get(pathIndex);
        if (expected[0] != currentTileX || expected[1] != currentTileY) {
            int idx = indexOfTile(pathTiles, currentTileX, currentTileY);
            if (idx >= 0) {
                pathIndex = idx;
            }
        }

        
        int nextIndex = pathIndex + 1;
        if (nextIndex >= pathTiles.size()) {
            
            nextIndex = 0;
        }

        if (nextIndex < 0 || nextIndex >= pathTiles.size()) {
            targetTileX = null;
            targetTileY = null;
            return;
        }

        int[] next = pathTiles.get(nextIndex);
        
        if (!isRoad(map, next[0], next[1])) {
            
            if (tryRecalculateRoute(map)) {
                
                targetTileX = null;
                targetTileY = null;
                return;
            }
            
            targetTileX = null;
            targetTileY = null;
            return;
        }

        
        int plannedDirection = getPlannedDirection(next[0], next[1]);

        
        game.building.TrafficLight lightAtNext = findTrafficLightAt(trafficLights, next[0], next[1]);
        if (lightAtNext != null) {
            
            if (!lightAtNext.isGreen(plannedDirection)) {
                
                targetTileX = null;
                targetTileY = null;
                return;
            }
        }

        
        if (allVehicles != null && isIntersection(map, next[0], next[1])) {
            
            if (lightAtNext == null && hasIntersectionConflict(next[0], next[1], allVehicles, plannedDirection)) {
                
                targetTileX = null;
                targetTileY = null;
                return;
            }
            
            if (lightAtNext == null) {
                intersectionClaimX = next[0];
                intersectionClaimY = next[1];
            }
        } else {
            
            intersectionClaimX = null;
            intersectionClaimY = null;
        }

        targetTileX = next[0];
        targetTileY = next[1];
        pathIndex = nextIndex;
    }

    


    private static game.building.TrafficLight findTrafficLightAt(List<game.building.TrafficLight> trafficLights, int x, int y) {
        if (trafficLights == null) return null;
        for (game.building.TrafficLight light : trafficLights) {
            if (light != null && light.getX() == x && light.getY() == y) {
                return light;
            }
        }
        return null;
    }

    


    protected boolean isIntersection(Map map, int x, int y) {
        if (!isRoad(map, x, y)) return false;
        return roadNeighbors(map, x, y).size() > 2;
    }

    





    protected boolean hasIntersectionConflict(int intersectionX, int intersectionY, List<Vehicle> allVehicles, int plannedDirection) {
        if (allVehicles == null) return false;
        if (plannedDirection == 0) return false;

        for (Vehicle other : allVehicles) {
            if (other == this) continue;
            if (!other.isSpawned()) continue;

            
            boolean otherInIntersection = (other.currentTileX == intersectionX && other.currentTileY == intersectionY);

            
            boolean otherClaimedIntersection = (other.intersectionClaimX != null &&
                                                 other.intersectionClaimX == intersectionX &&
                                                 other.intersectionClaimY == intersectionY);

            if (otherInIntersection || otherClaimedIntersection) {
                
                int otherDirection = other.currentDirection;

                
                if (otherDirection == 0 && other.targetTileX != null && other.targetTileY != null) {
                    otherDirection = other.getPlannedDirection(other.targetTileX, other.targetTileY);
                }

                
                if (pathsCross(plannedDirection, otherDirection)) {
                    return true; 
                }
            }
        }
        return false;
    }

    


    protected int getPlannedDirection(int nextTileX, int nextTileY) {
        int dx = nextTileX - currentTileX;
        int dy = nextTileY - currentTileY;

        if (dx == 0 && dy == -1) return 1; 
        if (dx == 1 && dy == 0) return 2; 
        if (dx == 0 && dy == 1) return 3; 
        if (dx == -1 && dy == 0) return 4; 
        return 0;
    }

    



    protected boolean pathsCross(int dir1, int dir2) {
        if (dir1 == 0 || dir2 == 0) return false;
        
        return (dir1 == 1 || dir1 == 3) && (dir2 == 2 || dir2 == 4)
            || (dir1 == 2 || dir1 == 4) && (dir2 == 1 || dir2 == 3);
    }

    private static int indexOfTile(List<int[]> tiles, int x, int y) {
        for (int i = 0; i < tiles.size(); i++) {
            int[] t = tiles.get(i);
            if (t != null && t.length >= 2 && t[0] == x && t[1] == y) return i;
        }
        return -1;
    }

    protected boolean isPrevious(int x, int y) {
        return previousTileX != null && previousTileY != null && previousTileX == x && previousTileY == y;
    }

    protected static boolean isRoad(Map map, int x, int y) {
        Tile tile = map.getTile(x, y);
        return tile != null && tile.getType() == TileType.ROAD;
    }

    protected static List<int[]> roadNeighbors(Map map, int x, int y) {
        List<int[]> result = new ArrayList<>(4);
        if (isRoad(map, x + 1, y)) result.add(new int[]{x + 1, y});
        if (isRoad(map, x - 1, y)) result.add(new int[]{x - 1, y});
        if (isRoad(map, x, y + 1)) result.add(new int[]{x, y + 1});
        if (isRoad(map, x, y - 1)) result.add(new int[]{x, y - 1});
        return result;
    }

    protected static double tileCenterX(int tileX) {
        return tileX * (double) TILE_SIZE_PX + (TILE_SIZE_PX / 2.0);
    }

    protected static double tileCenterY(int tileY) {
        return tileY * (double) TILE_SIZE_PX + (TILE_SIZE_PX / 2.0);
    }

    public void transport() {}

    public boolean needsMaintenance() {
        return getSecondsUntilMaintenanceDue() <= 0;
    }

    public void goToGarage() {}

    protected boolean canCarry(ResourceType type) {
        return true;
    }

    private void handleCityInteraction(City city, Player player) {
        if (city == null) return;

        if (currentCargo == null || currentCargo.isEmpty()) {
            
            if (!canCarry(ResourceType.PASSENGERS)) return;
            int taken = city.load(ResourceType.PASSENGERS, Math.max(0, capacity));
            if (taken > 0) {
                currentCargo = new Resource(ResourceType.PASSENGERS, taken);
            }
            return;
        }

        
        ResourceType type = currentCargo.getType();
        int amount = currentCargo.getAmount();
        if (amount <= 0) return;

        int accepted = city.deliver(type, amount);
        accepted = Math.max(0, Math.min(accepted, amount));
        if (accepted <= 0) return;

        int revenue = accepted * ResourcePrices.revenuePerUnit(type);
        if (revenue != 0) player.addMoney(revenue);
        currentCargo.removeUpTo(accepted);
        if (currentCargo.isEmpty()) currentCargo = null;
    }

    private void handleIndustryInteraction(Industry industry, Player player, City otherEndpointCity, Industry otherEndpointIndustry) {
        if (industry == null) return;

        
        if (currentCargo != null && !currentCargo.isEmpty()) {
            ResourceType type = currentCargo.getType();
            int amount = currentCargo.getAmount();

            if (amount > 0 && industry.consumes(type)) {
                industry.deliverToStorage(type, amount);
                int revenue = amount * ResourcePrices.revenuePerUnit(type);
                if (revenue != 0) player.addMoney(revenue);
                currentCargo = null;
            }
        }

        
        if (currentCargo == null || currentCargo.isEmpty()) {
            
            for (ResourceType type : industry.getProfile().getOutputsPerUnit().keySet()) {
                if (type == null) continue;
                if (type == ResourceType.PASSENGERS) continue;
                if (!canCarry(type)) continue;
                if (!canDeliverToOtherEndpoint(type, otherEndpointCity, otherEndpointIndustry)) continue;

                int taken = industry.takeFromStorage(type, Math.max(0, capacity));
                if (taken > 0) {
                    currentCargo = new Resource(type, taken);
                    return;
                }
            }
        }
    }

    private static boolean canDeliverToOtherEndpoint(ResourceType type, City otherCity, Industry otherIndustry) {
        if (type == null) return false;

        if (otherIndustry != null && otherIndustry.consumes(type)) return true;

        if (otherCity != null) {
            if (type == ResourceType.PASSENGERS) return true;
            return otherCity.getDemandBacklog().get(type) > 0;
        }

        return false;
    }

    private static City findAdjacentCity(Map map, int roadX, int roadY) {
        for (City c : map.getCities()) {
            if (c == null) continue;
            if (c.occupies(roadX + 1, roadY)
                    || c.occupies(roadX - 1, roadY)
                    || c.occupies(roadX, roadY + 1)
                    || c.occupies(roadX, roadY - 1)) {
                return c;
            }
        }
        return null;
    }

    private static Industry findAdjacentIndustry(Map map, int roadX, int roadY) {
        for (Industry i : map.getIndustries()) {
            if (i == null) continue;
            if (i.occupies(roadX + 1, roadY)
                    || i.occupies(roadX - 1, roadY)
                    || i.occupies(roadX, roadY + 1)
                    || i.occupies(roadX, roadY - 1)) {
                return i;
            }
        }
        return null;
    }

    


    private City findNextCityOnRoute(Map map) {
        if (!hasPath()) return null;
        int currentIdx = indexOfTile(pathTiles, currentTileX, currentTileY);
        if (currentIdx < 0) return null;

        
        for (int i = 1; i < pathTiles.size(); i++) {
            int idx = (currentIdx + i) % pathTiles.size();
            int[] tile = pathTiles.get(idx);
            City city = findAdjacentCity(map, tile[0], tile[1]);
            if (city != null) return city;
        }
        return null;
    }

    


    private Industry findNextIndustryOnRoute(Map map) {
        if (!hasPath()) return null;
        int currentIdx = indexOfTile(pathTiles, currentTileX, currentTileY);
        if (currentIdx < 0) return null;

        
        for (int i = 1; i < pathTiles.size(); i++) {
            int idx = (currentIdx + i) % pathTiles.size();
            int[] tile = pathTiles.get(idx);
            Industry industry = findAdjacentIndustry(map, tile[0], tile[1]);
            if (industry != null) return industry;
        }
        return null;
    }

    




    private boolean tryRecalculateRoute(Map map) {
        if (map == null) return false;
        if (routePathTiles == null || routePathTiles.isEmpty()) return false;

        
        java.util.Set<String> seenBuildings = new java.util.HashSet<>();
        java.util.List<Object> buildings = new java.util.ArrayList<>();

        for (int[] tile : routePathTiles) {
            City city = findAdjacentCity(map, tile[0], tile[1]);
            if (city != null) {
                String key = "city_" + city.getOriginX() + "_" + city.getOriginY();
                if (!seenBuildings.contains(key)) {
                    seenBuildings.add(key);
                    buildings.add(city);
                }
            }

            Industry industry = findAdjacentIndustry(map, tile[0], tile[1]);
            if (industry != null) {
                String key = "industry_" + industry.getOriginX() + "_" + industry.getOriginY();
                if (!seenBuildings.contains(key)) {
                    seenBuildings.add(key);
                    buildings.add(industry);
                }
            }
        }

        
        if (buildings.size() < 2) return false;

        
        Object nextBuilding = findNextBuildingFromCurrent(buildings);
        if (nextBuilding == null) nextBuilding = buildings.get(0);

        
        java.util.List<Object> reorderedBuildings = new java.util.ArrayList<>();
        int startIdx = buildings.indexOf(nextBuilding);
        for (int i = 0; i < buildings.size(); i++) {
            reorderedBuildings.add(buildings.get((startIdx + i) % buildings.size()));
        }

        
        Object firstBuilding = reorderedBuildings.get(0);
        int[] firstCoords = getBuildingCoords(firstBuilding);
        int[] firstSize = getBuildingSize(firstBuilding);

        java.util.List<int[]> pathToFirst = map.findRoadPathBetweenRoadTiles(
            currentTileX, currentTileY,
            firstCoords[0], firstCoords[1]
        );

        
        if (pathToFirst == null || pathToFirst.isEmpty()) {
            java.util.List<int[]> adjacentRoads = map.adjacentRoadTilesForArea(
                firstCoords[0], firstCoords[1], firstSize[0], firstSize[1]
            );
            for (int[] roadTile : adjacentRoads) {
                pathToFirst = map.findRoadPathBetweenRoadTiles(
                    currentTileX, currentTileY,
                    roadTile[0], roadTile[1]
                );
                if (pathToFirst != null && !pathToFirst.isEmpty()) break;
            }
        }

        if (pathToFirst == null || pathToFirst.isEmpty()) return false;

        
        java.util.List<int[]> fullCircularRoute = new java.util.ArrayList<>();

        for (int i = 0; i < reorderedBuildings.size(); i++) {
            Object current = reorderedBuildings.get(i);
            Object next = reorderedBuildings.get((i + 1) % reorderedBuildings.size());

            int[] currentCoords = getBuildingCoords(current);
            int[] currentSize = getBuildingSize(current);
            int[] nextCoords = getBuildingCoords(next);
            int[] nextSize = getBuildingSize(next);

            if (currentCoords == null || nextCoords == null) return false;

            java.util.List<int[]> segment = map.findRoadPathBetweenAreas(
                currentCoords[0], currentCoords[1], currentSize[0], currentSize[1],
                nextCoords[0], nextCoords[1], nextSize[0], nextSize[1]
            );

            if (segment == null || segment.isEmpty()) {
                return false;
            }

            for (int j = 0; j < segment.size(); j++) {
                if (i == 0 || j > 0) {
                    fullCircularRoute.add(segment.get(j));
                }
            }
        }

        if (fullCircularRoute.isEmpty()) return false;

        
        java.util.List<int[]> newPath = new java.util.ArrayList<>(pathToFirst);

        
        int[] lastOfPathToFirst = pathToFirst.get(pathToFirst.size() - 1);
        int connectionIdx = indexOfTile(fullCircularRoute, lastOfPathToFirst[0], lastOfPathToFirst[1]);

        if (connectionIdx >= 0) {
            
            for (int i = connectionIdx + 1; i < fullCircularRoute.size(); i++) {
                newPath.add(fullCircularRoute.get(i));
            }
            
            for (int i = 0; i <= connectionIdx; i++) {
                newPath.add(fullCircularRoute.get(i));
            }
        } else {
            
            newPath.addAll(fullCircularRoute);
        }

        
        this.routePathTiles = fullCircularRoute; 
        this.pathTiles = newPath; 

        
        this.pathIndex = 0;
        this.rejoiningRoute = true;
        this.rejoinRouteAtX = lastOfPathToFirst[0];
        this.rejoinRouteAtY = lastOfPathToFirst[1];

        return true;
    }

    


    private Object findNextBuildingFromCurrent(java.util.List<Object> buildings) {
        
        for (int i = pathIndex; i < Math.min(pathIndex + 20, pathTiles.size()); i++) {
            int[] tile = pathTiles.get(i);
            for (Object building : buildings) {
                if (isTileAdjacentToBuilding(tile[0], tile[1], building)) {
                    return building;
                }
            }
        }
        return null;
    }

    private boolean isTileAdjacentToBuilding(int tileX, int tileY, Object building) {
        if (building instanceof City c) {
            return c.occupies(tileX + 1, tileY) || c.occupies(tileX - 1, tileY) ||
                   c.occupies(tileX, tileY + 1) || c.occupies(tileX, tileY - 1);
        } else if (building instanceof Industry i) {
            return i.occupies(tileX + 1, tileY) || i.occupies(tileX - 1, tileY) ||
                   i.occupies(tileX, tileY + 1) || i.occupies(tileX, tileY - 1);
        }
        return false;
    }

    private int[] getBuildingCoords(Object building) {
        if (building instanceof City c) {
            return new int[]{c.getOriginX(), c.getOriginY()};
        } else if (building instanceof Industry i) {
            return new int[]{i.getOriginX(), i.getOriginY()};
        }
        return null;
    }

    private int[] getBuildingSize(Object building) {
        if (building instanceof City c) {
            return new int[]{c.getWidth(), c.getHeight()};
        } else if (building instanceof Industry i) {
            return new int[]{i.getWidth(), i.getHeight()};
        }
        return null;
    }
}