package com.pliakh.restservicecomparison;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pliakh.restservicecomparison.core.RestServiceComparator;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RestServiceComparatorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger("application");

    public static void main(String[] args) throws IOException {
        Map<String, Object> parameters = checkAndParseArguments(args);
        RestServiceComparator restServiceComparator =
                new AnnotationConfigApplicationContext("com.pliakh").getBean(RestServiceComparator.class);
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
        LOGGER.info("Rest comparison completed, log of comparison available in 'rest-comparison.log' file");
    }

    private static Map<String, Object> checkAndParseArguments(String[] args) throws IOException {
        Options options = new Options();
        Option debugLogLevel = new Option("d", "debug", false, "enable debug log level");
        debugLogLevel.setRequired(false);
        options.addOption(debugLogLevel);
        Option file = new Option("f", "file", true, "input file with parameters");
        file.setRequired(true);
        options.addOption(file);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rest service comparison tool", options);

        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // set logger variable
        if (cmd.getArgList().contains("debug")) {
            System.setProperty("LOG_LEVEL", "debug");
        }

        // open file
        Path parametersFilePath = Paths.get(cmd.getOptionValue("file"));
        if (!Files.exists(parametersFilePath)) {
            LOGGER.warn(String.format("File doesn't exist: %s", parametersFilePath));
            System.exit(1);
        } else if (!Files.isReadable(parametersFilePath)) {
            LOGGER.warn(String.format("Cannot open file for reading: %s", parametersFilePath));
            System.exit(1);
        }

        return parseParameters(FileUtils.readFileToString(parametersFilePath.toFile()));
    }

    // TODO нужна анти-вандальная защита
    private static Map<String, Object> parseParameters(String parametersFileContent) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        try {
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
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            printExamples();
            System.exit(1);
        }
        return parameters;
    }

    private static void printExamples() {
        LOGGER.info("EXAMPLE of file for GET method\n" +
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
        LOGGER.info("EXAMPLE of file for POST method\n" +
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
}
