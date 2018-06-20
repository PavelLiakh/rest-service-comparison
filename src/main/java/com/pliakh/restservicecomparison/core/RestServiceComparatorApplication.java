package com.pliakh.restservicecomparison.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pliakh.restservicecomparison.impl.PrettyPrinter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan("com.pliakh")
@SpringBootConfiguration
public class RestServiceComparatorApplication implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceComparatorApplication.class.getName());

    @Autowired
    private PrettyPrinter prettyPrinter;

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
            readParameters(
                FileUtils.readFileToString(Paths.get(args.getOptionValues("file").get(0).split(" ")[0]).toFile()));

        if ("GET".equals(parameters.get("method").toString().toUpperCase())) {
            restServiceComparator.doCompareRest(
                (String) parameters.get("url1"),
                (String) parameters.get("url2"),
                (Map<String, String>) parameters.get("parameters"),
                (List<String>) parameters.get("exclude_fields"));
        } else if ("POST".equals(parameters.get("method").toString().toUpperCase())) {
            restServiceComparator.doCompareRest(
                (String) parameters.get("url1"),
                (String) parameters.get("url2"),
                (Map<String, String>) parameters.get("parameters"),
                (String) parameters.get("body"),
                (List<String>) parameters.get("exclude_fields"));
        }
        LOGGER.info("Comparison completed. Logs available in rest-comparison.log");
        prettyPrinter.showProblemsIfAny();
    }

    private void checkArguments(ApplicationArguments args) {
        LOGGER.debug("Received properties: " + Arrays.asList(args.getSourceArgs()).toString());
        if (args.getSourceArgs().length < 1) {
            LOGGER.warn("Required file name as parameter.");
            printMan();
        } else if (args.getOptionNames().contains("man")) {
            printMan();
        } else if (!args.getOptionNames().contains("file")) {
            LOGGER.warn("Required file name as parameter.");
            System.exit(2);
        }

        Path parametersFilePath = Paths.get(args.getOptionValues("file").get(0).split(" ")[0]);
        if (!Files.exists(parametersFilePath)) {
            LOGGER.warn(String.format("File doesn't exist: %s", parametersFilePath));
            System.exit(2);
        } else if (!Files.isReadable(parametersFilePath)) {
            LOGGER.warn(String.format("Cannot open file for reading: %s", parametersFilePath));
            System.exit(2);
        }
    }

    private void printMan() {
        LOGGER.warn("To run application provide parameter \"file\".\n" +
                "    example: java -jar <.jar> --file=<pathToFile> --logLevel=DEBUG");
        LOGGER.info("EXAMPLE for GET method\n" +
            "{\n" +
            "  \"url1\": \"<url1>\",\n" +
            "  \"url2\": \"<url2>\",\n" +
            "  \"method\": \"get\",\n" +
            "  \"parameters\": {\n" +
            "    \"<field1>\": \"<value1>\",\n" +
            "    \"<field2>[]\": \"<value2,value3,value4>\",\n" +
            "    ...\n" +
            "  },\n" +
            "  (optional)\"exclude_fields\": \"<field1,field2,field3>\"\n" +
            "}");
        LOGGER.info("EXAMPLE for POST method\n" +
            "{\n" +
            "  \"url1\": \"<url1>\",\n" +
            "  \"url2\": \"<url2>\",\n" +
            "  \"method\": \"post\",\n" +
            "  \"parameters\": {\n" +
            "    \"<field1>\": \"<value1>\",\n" +
            "    \"<field2>[]\": \"<value2,value3,value4>\",\n" +
            "    ...\n" +
            "  },\n" +
            "  \"body\": <body>,\n" +
            "  (optional)\"exclude_fields\": \"<field1,field2,field3>\"\n" +
            "}\n");
        // FIXME: does not work running from jar
        // Files.lines(Paths.get(ClassLoader.getSystemResource("man.txt").toURI())).forEach(System.out::println);
        // Caused by: java.nio.file.FileSystemNotFoundException
        //	at com.sun.nio.zipfs.ZipFileSystemProvider.getFileSystem(ZipFileSystemProvider.java:171)
        System.exit(2);
    }

    private Map<String, Object> readParameters(String parametersFileContent) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        JsonNode tree = new ObjectMapper().readTree(parametersFileContent);
        parameters.put("url1", tree.get("url1").asText());
        parameters.put("url2", tree.get("url2").asText());
        parameters.put("method", tree.get("method").asText());
        // parameters
        try {
            JsonNode requestParametersNode = tree.get("parameters");

            Map<String, String> requestParameters = new HashMap<>();
            parameters.put("parameters", requestParameters);
            requestParametersNode.fieldNames().forEachRemaining(parameterName -> {
                requestParameters.put(parameterName, requestParametersNode.get(parameterName).asText());
            });
        } catch (NullPointerException e) {
            LOGGER.debug("Can't find url parameters, skipping");
        }

        // post body
        if ("POST".equals(parameters.get("method").toString().toUpperCase())) {
            JsonNode requestBody = tree.get("body");
            parameters.put("body", requestBody.toString());
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
