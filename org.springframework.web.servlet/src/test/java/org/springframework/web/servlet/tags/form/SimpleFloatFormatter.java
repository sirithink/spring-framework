package org.springframework.web.servlet.tags.form;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.model.ui.format.Formatter;

/**
 * 
 * @author Jeremy Grelle
 */
public class SimpleFloatFormatter implements Formatter<Float> {

    @Override
    public String format(Float object, Locale locale) {
        return object+"f";
    }

    @Override
    public Float parse(String formatted, Locale locale) throws ParseException {
        return new Float(formatted);
    }

}
