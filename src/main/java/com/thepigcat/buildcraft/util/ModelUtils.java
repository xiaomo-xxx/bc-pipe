package com.thepigcat.buildcraft.util;

import com.thepigcat.buildcraft.PipesRegistry;
import com.thepigcat.buildcraft.api.pipes.Pipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public final class ModelUtils {
    public static final BiFunction<Pipe, ResourceLocation, String> DEFAULT_BLOCK_MODEL_DEFINITION = (pipe, pipeId) -> {
        String pipeIdLiteral = pipeId.withPrefix("block/").toString();
        return """
                {"multipart":[{"apply":{"model":"%s_connection"},"when":{"down":"connected"}},
                {"apply":{"model":"%s_connection","x":180},"when":{"up":"connected"}},
                {"apply":{"model":"%s_connection","x":90,"y":180},"when":{"north":"connected"}},
                {"apply":{"model":"%s_connection","x":90,"y":270},"when":{"east":"connected"}},
                {"apply":{"model":"%s_connection","x":90},"when":{"south":"connected"}},
                {"apply":{"model":"%s_connection","x":90,"y":90},"when":{"west":"connected"}},
                {"apply":{"model":"%s_base"}}]}""".formatted(pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral);
    };
    public static final BiFunction<Pipe, ResourceLocation, String> EXTRACTING_BLOCK_MODEL_DEFINITION = (pipe, pipeId) -> {
        String pipeIdLiteral = pipeId.withPrefix("block/").toString();
        return """
                {
                  "multipart": [
                    {
                      "apply": {
                        "model": "%s_connection"
                      },
                      "when": {
                        "down": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting"
                      },
                      "when": {
                        "down": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection",
                        "x": 180
                      },
                      "when": {
                        "up": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting",
                        "x": 180
                      },
                      "when": {
                        "up": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection",
                        "x": 90,
                        "y": 180
                      },
                      "when": {
                        "north": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting",
                        "x": 90,
                        "y": 180
                      },
                      "when": {
                        "north": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection",
                        "x": 90,
                        "y": 270
                      },
                      "when": {
                        "east": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting",
                        "x": 90,
                        "y": 270
                      },
                      "when": {
                        "east": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection",
                        "x": 90
                      },
                      "when": {
                        "south": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting",
                        "x": 90
                      },
                      "when": {
                        "south": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection",
                        "x": 90,
                        "y": 90
                      },
                      "when": {
                        "west": "connected"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_connection_extracting",
                        "x": 90,
                        "y": 90
                      },
                      "when": {
                        "west": "extracting"
                      }
                    },
                    {
                      "apply": {
                        "model": "%s_base"
                      }
                    }
                  ]
                }""".formatted(pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral, pipeIdLiteral);
    };

    public static final BiFunction<Pipe, String, String> DEFAULT_ITEM_MODEL_FILE = ((pipe, pipeId) -> {
        List<ResourceLocation> textures = PipesRegistry.PIPES.get(pipeId).textures();
        return """
                {
                  "parent": "buildcraft:item/pipe_inventory",
                  "textures": {
                    "texture": "%s"
                  }
                }""".formatted(!textures.isEmpty() ? textures.getFirst().toString() : "missing");
    });

    public static final BiFunction<Pipe, ResourceLocation, String> DEFAULT_BLOCK_MODEL_FILE = ((pipe, texture) -> {
        String path = texture.getPath();

        // Diamond pipe uses per-face textures for the base model.
        // 刻意写成「不依赖父模型」的自包含模型：之前用 pipe_base_colored 作 parent 时
        // 方块上看不到任何方向色，怀疑是父模型解析/覆盖的问题，这里把 elements 内联，
        // 只要模型文件被加载就一定会按六面贴图渲染。
        if (path.endsWith("_base") && path.contains("diamond")) {
            String blockPath = path.replace("_base", "").replace("block/", "");
            return """
                    {
                      "render_type": "cutout",
                      "textures": {
                        "particle": "buildcraft:block/%s",
                        "down": "buildcraft:block/%s_down",
                        "up": "buildcraft:block/%s_up",
                        "north": "buildcraft:block/%s_north",
                        "south": "buildcraft:block/%s_south",
                        "west": "buildcraft:block/%s_west",
                        "east": "buildcraft:block/%s_east"
                      },
                      "elements": [
                        {
                          "from": [4, 4, 4],
                          "to": [12, 12, 12],
                          "faces": {
                            "down": { "texture": "#down" },
                            "up": { "texture": "#up" },
                            "north": { "texture": "#north" },
                            "south": { "texture": "#south" },
                            "west": { "texture": "#west" },
                            "east": { "texture": "#east" }
                          }
                        }
                      ]
                    }""".formatted(blockPath, blockPath, blockPath, blockPath, blockPath, blockPath, blockPath);
        }

        // 连接臂不再用方向色：臂的模型在 blockstate 里带旋转，套方向色会被转乱，
        // 而且整根臂涂成高饱和色看着很花。只让管体六个面保留方向色，臂用中性材质。
        // （下面走到通用分支，用 textures[0] 即 diamond_pipe 本身）

        // Diamond pipe extracting connection uses the extracting texture per-face
        if (path.contains("diamond") && path.endsWith("_connection_extracting")) {
            String blockPath = path.replace("_connection_extracting", "").replace("block/", "");
            return """
                    {
                      "parent": "buildcraft:block/pipe_connection_colored",
                      "textures": {
                        "down": "buildcraft:block/%s_extracting",
                        "up": "buildcraft:block/%s_extracting",
                        "north": "buildcraft:block/%s_extracting",
                        "south": "buildcraft:block/%s_extracting",
                        "west": "buildcraft:block/%s_extracting",
                        "east": "buildcraft:block/%s_extracting"
                      }
                    }""".formatted(blockPath, blockPath, blockPath, blockPath, blockPath, blockPath);
        }

        int textureIndex = 0;
        String parent = "";
        if (path.endsWith("_connection")) {
            parent = "buildcraft:block/pipe_connection";
        } else if (path.endsWith("_base")) {
            parent = "buildcraft:block/pipe_base";
        } else if (path.endsWith("_connection_extracting")) {
            textureIndex = 1;
            parent = "buildcraft:block/pipe_connection";
        }
        return """
                {
                  "parent": "%s",
                  "textures": {
                    "texture": "%s"
                  }
                }""".formatted(parent, pipe.textures().size() > textureIndex ? pipe.textures().get(textureIndex) : "missing");
    });

    public static ResourceLocation modelLocationToBlockId(ResourceLocation modelLocation) {
        String[] split = modelLocation.getPath().split("/");
        String name = split[split.length - 1];
        return ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), name);
    }

    public enum ParentType implements StringRepresentable {
        BASE("base"),
        CONNECTION("connection");

        private final String parentType;

        ParentType(String parentType) {
            this.parentType = parentType;
        }

        @Override
        public @NotNull String getSerializedName() {
            return parentType;
        }
    }

}
