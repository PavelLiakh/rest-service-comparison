package com.pliakh.restservicecomparison.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Configuration
@ComponentScan("com.pliakh")
@SpringBootConfiguration
public class RestServiceComparatorApplication implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(RestServiceComparatorApplication.class.getName());

    @Autowired
    private RestServiceComparator restServiceComparator;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) throws Exception {
        checkArguments(args);
        Map<String, Object> parameters =
            readParameters(FileUtils.readFileToString(Paths.get(args.getOptionValues("file").get(0)).toFile()));
        if ("GET".equals(parameters.get("method").toString().toUpperCase())) {
            restServiceComparator.doCompareRest((String) parameters.get("url1"), (String) parameters.get("url2"),
                (Map<String, String>) parameters.get("parameters"), (List<String>) parameters.get("exclude_fields"));
        } else if ("POST".equals(parameters.get("method").toString().toUpperCase())) {
            restServiceComparator.doCompareRest((String) parameters.get("url1"), (String) parameters.get("url2"),
                (String) parameters.get("body"), (List<String>) parameters.get("exclude_fields"));
        }
    }

    private void checkArguments(ApplicationArguments args) throws IOException, URISyntaxException {
        LOGGER.info("Received properties: " + args.getSourceArgs());
        if (args.getSourceArgs().length != 1) {
            LOGGER.warning("Required file name as parameter. Run with '--man' to get manual.");
            printMan();
        } else if (args.getOptionNames().contains("man")) {
            printMan();
        } else if (!args.getOptionNames().contains("file")) {
            LOGGER.warning("Required file name as parameter. Run with '--man' to get manual.");
            System.exit(1);
        }

        Path parametersFilePath = Paths.get(args.getOptionValues("file").get(0));
        if (!Files.exists(parametersFilePath)) {
            LOGGER.warning("File doesn't exist " + parametersFilePath);
            System.exit(1);
        } else if (!Files.isReadable(parametersFilePath)) {
            LOGGER.warning("Cannot open file for reading " + parametersFilePath);
            System.exit(1);
        }
    }

    private void printMan() throws IOException, URISyntaxException {
        // FIXME: does not work running from jar
        // Files.lines(Paths.get(ClassLoader.getSystemResource("man.txt").toURI())).forEach(System.out::println);
        // Caused by: java.nio.file.FileSystemNotFoundException
        //	at com.sun.nio.zipfs.ZipFileSystemProvider.getFileSystem(ZipFileSystemProvider.java:171)
        System.exit(1);
    }

    private Map<String, Object> readParameters(String parametersFileContent) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        JsonNode tree = new ObjectMapper().readTree(parametersFileContent);
        parameters.put("url1", tree.get("url1").asText());
        parameters.put("url2", tree.get("url2").asText());
        parameters.put("method", tree.get("method").asText());
        if ("GET".equals(parameters.get("method").toString().toUpperCase())) {
            JsonNode requestParametersNode = tree.get("parameters");
            Map<String, String> requestParameters = new HashMap<>();
            parameters.put("parameters", requestParameters);
            requestParametersNode.fieldNames().forEachRemaining(parameterName -> {
                requestParameters.put(parameterName, requestParametersNode.get(parameterName).asText());
            });
        } else if ("POST".equals(parameters.get("method").toString().toUpperCase())) {
            JsonNode requestBody = tree.get("body");
            parameters.put("body", requestBody.toString());
        } else {
            LOGGER.warning("Supported methods: get, post. But was: " + parameters.get("method"));
        }

        // exclude fields
        List<String> excludeFeilds = new ArrayList<>();
        try {
            JsonNode excludeFieldsNode = tree.get("exclude_fields");
            excludeFeilds.addAll(Arrays.asList(excludeFieldsNode.asText().split(",")));
        } catch (Exception e) {
            // FIXME avoid that
        }
        parameters.put("exclude_fields", excludeFeilds);
        return parameters;
    }
}
