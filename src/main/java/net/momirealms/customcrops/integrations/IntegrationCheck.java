package net.momirealms.customcrops.integrations;

import net.momirealms.customcrops.datamanager.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IntegrationCheck {

    //收获权限检测
    public static boolean HarvestCheck(Location location, Player player){
        if(ConfigManager.Config.res){
            if(!ResidenceIntegrations.checkResHarvest(location, player)){
                return false;
            }
        }
        if(ConfigManager.Config.king){
            if(!KingdomsXIntegrations.checkKDBuild(location, player)){
                return false;
            }
        }
        if(ConfigManager.Config.wg){
            if(!WorldGuardIntegrations.checkWGHarvest(location, player)){
                return false;
            }
        }
        if(ConfigManager.Config.gd){
            if(!GriefDefenderIntegrations.checkGDBreak(location, player)){
                return false;
            }
        }
        return true;
    }
    //种植等权限检测
    public static boolean PlaceCheck(Location location, Player player){
        if(ConfigManager.Config.res){
            if(!ResidenceIntegrations.checkResBuild(location,player)){
                return false;
            }
        }
        if(ConfigManager.Config.king){
            if(!KingdomsXIntegrations.checkKDBuild(location,player)){
                return false;
            }
        }
        if(ConfigManager.Config.wg){
            if(!WorldGuardIntegrations.checkWGBuild(location, player)){
                return false;
            }
        }
        if(ConfigManager.Config.gd){
            if(!GriefDefenderIntegrations.checkGDBuild(location, player)){
                return false;
            }
        }
        return true;
    }
}