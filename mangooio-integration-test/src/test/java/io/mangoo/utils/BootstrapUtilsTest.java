package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class BootstrapUtilsTest {

    @Test
    public void testGetPackageName() {
        //given
        String package1 = "io.mangoo";
        String package2 = "io.mangoo.";
        
        //then
        assertThat(BootstrapUtils.getPackageName(package1), equalTo(package2));
        assertThat(BootstrapUtils.getPackageName(package2), equalTo(package2));
    } 
    
    @Test
    public void testGetBaseDirectory() {
        //then
        assertThat(BootstrapUtils.getBaseDirectory(), not(nullValue()));
    }
    
    @Test
    public void testGetVersion() throws InterruptedException {
        //then
        assertThat(BootstrapUtils.getVersion(), not(nullValue()));
    } 
    
    @Test
    public void testGetLogo() throws InterruptedException {
        //then
        assertThat(BootstrapUtils.getLogo(), not(nullValue()));
    }
}