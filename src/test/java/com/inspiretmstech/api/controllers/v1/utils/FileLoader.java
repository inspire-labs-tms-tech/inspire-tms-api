package com.inspiretmstech.api.controllers.v1.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileLoader {

    private final ResourceLoader loader;

    public FileLoader(ResourceLoader loader) {
        this.loader = loader;
    }

    public static FileLoader from(ResourceLoader loader) {
        return new FileLoader(loader);
    }

    /**
     * Load a list of file via a path
     * - Support wildcard expressions (ex.: /gp/*.json)
     * - Automatically prepends "classpath:" (do not include)
     * @param path the path to load files from (supports wildcard expressions, such as /top/level/directory/*.ext)
     * @throws IOException a file is unable to be loaded
     */
    public List<File> load(String path) throws IOException {
        Resource[] testFiles = ResourcePatternUtils.getResourcePatternResolver(this.loader).getResources(path.startsWith("classpath:") ? path : "classpath:" + path);
        List<File> files = new ArrayList<>();
        for (Resource test : testFiles) files.add(new File(test.getFile().getAbsolutePath()));
        return files;
    }

    /**
     * Load a list of file via a path
     * - Support wildcard expressions (ex.: /gp/*.json)
     * - Automatically prepends "classpath:" (do not include)
     * @param path the path to load files from (supports wildcard expressions, such as /top/level/directory/*.ext)
     * @throws IOException a file is unable to be loaded
     */
    public List<String> loadAsString(String path) throws IOException {
        List<File> files = this.load(path);
        List<String> lines = new ArrayList<>();
        for (File file : files) {
            Scanner reader = new Scanner(file);
            StringBuilder data = new StringBuilder();
            while (reader.hasNextLine()) data.append(reader.nextLine());
            reader.close();
            lines.add(data.toString());
        }
        return lines;
    }

}
