package com.agileengine.util;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


public class AnalyzerUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(AnalyzerUtil.class);

    public static String getClassSelector(Element element) {
        StringBuilder selector = new StringBuilder(element.tagName());
        String classes = StringUtil.join(element.classNames(), ".");
        if (classes.length() > 0) {
            selector.append('.').append(classes);
        }
        LOGGER.info("Class selector for element {} : {}", element.cssSelector(), selector);
        return selector.toString();
    }

    public static String getClassSelectorWithParent(Element element) {
        return getParentSelector(element) + getClassSelector(element);
    }

    public static String getAttributeSelector(Element element) {
        StringBuilder selector = new StringBuilder(element.tagName());
        String attributes = "[" + StringUtil.join(element.attributes().asList()
                .stream().skip(1).map(Attribute::getKey).collect(Collectors.toList()), "][") + "]";
        if (attributes.length() > 0) {
            selector.append(attributes);
        }
        LOGGER.info("Attribute selector for element {} : {}", element.cssSelector(), selector);
        return selector.toString();
    }

    private static String getParentSelector(Element element) {
        String selector = element.parent().cssSelector() + " > ";
        LOGGER.info("Parent selector for element {} : {}", element.cssSelector(), selector);
        return selector;
    }
}