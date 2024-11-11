package io.mangoo.test.hamcrest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * 
 * @author svenkubiak
 *
 */
@SuppressWarnings("rawtypes")
public class RegexMatcher extends BaseMatcher {
    private final String regex;

    public RegexMatcher(String regex){
        this.regex = regex;
    }

    public static RegexMatcher matches(String regex){
        return new RegexMatcher(regex); //NOSONAR
    }

    @Override
    public boolean matches(Object object){
        return ((String) object).matches(regex); //NOSONAR
    }
    
    @Override
    public void describeTo(Description description){
        description.appendText("Matches regex = " + this.regex);
    }
}