package com.pliakh.restservicecomparison.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PrettyPrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(PrettyPrinter.class.getName());

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

    public void debug(String message, List<JsonNode> messageList) {
        LOGGER.debug(message + "\n" + StringUtils.join(messageList, "\n"));
    }

    private String msg(String message, String parameterName, Object arg1, Object arg2) {
        return String.format("%s. %s1=%s, %s2=%s", message,
                parameterName, Objects.nonNull(arg1) ? arg1.toString() : "",
                parameterName, Objects.nonNull(arg2) ? arg2.toString() : "");
    }
}
