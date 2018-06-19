package com.pliakh.restservicecomparison.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pliakh.restservicecomparison.api.IResponseComparator;
import com.pliakh.restservicecomparison.api.RestResponse;

import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

@Component
public class ResponseComparator implements IResponseComparator {

    private static final Logger LOGGER = Logger.getLogger(ResponseComparator.class.getName());

    // message\n header(30chars align to left)| arg1 : arg2|
    private static final String PRINT_FORMAT = "|%-30s\t|%-15s|%5s : %-5s|";

    @Override
    public void doCompare(RestResponse restResponse1, RestResponse restResponse2, List<String> excludeFields) {
        System.out.println("url1 = " + restResponse1.getUrl());
        System.out.println("url2 = " + restResponse2.getUrl());
        compareStatusCodes(restResponse1, restResponse2);
        compareResponseTime(restResponse1, restResponse2);
        compareResponseEntitiesNumber(restResponse1, restResponse2);
        compareResponseBodyPlain(restResponse1, restResponse2);
        compareResponseByEntites(restResponse1, restResponse2, excludeFields);
    }

    void compareStatusCodes(RestResponse restResponse1, RestResponse restResponse2) {
        if (restResponse1.getHttpStatus().equals(restResponse2.getHttpStatus())) {
            printPretty(LogLevel.INFO, "Response codes equals", "Response code", restResponse1.getHttpStatus(),
                restResponse2.getHttpStatus());
        } else {
            printPretty(LogLevel.INFO, "Different response codes", "Response code",
                restResponse1.getHttpStatus().value(),
                restResponse2.getHttpStatus().value());
        }
    }

    void compareResponseTime(RestResponse restResponse1, RestResponse restResponse2) {
        char comparisonSign =
            restResponse1.getTime() == restResponse2.getTime()
                ? '='
                : restResponse1.getTime() > restResponse2.getTime()
                ? '>'
                : '<';
        printPretty(LogLevel.INFO,
            String.format("Response time url1 %s url2", comparisonSign), "Response time", restResponse1.getTime(),
            restResponse2.getTime());
    }

    void compareResponseEntitiesNumber(RestResponse restResponse1, RestResponse restResponse2) {
        int response1NodesCount = getJsonNodesCount(restResponse1.getResponseBody());
        int response2NodesCount = getJsonNodesCount(restResponse2.getResponseBody());
        if (response1NodesCount == response2NodesCount) {
            printPretty(LogLevel.INFO, "Entites count equals", "entities count", response1NodesCount,
                response2NodesCount);
        } else {
            printPretty(LogLevel.WARN, "Entities count not equals", "entities count", response1NodesCount,
                response2NodesCount);
        }
    }

    void compareResponseBodyPlain(RestResponse restResponse1, RestResponse restResponse2) {
        if (restResponse1.getResponseBody().equals(restResponse2.getResponseBody())) {
            printPretty(LogLevel.INFO, "Responses equals");
        } else {
            printPretty(LogLevel.WARN, "Responses are not equals", "Responses", null, null);
        }
        prettyPrintTwoJsons(restResponse1.getResponseBody(),
            restResponse2.getResponseBody());
    }

    void compareResponseByEntites(RestResponse restResponse1, RestResponse restResponse2, List<String> excludeFields) {
        BiConsumer<JsonNode, List<String>> removeFieldConsumer = (jsonNode, fields) ->
        {
            if (jsonNode.isObject()) {
                fields.forEach(((ObjectNode) jsonNode)::remove
                );
            }
        };
        List<JsonNode> response1Nodes = tryGetMainContentNodes(restResponse1.getResponseBody());
        List<JsonNode> response2Nodes = tryGetMainContentNodes(restResponse2.getResponseBody());
        List<JsonNode> commonNodes = new ArrayList<>();

        for (JsonNode response1Node : response1Nodes) {
            removeFieldConsumer.accept(response1Node, excludeFields);
        }
        for (JsonNode response2Node : response2Nodes) {
            removeFieldConsumer.accept(response2Node, excludeFields);
        }

        response1Nodes.forEach(node -> {
            if (response2Nodes.contains(node)) {
                commonNodes.add(node);
                response2Nodes.remove(node);
            }
        });
        response1Nodes.removeAll(commonNodes);
        response2Nodes.removeAll(commonNodes);
        System.out.println("Common nodes");
        if (commonNodes.isEmpty()) {
            System.out.println("none");
        } else {
            commonNodes.forEach(node -> prettyPrintTwoJsons("", node.toString()));
        }
        System.out.println("URL1 extra nodes");
        if (response1Nodes.isEmpty()) {
            System.out.println("none");
        } else {
            response1Nodes.forEach(node -> prettyPrintTwoJsons(node.toString(), ""));
        }
        System.out.println("URL2 extra nodes");
        if (response2Nodes.isEmpty()) {
            System.out.println("none");
        } else {
            response2Nodes.forEach(node -> prettyPrintTwoJsons("", node.toString()));
        }
    }

