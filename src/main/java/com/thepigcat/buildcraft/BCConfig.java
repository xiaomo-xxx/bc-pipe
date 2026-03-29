package com.thepigcat.buildcraft;

import com.portingdeadmods.portingdeadlibs.api.config.ConfigValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public final class BCConfig {
    @ConfigValue(name = "Tank Capacity", comment = "The maximum amount of fluid a tank can store", category = "capacity.fluid")
    public static int tankCapacity = 8000;

    @ConfigValue(name = "Combustion Engine Fluid Capacity", comment = "The maximum amount of fluid a combustion engine can store", category = "capacity.fluid")
    public static int combustionEngineFluidCapacity = 2000;

    @ConfigValue(name = "Tank Retain Fluids", comment = "Whether the Fluid Tank retains its contents after being broken")
    public static boolean tankRetainFluids = true;
    @ConfigValue(name = "Create Retain Items", comment = "Whether the Crate retains its contents after being broken")
    public static boolean crateRetainItems = true;

    @ConfigValue(name = "Redstone Engine Energy Capacity", comment = "The maximum amount of energy a redstone engine can store", category = "capacity.energy")
    public static int redstoneEngineEnergyCapacity = 1000;
    @ConfigValue(name = "Stirling Engine Energy Capacity", comment = "The maximum amount of energy a stirling engine can store", category = "capacity.energy")
    public static int stirlingEngineEnergyCapacity = 5000;
    @ConfigValue(name = "Combustion Engine Energy Capacity", comment = "The maximum amount of energy a combustion engine can store", category = "capacity.energy")
    public static int combustionEngineEnergyCapacity = 10_000;

    @ConfigValue(name = "Redstone Engine Energy Production", comment = "The amount of energy a redstone engine produces", category = "production.energy")
    public static int redstoneEngineEnergyProduction;
    @ConfigValue(name = "Stirling Engine Energy Production", comment = "The amount of energy a stirling engine produces", category = "production.energy")
    public static int stirlingEngineEnergyProduction;
    @ConfigValue(name = "Combustion Engine Energy Production", comment = "The amount of energy a combustion engine produces", category = "production.energy")
    public static int combustionEngineEnergyProduction;

    @ConfigValue(name = "Crate Item Capacity", comment = "The maximum amount of items the crate can store", category = "capacity.items")
    public static int crateItemCapacity = 4096;

    // === Pipe Transfer Speeds (per tick, item moves when progress >= 1.0) ===
    @ConfigValue(name = "Basic Pipe Speed", comment = "Transfer speed for basic pipes (cobblestone, stone, quartz, sandstone, iron, clay). 0.01 = 5 seconds per pipe", category = "pipes.speed")
    public static float basicPipeSpeed = 0.01f;

    @ConfigValue(name = "Wooden Pipe Speed", comment = "Transfer speed for wooden extracting pipes. 0.0125 = 4 seconds per pipe", category = "pipes.speed")
    public static float woodenPipeSpeed = 0.0125f;

    @ConfigValue(name = "Gold Pipe Speed", comment = "Transfer speed for gold pipes (fast). 0.02 = 2.5 seconds per pipe", category = "pipes.speed")
    public static float goldPipeSpeed = 0.02f;

    @ConfigValue(name = "Diamond Pipe Speed", comment = "Transfer speed for diamond extracting pipes. 0.015 = 3.3 seconds per pipe", category = "pipes.speed")
    public static float diamondPipeSpeed = 0.015f;

    @ConfigValue(name = "Void Pipe Speed", comment = "Transfer speed for void pipes (destroys items). 0.05 = 1 second", category = "pipes.speed")
    public static float voidPipeSpeed = 0.05f;
}
