package game.save;

import game.map.IndustryType;
import game.resource.ResourceType;

import java.util.List;
import java.util.Map;






public record GameSnapshot(
        int version,
        String name,
        long savedAtEpochMillis,
        PlayerData player,
        TimeData time,
        MapData map,
        List<VehicleData> vehicles,
        List<TrafficLightData> trafficLights
) {

        public static final int CURRENT_VERSION = 3;

    public record PlayerData(int money) {}

    public record TimeData(String speed, long totalTicks, double gameTimeSeconds) {}

    public record MapData(
            int width,
            int height,
            List<CityData> cities,
            List<IndustryData> industries,
            List<IntPair> roads,
            List<BuildingData> buildings,
            List<ForestData> forests
    ) {}

    



    public record ForestData(int x, int y, int trees) {}

    public record CityData(
            String name,
            int originX,
            int originY,
            int width,
            int height,
            Map<ResourceType, Integer> waiting,
            Map<ResourceType, Integer> demandBacklog
    ) {}

    public record IndustryData(
            String name,
            IndustryType type,
            int originX,
            int originY,
            int width,
            int height,
            Map<ResourceType, Integer> storage
    ) {}

    public record BuildingData(String type, int x, int y) {}

    public record TrafficLightData(
            int x,
            int y,
            String state,
            double timeInState,
            double greenDurationMain,
            double greenDurationCross
    ) {}

    public record CargoData(ResourceType type, int amount) {}

    public record RouteBuildingsData(int startOriginX, int startOriginY, int endOriginX, int endOriginY) {}

    public record VehicleData(
            String kind,
            double worldX,
            double worldY,
            int currentTileX,
            int currentTileY,
            Integer targetTileX,
            Integer targetTileY,
            Integer previousTileX,
            Integer previousTileY,
            int lastMoveDx,
            int lastMoveDy,
            int currentDirection,
            List<IntPair> pathTiles,
            int pathIndex,
            boolean pathForward,
            CargoData cargo,
            RouteBuildingsData routeBuildings,
            List<IntPair> routePathTiles,
            boolean rejoiningRoute,
            Integer rejoinRouteAtX,
            Integer rejoinRouteAtY,
            double ageSeconds,
            double secondsSinceMaintenance,
            boolean goingToMaintenance,
            boolean inMaintenance,
            double maintenanceSecondsRemaining,
            Integer maintenanceDestRoadX,
            Integer maintenanceDestRoadY,
            Integer homeGarageX,
            Integer homeGarageY,
            Integer maintenanceGarageX,
            Integer maintenanceGarageY,
            int purchasePrice
    ) {}

    public record IntPair(int x, int y) {}
}
