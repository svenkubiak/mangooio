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

    public boolean matches(Object o){
        return ((String) o).matches(regex);
    }

    @Override
    public void describeTo(Description description){
        description.appendText("Matches regex = " + this.regex);
    }
    
    public static RegexMatcher matches(String regex){
        return new RegexMatcher(regex);
    }
}