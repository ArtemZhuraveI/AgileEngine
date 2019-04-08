package com.agileengine;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.agileengine.exception.ElementNotFoundException;
import com.agileengine.util.AnalyzerUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static String CHARSET_NAME = "utf8";

    public static final AnalyzerUtil analyzerUtil = new AnalyzerUtil();

    public static final String TARGET_ELEMENT_ID = "make-everything-ok-button";

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        String originalFilePath = args[0];
        String changedFilePath = args[1];
        String originalElementId = args.length == 3 ? args[2] : TARGET_ELEMENT_ID;

        Optional<Element> buttonOpt = findElementById(new File(originalFilePath), originalElementId);

        Optional<String> stringifiedAttributesOpt = buttonOpt.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );

        stringifiedAttributesOpt.ifPresent(attrs -> LOGGER.info("Original element : [{}]", attrs));

        Element original = buttonOpt.orElseThrow(ElementNotFoundException::new);
        LOGGER.info("Absolute path for target: {}", findTargetElement(new File(changedFilePath), original));
    }

    public static Optional<Element> findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.getElementById(targetElementId));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    public static List<String> findTargetElement(File htmlFile, Element element) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            LOGGER.info("Searching element by css selector {}", element.cssSelector());
            Elements result = doc.select(element.cssSelector());
            if (!result.isEmpty()) {
                return formPath(result.first());
            }

            result = doc.select(analyzerUtil.getClassSelector(element));
            if (!result.isEmpty()) {
                return formPath(result.first());
            }

            result = doc.select(analyzerUtil.getClassSelectorWithParent(element));
            if (!result.isEmpty()) {
                return formPath(result.first());
            }

            result = doc.select(analyzerUtil.getAttributeSelector(element));
            if (!result.isEmpty()) {
                return formPath(result.first());
            }

            LOGGER.error("Requested element has not been found!");
            throw new ElementNotFoundException("Please check element for existence");

        } catch (IOException e) {
            LOGGER.error("Error occured while reading [{}] file", htmlFile.getAbsolutePath(), e);
            return formPath(new Elements().first());
        } catch (ElementNotFoundException e) {
            LOGGER.error("Error occured while reading [{}] file", htmlFile.getAbsolutePath(), e);
            return formPath(new Elements().first());
        }
    }

    static List<String> formPath(Element targetElement) {
        List<String> path = new ArrayList<>();
        Element previouse = null;
        boolean goOn = true;
        while (goOn) {
            path.add( " " + targetElement.tagName());
            if (!targetElement.hasParent()) {
                goOn = false;
                break;
            }

            targetElement = targetElement.parent();
        }

        Collections.reverse(path);
        return path;
    }
}