    private List<JsonNode> tryGetMainContentNodes(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode response = root.get("response");
            JsonNode content = response.get("content");
            JsonNode mainContentNodeCandidate;
            if (content.isArray()) {
                mainContentNodeCandidate = content.get(1);
            } else if (content.isObject()) {
                mainContentNodeCandidate = content;
                Iterator<Entry<String, JsonNode>> mainContentNodeCandidateFields = mainContentNodeCandidate.fields();
                while (mainContentNodeCandidateFields.hasNext()) {
                    JsonNode contentElement = mainContentNodeCandidateFields.next().getValue();
                    if (contentElement.isArray()) {
                        mainContentNodeCandidate = contentElement;
                        break;
                    }
                }
            } else {
                mainContentNodeCandidate = root;
            }

            List<JsonNode> nodes = new ArrayList<>();
            for (int i = 0; i < mainContentNodeCandidate.size(); i++) {
                nodes.add(mainContentNodeCandidate.get(i));
            }
            return nodes;
        } catch (NullPointerException | IOException e) {
            // FIXME process or avoid exceptions
            System.out.println("Unsuccessful trying to get content nodes");
            return new ArrayList<>();
        }
    }

    private int getJsonNodesCount(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        Function<String, Integer> responseToEntitiesCountFunction = (responseBody) -> {
            try {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode response = root.get("response");
                JsonNode content = response.get("content");

                // logic to try get array node of find max count of fields
                int size = -1;
                if (content.isArray()) {
                    content = content.get(1);
                    size = content.size();
                } else {
                    Iterator<Entry<String, JsonNode>> contentFields = content.fields();
                    while (contentFields.hasNext()) {
                        JsonNode contentElement = contentFields.next().getValue();
                        if (contentElement.isArray()) {
                            size = contentElement.size();
                        }
                        if (size > 0) {
                            break;
                        }
                    }
                    if (size == -1) {
                        size = content.size();
                    }
                }
                return size;
            } catch (NullPointerException | IOException e) {
                // FIXME process or avoid exceptions
                System.out.println("Unsuccessful trying to get nodes count");
                return 0;
            }
        };
        return responseToEntitiesCountFunction.apply(json);
    }

    private void prettyPrintTwoJsons(String json1, String json2) {
        Function<String, List<String>> jsonToListOfLines = json -> {
            ObjectMapper objectMapper = new ObjectMapper();
            String prettyJson = null;
            try {
                Object jsonObject = objectMapper.readValue(json, Object.class);
                prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            } catch (Exception e) {
            }
            return Objects.nonNull(prettyJson)
                ? Arrays.asList(prettyJson.split("\n"))
                : new ArrayList<>();
        };

        List<String> json1Lines = jsonToListOfLines.apply(json1);
        List<String> json2Lines = jsonToListOfLines.apply(json2);
        int i = 0;
        System.out.println("-------------------------");
        while (i < json1Lines.size() || i < json2Lines.size()) {
            System.out.printf("%-100s | %-100s\n",
                json1Lines.size() > i ? json1Lines.get(i) : "",
                json2Lines.size() > i ? json2Lines.get(i) : "");
            i++;
        }
        System.out.println("-------------------------");
    }

    // FIXME use log level
    private void printPretty(LogLevel logLevel, String message) {
//        LOGGER.info(message);
        System.out.println(message);
    }

    // FIXME use log level
    private void printPretty(LogLevel logLevel, String message, String parameterName, Object arg1, Object arg2) {
        System.out.println(String.format(PRINT_FORMAT, message, parameterName,
            Objects.nonNull(arg1) ? arg1.toString() : "",
            Objects.nonNull(arg2) ? arg2.toString() : ""));
//        LOGGER.info(String.format(PRINT_FORMAT, message, parameterName,
//            Objects.nonNull(arg1) ? arg1.toString() : "",
//            Objects.nonNull(arg2) ? arg2.toString() : ""));
    }
}
