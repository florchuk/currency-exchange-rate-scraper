package org.example.utils;

import org.example.scrapers.Scraper;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Queue;

public class ScraperClassReader {
    /**
     * Getting Queue of the classes.
     * @param classNames The class names.
     * @return Queue of the classes.
     * @throws IllegalArgumentException If the one of provided class names is empty, or is not valid.
     * @throws ClassNotFoundException If class wasn't found, for one of the provided class names.
     */
    public static Queue<Class<? extends Scraper>> getClasses(String ...classNames)
            throws IllegalArgumentException, ClassNotFoundException
    {
        Queue<Class<? extends Scraper>> queueClasses = new ArrayDeque<>();

        for (String className : classNames) {
            if (className.isEmpty()) {
                throw new IllegalArgumentException("The one of the class names is empty.");
            }

            Class<?> scraperClass = Class.forName(className);

            if (!Scraper.class.isAssignableFrom(scraperClass) || Modifier.isAbstract(scraperClass.getModifiers())) {
                throw new IllegalArgumentException(
                        String.format("\"%s\" class name is invalid.", className)
                );
            }

            queueClasses.add(scraperClass.asSubclass(Scraper.class));
        }

        return queueClasses;
    }
}