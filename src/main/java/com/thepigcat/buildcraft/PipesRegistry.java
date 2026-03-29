// Thanks to BlakeBro and IronJetpacks for part of this code <3

package com.thepigcat.buildcraft;

import com.google.common.base.Stopwatch;
import com.google.gson.*;
import com.thepigcat.buildcraft.api.pipes.Pipe;
import com.thepigcat.buildcraft.api.pipes.PipeHolder;
import com.thepigcat.buildcraft.registries.BCPipes;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class PipesRegistry {
    public static final Map<String, Pipe> PIPES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean isErrored = false;

    /**
     * Writes default pipe JSON files for any pipes that don't have a config file yet.
     * This ensures new pipes added in updates are available in-game.
     */
    public static void writeDefaultPipeFiles() {
        File dir = getPipesDirectory();

        // Create directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.isDirectory()) {
            BuildcraftLegacy.LOGGER.error("Failed to create pipes config directory: {}", dir.getAbsolutePath());
            return;
        }

        Map<String, Pipe> pipes = BCPipes.HELPER.getPipes();
        for (Map.Entry<String, Pipe> entry : pipes.entrySet()) {
            File file = new File(dir, entry.getKey() + ".json");
            // Only write if the file doesn't exist yet (new pipes from updates)
            if (!file.exists()) {
                JsonElement json = entry.getValue().toJson();
                if (json == null) {
                    BuildcraftLegacy.LOGGER.error("Pipe {} could not be written to Json due to encoding error", entry.getKey());
                    continue;
                }

                try (FileWriter writer = new FileWriter(file)) {
                    GSON.toJson(json, writer);
                    BuildcraftLegacy.LOGGER.info("Created pipe config: {}", entry.getKey());
                } catch (Exception e) {
                    BuildcraftLegacy.LOGGER.error("An error occurred while generating pipe jsons, affected pipe: {}", entry.getKey(), e);
                }
            }
        }
    }

    public static void loadPipes() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        File dir = getPipesDirectory();

        writeDefaultPipeFiles();

        PIPES.clear();

        if (dir.isDirectory()) {
            loadFiles(dir);
        }

        stopwatch.stop();

        BuildcraftLegacy.LOGGER.info("Loaded {} pipe type(s) in {} ms", PIPES.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private static void loadFiles(File dir) {
        var files = dir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".json"));

        if (files == null)
            return;

        List<PipeHolder> pipes = new ArrayList<>();

        for (var file : files) {
            PipeHolder pipe = null;
            InputStreamReader reader = null;

            try {
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

                var json = JsonParser.parseReader(reader).getAsJsonObject();

                reader.close();

                if (handleMigrations(json)) {
                    try (var writer = new FileWriter(file)) {
                        GSON.toJson(json, writer);
                    } catch (Exception e) {
                        BuildcraftLegacy.LOGGER.error("An error occurred while migrating pipe json {}", file.getName(), e);
                        continue;
                    }
                }

                Pipe loadedPipe = Pipe.fromJson(json);
                if (loadedPipe != null) {
                    String id = file.getName();
                    if (id.contains(".json")) {
                        id = id.substring(0, id.length() - 5);
                    }
                    pipe = new PipeHolder(id, loadedPipe);
                }
            } catch (Exception e) {
                BuildcraftLegacy.LOGGER.error("An error occurred while reading pipe json {}", file.getName(), e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            if (pipe != null && !pipe.value().disabled()) {
                pipes.add(pipe);
            }
        }

        for (PipeHolder pipeHolder : pipes) {
            register(pipeHolder);
        }
    }

    private static boolean handleMigrations(JsonObject json) {
        boolean changed = false;
        return changed;
    }

    public static void register(PipeHolder pipe) {
        if (PIPES.containsKey(pipe.key())) {
            isErrored = true;
            throw new RuntimeException(String.format("Tried to register multiple pipes with the same name: %s", pipe.key()));
        }

        PIPES.put(pipe.key(), pipe.value());

    }

    private static @NotNull File getPipesDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve(BuildcraftLegacy.MODID + "/pipes").toFile();
    }

}
