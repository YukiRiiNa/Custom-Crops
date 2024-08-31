package net.momirealms.customcrops.api.core.block;

import net.momirealms.customcrops.api.action.Action;
import net.momirealms.customcrops.api.core.item.FertilizerType;
import net.momirealms.customcrops.api.core.water.WateringMethod;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.misc.WaterBar;
import net.momirealms.customcrops.api.requirement.Requirement;
import net.momirealms.customcrops.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;

public interface PotConfig {

    String id();

    int storage();

    boolean isRainDropAccepted();

    boolean isNearbyWaterAccepted();

    WateringMethod[] wateringMethods();

    Set<String> blocks();

    boolean isWet(String blockID);

    WaterBar waterBar();

    int maxFertilizers();

    String getPotAppearance(boolean watered, FertilizerType type);

    Requirement<Player>[] placeRequirements();

    Requirement<Player>[] breakRequirements();

    Requirement<Player>[] useRequirements();

    Action<CustomCropsBlockState>[] tickActions();

    Action<Player>[] reachLimitActions();

    Action<Player>[] interactActions();

    Action<Player>[] placeActions();

    Action<Player>[] breakActions();

    Action<Player>[] addWaterActions();

    Action<Player>[] fullWaterActions();

    static Builder builder() {
        return new PotConfigImpl.BuilderImpl();
    }

    interface Builder {

        PotConfig build();

        Builder id(String id);

        Builder storage(int storage);

        Builder isRainDropAccepted(boolean isRainDropAccepted);

        Builder isNearbyWaterAccepted(boolean isNearbyWaterAccepted);

        Builder wateringMethods(WateringMethod[] wateringMethods);

        Builder waterBar(WaterBar waterBar);

        Builder maxFertilizers(int maxFertilizers);

        Builder placeRequirements(Requirement<Player>[] requirements);

        Builder breakRequirements(Requirement<Player>[] requirements);

        Builder useRequirements(Requirement<Player>[] requirements);

        Builder tickActions(Action<CustomCropsBlockState>[] tickActions);

        Builder reachLimitActions(Action<Player>[] reachLimitActions);

        Builder interactActions(Action<Player>[] interactActions);

        Builder placeActions(Action<Player>[] placeActions);

        Builder breakActions(Action<Player>[] breakActions);

        Builder addWaterActions(Action<Player>[] addWaterActions);

        Builder fullWaterActions(Action<Player>[] fullWaterActions);

        Builder basicAppearance(Pair<String, String> basicAppearance);

        Builder potAppearanceMap(HashMap<FertilizerType, Pair<String, String>> potAppearanceMap);
    }
}