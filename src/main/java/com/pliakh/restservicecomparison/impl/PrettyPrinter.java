package com.pliakh.restservicecomparison.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pliakh.restservicecomparison.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class PrettyPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class.getName());

    // message\n header(30chars align to left)| arg1 : arg2|
    private static final String PRINT_FORMAT = "|%-30s\t|%-15s|%5s : %-5s|";

    private static List<String> commonWarnings = new ArrayList<>();

    public void info(String message) {
        LOGGER.info(message);
    }

    public void infoPretty(String message, String parameterName, Object arg1, Object arg2) {
        LOGGER.info(msg(message, parameterName, arg1, arg2));
    }

    public void warn(String message) {
        LOGGER.warn(message);
        commonWarnings.add(message);
    }

    public void warnPretty(String message, String parameterName, Object arg1, Object arg2) {
        LOGGER.warn(msg(message, parameterName, arg1, arg2));
        commonWarnings.add(msg(message, parameterName, arg1, arg2));
    }

    public void error(String message) {
        LOGGER.error(message);
    }

    public void debug(String message) {
        LOGGER.debug(message);
    }

    public void prettyDebugTwoJson(String json1, String json2) {
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
        while (i < json1Lines.size() || i < json2Lines.size()) {
            LOGGER.debug(String.format("%-100s | %-100s",
                    json1Lines.size() > i ? json1Lines.get(i) : "",
                    json2Lines.size() > i ? json2Lines.get(i) : ""));
            i++;
        }
    }

    public void showProblemsIfAny() {
        if (!commonWarnings.isEmpty()) {
            LOGGER.warn("Summary problems:");
            commonWarnings.forEach(LOGGER::warn);
            System.exit(2);
        }
    }

    private String msg(String message, String parameterName, Object arg1, Object arg2) {
        return String.format(PRINT_FORMAT, message, parameterName,
                Objects.nonNull(arg1) ? arg1.toString() : "",
                Objects.nonNull(arg2) ? arg2.toString() : "");
    }
}
