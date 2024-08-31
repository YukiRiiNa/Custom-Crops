package net.momirealms.customcrops.api.core.block;

import com.flowpowered.nbt.IntTag;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.action.ActionManager;
import net.momirealms.customcrops.api.context.Context;
import net.momirealms.customcrops.api.core.*;
import net.momirealms.customcrops.api.core.water.WateringMethod;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.core.world.CustomCropsWorld;
import net.momirealms.customcrops.api.core.world.Pos3;
import net.momirealms.customcrops.api.core.wrapper.WrappedBreakEvent;
import net.momirealms.customcrops.api.core.wrapper.WrappedInteractEvent;
import net.momirealms.customcrops.api.core.wrapper.WrappedPlaceEvent;
import net.momirealms.customcrops.api.event.SprinklerBreakEvent;
import net.momirealms.customcrops.api.event.SprinklerFillEvent;
import net.momirealms.customcrops.api.event.SprinklerInteractEvent;
import net.momirealms.customcrops.api.event.SprinklerPlaceEvent;
import net.momirealms.customcrops.api.requirement.RequirementManager;
import net.momirealms.customcrops.api.util.EventUtils;
import net.momirealms.customcrops.api.util.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SprinklerBlock extends AbstractCustomCropsBlock {

    public SprinklerBlock() {
        super(BuiltInBlockMechanics.SPRINKLER.key());
    }

    @Override
    public void randomTick(CustomCropsBlockState state, CustomCropsWorld<?> world, Pos3 location) {
        if (!world.setting().randomTickSprinkler() && canTick(state, world.setting().tickSprinklerInterval())) {
            tickSprinkler(state, world, location);
        }
    }

    @Override
    public void scheduledTick(CustomCropsBlockState state, CustomCropsWorld<?> world, Pos3 location) {
        if (world.setting().randomTickSprinkler() && canTick(state, world.setting().tickSprinklerInterval())) {
            tickSprinkler(state, world, location);
        }
    }

    @Override
    public void onBreak(WrappedBreakEvent event) {
        CustomCropsWorld<?> world = event.world();
        Pos3 pos3 = Pos3.from(event.location());
        SprinklerConfig config = Registries.ITEM_TO_SPRINKLER.get(event.brokenID());
        if (config == null) {
            world.removeBlockState(pos3);
            return;
        }

        final Player player = event.playerBreaker();
        Context<Player> context = Context.player(player);
        CustomCropsBlockState state = fixOrGetState(world, pos3, config, event.brokenID());
        if (!RequirementManager.isSatisfied(context, config.breakRequirements())) {
            event.setCancelled(true);
            return;
        }

        SprinklerBreakEvent breakEvent = new SprinklerBreakEvent(event.entityBreaker(), event.blockBreaker(), event.location(), state, config, event.reason());
        if (EventUtils.fireAndCheckCancel(breakEvent)) {
            event.setCancelled(true);
            return;
        }

        world.removeBlockState(pos3);
        ActionManager.trigger(context, config.breakActions());
    }

    @Override
    public void onPlace(WrappedPlaceEvent event) {
        SprinklerConfig config = Registries.ITEM_TO_SPRINKLER.get(event.placedID());
        if (config == null) {
            event.setCancelled(true);
            return;
        }

        final Player player = event.player();
        Context<Player> context = Context.player(player);
        if (!RequirementManager.isSatisfied(context, config.placeRequirements())) {
            event.setCancelled(true);
            return;
        }

        Pos3 pos3 = Pos3.from(event.location());
        CustomCropsWorld<?> world = event.world();
        if (world.setting().sprinklerPerChunk() >= 0) {
            if (world.testChunkLimitation(pos3, this.getClass(), world.setting().sprinklerPerChunk())) {
                event.setCancelled(true);
                ActionManager.trigger(context, config.reachLimitActions());
                return;
            }
        }

        CustomCropsBlockState state = createBlockState();
        id(state, config.id());
        water(state, config.threeDItemWithWater().equals(event.placedID()) ? 1 : 0);

        SprinklerPlaceEvent placeEvent = new SprinklerPlaceEvent(player, event.item(), event.hand(), event.location(), config, state);
        if (EventUtils.fireAndCheckCancel(placeEvent)) {
            event.setCancelled(true);
            return;
        }

        world.addBlockState(pos3, state);
        ActionManager.trigger(context, config.placeActions());
    }

    @Override
    public void onInteract(WrappedInteractEvent event) {
        SprinklerConfig config = Registries.ITEM_TO_SPRINKLER.get(event.relatedID());
        if (config == null) {
            return;
        }

        final Player player = event.player();
        Context<Player> context = Context.player(player);
        CustomCropsBlockState state = fixOrGetState(event.world(), Pos3.from(event.location()), config, event.relatedID());
        if (!RequirementManager.isSatisfied(context, config.useRequirements())) {
            return;
        }

        int waterInSprinkler = water(state);
        String itemID = event.itemID();
        ItemStack itemInHand = event.itemInHand();
        if (!config.infinite()) {
            for (WateringMethod method : config.wateringMethods()) {
                if (method.getUsed().equals(itemID) && method.getUsedAmount() <= itemInHand.getAmount()) {
                    if (method.checkRequirements(context)) {
                        if (waterInSprinkler >= config.storage()) {
                            ActionManager.trigger(context, config.fullWaterActions());
                        } else {
                            SprinklerFillEvent waterEvent = new SprinklerFillEvent(player, itemInHand, event.hand(), event.location(), method, state, config);
                            if (EventUtils.fireAndCheckCancel(waterEvent))
                                return;
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                itemInHand.setAmount(Math.max(0, itemInHand.getAmount() - method.getUsedAmount()));
                                if (method.getReturned() != null) {
                                    ItemStack returned = BukkitCustomCropsPlugin.getInstance().getItemManager().build(player, method.getReturned());
                                    if (returned != null) {
                                        PlayerUtils.giveItem(player, returned, method.getReturnedAmount());
                                    }
                                }
                            }
                            method.triggerActions(context);
                            ActionManager.trigger(context, config.addWaterActions());
                        }
                    }
                    return;
                }
            }
        }

        SprinklerInteractEvent interactEvent = new SprinklerInteractEvent(player, event.itemInHand(), event.location(), config, state, event.hand());
        if (EventUtils.fireAndCheckCancel(interactEvent)) {
            return;
        }

        ActionManager.trigger(context, config.interactActions());
    }

    public CustomCropsBlockState fixOrGetState(CustomCropsWorld<?> world, Pos3 pos3, SprinklerConfig sprinklerConfig, String blockID) {
        Optional<CustomCropsBlockState> optionalPotState = world.getBlockState(pos3);
        if (optionalPotState.isPresent()) {
            CustomCropsBlockState potState = optionalPotState.get();
            if (potState.type() instanceof SprinklerBlock sprinklerBlock) {
                if (sprinklerBlock.id(potState).equals(sprinklerConfig.id())) {
                    return potState;
                }
            }
        }
        CustomCropsBlockState state = BuiltInBlockMechanics.SPRINKLER.createBlockState();
        id(state, sprinklerConfig.id());
        water(state, blockID.equals(sprinklerConfig.threeDItemWithWater()) ? 1 : 0);
        world.addBlockState(pos3, state).ifPresent(previous -> {
            BukkitCustomCropsPlugin.getInstance().debug(
                    "Overwrite old data with " + state.compoundMap().toString() +
                            " at location[" + world.worldName() + "," + pos3 + "] which used to be " + previous.compoundMap().toString()
            );
        });
        return state;
    }

    private void tickSprinkler(CustomCropsBlockState state, CustomCropsWorld<?> world, Pos3 location) {
        SprinklerConfig config = config(state);
        if (config == null) {
            BukkitCustomCropsPlugin.getInstance().getPluginLogger().warn("Sprinkler data is removed at location[" + world.worldName() + "," + location + "] because the sprinkler config[" + id(state) + "] has been removed.");
            world.removeBlockState(location);
            return;
        }
        boolean updateState;
        if (!config.infinite()) {
            int water = water(state);
            if (water <= 0) {
                return;
            }
            water(state, --water);
            updateState = water == 0;
        } else {
            updateState = false;
        }

        Context<CustomCropsBlockState> context = Context.block(state);
        World bukkitWorld = world.bukkitWorld();
        Location bukkitLocation = location.toLocation(bukkitWorld);

        CompletableFuture<Boolean> syncCheck = new CompletableFuture<>();

        // place/remove entities on main thread
        BukkitCustomCropsPlugin.getInstance().getScheduler().sync().run(() -> {

            if (ConfigManager.doubleCheck()) {
                String modelID = BukkitCustomCropsPlugin.getInstance().getItemManager().id(bukkitLocation, config.existenceForm());
                if (modelID == null || !config.modelIDs().contains(modelID)) {
                    world.removeBlockState(location);
                    BukkitCustomCropsPlugin.getInstance().getPluginLogger().warn("Sprinkler[" + config.id() + "] is removed at Location["  +  world.worldName() + "," + location + "] because the id of the block/furniture is " + modelID);
                    syncCheck.complete(false);
                    return;
                }
            }

            ActionManager.trigger(context, config.workActions());
            if (updateState && !config.threeDItem().equals(config.threeDItemWithWater())) {
                updateBlockAppearance(bukkitLocation, config, false);
            }

            syncCheck.complete(true);
        }, bukkitLocation);

        syncCheck.thenAccept(result -> {
           if (result) {
               int[][] range = config.range();
               Pos3[] pos3s = new Pos3[range.length * 2];
               for (int i = 0; i < range.length; i++) {
                   int x = range[i][0];
                   int z = range[i][1];
                   pos3s[i] = location.add(x, 0, z);
                   pos3s[i] = location.add(x, -1, z);
               }

               for (Pos3 pos3 : pos3s) {
                   Optional<CustomCropsBlockState> optionalState = world.getBlockState(pos3);
                   if (optionalState.isPresent()) {
                       CustomCropsBlockState anotherState = optionalState.get();
                       if (anotherState.type() instanceof PotBlock potBlock) {
                           PotConfig potConfig = potBlock.config(anotherState);
                           if (config.potWhitelist().contains(potConfig.id())) {
                               if (potBlock.addWater(anotherState, potConfig, config.sprinklingAmount())) {
                                   BukkitCustomCropsPlugin.getInstance().getScheduler().sync().run(
                                           () -> potBlock.updateBlockAppearance(
                                                   pos3.toLocation(world.bukkitWorld()),
                                                   potConfig,
                                                   true,
                                                   potBlock.fertilizers(anotherState)
                                           ),
                                           bukkitWorld,
                                           pos3.chunkX(), pos3.chunkZ()
                                   );
                               }
                           }
                       }
                   }
               }
           }
        });
    }

    public boolean addWater(CustomCropsBlockState state, int water) {
        return water(state, water + water(state));
    }

    public boolean addWater(CustomCropsBlockState state, SprinklerConfig config, int water) {
        return water(state, config, water + water(state));
    }

    public int water(CustomCropsBlockState state) {
        return state.get("water").getAsIntTag().map(IntTag::getValue).orElse(0);
    }

    public boolean water(CustomCropsBlockState state, int water) {
        return water(state, config(state), water);
    }

    public boolean water(CustomCropsBlockState state, SprinklerConfig config, int water) {
        if (water < 0) water = 0;
        int current = Math.min(water, config.storage());
        int previous = water(state);
        if (water == previous) return false;
        state.set("water", new IntTag("water", current));
        return previous == 0 ^ current == 0;
    }

    public SprinklerConfig config(CustomCropsBlockState state) {
        return Registries.SPRINKLER.get(id(state));
    }

    public void updateBlockAppearance(Location location, SprinklerConfig config, boolean hasWater) {
        FurnitureRotation rotation = BukkitCustomCropsPlugin.getInstance().getItemManager().remove(location, ExistenceForm.ANY);
        BukkitCustomCropsPlugin.getInstance().getItemManager().place(location, config.existenceForm(), hasWater ? config.threeDItemWithWater() : config.threeDItem(), rotation);
    }
}