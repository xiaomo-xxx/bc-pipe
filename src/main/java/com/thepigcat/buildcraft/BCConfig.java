package com.thepigcat.buildcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = BuildcraftLegacy.MODID)
public final class BCConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // === Fluid ===
    private static final ModConfigSpec.IntValue TANK_CAPACITY = BUILDER
            .comment("储罐容量 (mB)")
            .defineInRange("tankCapacity", 8000, 1, 1_000_000);

    private static final ModConfigSpec.IntValue COMBUSTION_ENGINE_FLUID_CAPACITY = BUILDER
            .comment("燃烧引擎流体容量 (mB)")
            .defineInRange("combustionEngineFluidCapacity", 2000, 1, 100_000);

    // === Energy ===
    private static final ModConfigSpec.IntValue REDSTONE_ENGINE_ENERGY_CAPACITY = BUILDER
            .comment("红石引擎能量容量")
            .defineInRange("redstoneEngineEnergyCapacity", 1000, 1, 1_000_000);

    private static final ModConfigSpec.IntValue STIRLING_ENGINE_ENERGY_CAPACITY = BUILDER
            .comment("斯特林引擎能量容量")
            .defineInRange("stirlingEngineEnergyCapacity", 5000, 1, 1_000_000);

    private static final ModConfigSpec.IntValue COMBUSTION_ENGINE_ENERGY_CAPACITY = BUILDER
            .comment("燃烧引擎能量容量")
            .defineInRange("combustionEngineEnergyCapacity", 10000, 1, 10_000_000);

    private static final ModConfigSpec.IntValue REDSTONE_ENGINE_ENERGY_PRODUCTION = BUILDER
            .comment("红石引擎每tick能量产出")
            .defineInRange("redstoneEngineEnergyProduction", 1, 0, 10000);

    private static final ModConfigSpec.IntValue STIRLING_ENGINE_ENERGY_PRODUCTION = BUILDER
            .comment("斯特林引擎每tick能量产出")
            .defineInRange("stirlingEngineEnergyProduction", 5, 0, 10000);

    private static final ModConfigSpec.IntValue COMBUSTION_ENGINE_ENERGY_PRODUCTION = BUILDER
            .comment("燃烧引擎每tick能量产出")
            .defineInRange("combustionEngineEnergyProduction", 20, 0, 100000);

    // === Items ===
    private static final ModConfigSpec.IntValue CRATE_ITEM_CAPACITY = BUILDER
            .comment("板条箱容量")
            .defineInRange("crateItemCapacity", 4096, 1, 1_000_000);

    private static final ModConfigSpec.BooleanValue TANK_RETAIN_FLUIDS = BUILDER
            .comment("破坏储罐时保留流体")
            .define("tankRetainFluids", true);

    private static final ModConfigSpec.BooleanValue CRATE_RETAIN_ITEMS = BUILDER
            .comment("破坏板条箱时保留物品")
            .define("crateRetainItems", true);

    // === Pipe Speeds (blocks per second) ===
    private static final ModConfigSpec.DoubleValue BASIC_PIPE_SPEED = BUILDER
            .comment("基础管道速度 (每秒通过多少个管道)",
                     "圆石/石/石英/砂岩/铁/粘土管道",
                     "默认: 0.5 = 2秒通过1段管道")
            .defineInRange("basicPipeSpeed", 0.5, 0.01, 20.0);

    private static final ModConfigSpec.DoubleValue WOODEN_PIPE_SPEED = BUILDER
            .comment("木质管道速度 (每秒通过多少个管道)",
                     "默认: 0.5 = 2秒通过1段管道")
            .defineInRange("woodenPipeSpeed", 0.5, 0.01, 20.0);

    private static final ModConfigSpec.DoubleValue GOLD_PIPE_SPEED = BUILDER
            .comment("金质管道速度 (每秒通过多少个管道)",
                     "默认: 1.0 = 1秒通过1段管道")
            .defineInRange("goldPipeSpeed", 1.0, 0.01, 20.0);

    private static final ModConfigSpec.DoubleValue DIAMOND_PIPE_SPEED = BUILDER
            .comment("钻石管道速度 (每秒通过多少个管道)",
                     "默认: 0.67 = 1.5秒通过1段管道")
            .defineInRange("diamondPipeSpeed", 0.67, 0.01, 20.0);

    private static final ModConfigSpec.DoubleValue VOID_PIPE_SPEED = BUILDER
            .comment("虚空管道速度 (每秒通过多少个管道)",
                     "默认: 1.0 = 1秒销毁物品")
            .defineInRange("voidPipeSpeed", 1.0, 0.01, 20.0);

    private static final ModConfigSpec.DoubleValue EMERALD_PIPE_SPEED = BUILDER
            .comment("绿宝石管道速度 (每秒通过多少个管道)",
                     "快速抽取管道",
                     "默认: 1.25 = 0.8秒通过1段管道")
            .defineInRange("emeraldPipeSpeed", 1.25, 0.01, 20.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    // === Runtime values (updated on config load/reload) ===
    public static int tankCapacity;
    public static int combustionEngineFluidCapacity;
    public static int redstoneEngineEnergyCapacity;
    public static int stirlingEngineEnergyCapacity;
    public static int combustionEngineEnergyCapacity;
    public static int redstoneEngineEnergyProduction;
    public static int stirlingEngineEnergyProduction;
    public static int combustionEngineEnergyProduction;
    public static int crateItemCapacity;
    public static boolean tankRetainFluids;
    public static boolean crateRetainItems;
    public static double basicPipeSpeed;
    public static double woodenPipeSpeed;
    public static double goldPipeSpeed;
    public static double diamondPipeSpeed;
    public static double voidPipeSpeed;
    public static double emeraldPipeSpeed;

    @SubscribeEvent
    static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            sync();
        }
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            sync();
        }
    }

    private static void sync() {
        tankCapacity = TANK_CAPACITY.get();
        combustionEngineFluidCapacity = COMBUSTION_ENGINE_FLUID_CAPACITY.get();
        redstoneEngineEnergyCapacity = REDSTONE_ENGINE_ENERGY_CAPACITY.get();
        stirlingEngineEnergyCapacity = STIRLING_ENGINE_ENERGY_CAPACITY.get();
        combustionEngineEnergyCapacity = COMBUSTION_ENGINE_ENERGY_CAPACITY.get();
        redstoneEngineEnergyProduction = REDSTONE_ENGINE_ENERGY_PRODUCTION.get();
        stirlingEngineEnergyProduction = STIRLING_ENGINE_ENERGY_PRODUCTION.get();
        combustionEngineEnergyProduction = COMBUSTION_ENGINE_ENERGY_PRODUCTION.get();
        crateItemCapacity = CRATE_ITEM_CAPACITY.get();
        tankRetainFluids = TANK_RETAIN_FLUIDS.get();
        crateRetainItems = CRATE_RETAIN_ITEMS.get();
        basicPipeSpeed = BASIC_PIPE_SPEED.get();
        woodenPipeSpeed = WOODEN_PIPE_SPEED.get();
        goldPipeSpeed = GOLD_PIPE_SPEED.get();
        diamondPipeSpeed = DIAMOND_PIPE_SPEED.get();
        voidPipeSpeed = VOID_PIPE_SPEED.get();
        emeraldPipeSpeed = EMERALD_PIPE_SPEED.get();
    }
}
