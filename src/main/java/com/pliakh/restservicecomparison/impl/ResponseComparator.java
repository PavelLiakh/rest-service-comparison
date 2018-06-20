package com.pliakh.restservicecomparison.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pliakh.restservicecomparison.api.IResponseComparator;
import com.pliakh.restservicecomparison.api.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class ResponseComparator implements IResponseComparator {

    @Autowired
    PrettyPrinter prettyPrinter;

    @Override
    public void doCompare(RestResponse restResponse1, RestResponse restResponse2, List<String> excludeFields) {
        prettyPrinter.infoPretty("Urls to compare", "urls", restResponse1.getUrl(), restResponse2.getUrl());
        compareStatusCodes(restResponse1, restResponse2);
        compareResponseTime(restResponse1, restResponse2);
        compareResponseEntitiesNumber(restResponse1, restResponse2);
        compareResponseByEntites(restResponse1, restResponse2, excludeFields);
        compareResponseBodyPlain(restResponse1, restResponse2);
    }

    void compareStatusCodes(RestResponse restResponse1, RestResponse restResponse2) {
        if (restResponse1.getHttpStatus().equals(restResponse2.getHttpStatus())) {
            prettyPrinter.infoPretty("Response codes equals", "Response code",
                    restResponse1.getHttpStatus(),
                    restResponse2.getHttpStatus());
        } else {
            prettyPrinter.infoPretty("Different response codes", "Response code",
                    restResponse1.getHttpStatus().value(),
                    restResponse2.getHttpStatus().value());
        }
        if (restResponse1.getHttpStatus() != HttpStatus.OK ||
                restResponse2.getHttpStatus() != HttpStatus.OK) {
            prettyPrinter.warnPretty("Response code is not OK", "response code",
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
        prettyPrinter.infoPretty(
                String.format("Response time url1 %s url2", comparisonSign), "Response time", restResponse1.getTime(),
                restResponse2.getTime());
    }

    void compareResponseEntitiesNumber(RestResponse restResponse1, RestResponse restResponse2) {
        int response1NodesCount = getJsonNodesCount(restResponse1.getResponseBody());
        int response2NodesCount = getJsonNodesCount(restResponse2.getResponseBody());
        if (response1NodesCount == response2NodesCount) {
            prettyPrinter.infoPretty("Entities count equals", "entities count", response1NodesCount,
                    response2NodesCount);
        } else {
            prettyPrinter.warnPretty("Entities count not equals", "entities count", response1NodesCount,
                    response2NodesCount);
        }
    }

    void compareResponseBodyPlain(RestResponse restResponse1, RestResponse restResponse2) {
//        if (restResponse1.getResponseBody().equals(restResponse2.getResponseBody())) {
//            prettyPrinter.info("Responses equals (plain comparsion)");
//        } else {
//            prettyPrinter.warn("Responses are not equals (plain comparsion)");
//        }
//        prettyPrinter.prettyDebugTwoJson(restResponse1.getResponseBody(),
//                restResponse2.getResponseBody());
        prettyPrinter.debug("Response1 body: " + restResponse1.getResponseBody());
        prettyPrinter.debug("Response2 body: " + restResponse2.getResponseBody());
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

        prettyPrinter.info("Common nodes count: " + commonNodes.size());
        if (!response1Nodes.isEmpty() && !response2Nodes.isEmpty()) {
            prettyPrinter.warnPretty("Extra nodes in responses", "extra nodes #", response1Nodes.size(), response2Nodes.size());
        }
        if (commonNodes.isEmpty()) {
            prettyPrinter.debug("Common nodes: none");
        } else {
            prettyPrinter.debug("Common nodes");
//            commonNodes.forEach(node -> prettyPrinter.prettyDebugTwoJson("", node.toString()));
//            commonNodes.forEach(node -> prettyPrinter.debug());
        }
        if (response1Nodes.isEmpty()) {
            prettyPrinter.info("No extra nodes in URL1");
        } else {
            prettyPrinter.info("URL1 extra nodes count: " + response1Nodes.size());
            prettyPrinter.debug("URL1 extra nodes:");
            response1Nodes.forEach(node -> prettyPrinter.debug(node.toString()));
//            response1Nodes.forEach(node -> prettyPrinter.prettyDebugTwoJson(node.toString(), ""));
        }
        if (response2Nodes.isEmpty()) {
            prettyPrinter.info("No extra nodes in URL2");
        } else {
            prettyPrinter.info("URL2 extra nodes count: " + response1Nodes.size());
            prettyPrinter.debug("URL2 extra nodes:");
            response2Nodes.forEach(node -> prettyPrinter.debug(node.toString()));
//            response2Nodes.forEach(node -> prettyPrinter.prettyDebugTwoJson("", node.toString()));
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
            prettyPrinter.error("Unsuccessful trying to get content nodes");
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

                // logic to try get array node or find max count of fields
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
                prettyPrinter.error("Unsuccessful trying to get nodes count");
                return 0;
            }
        };
        return responseToEntitiesCountFunction.apply(json);
    }
}
