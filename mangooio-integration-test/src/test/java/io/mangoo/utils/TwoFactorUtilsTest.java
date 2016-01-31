package io.mangoo.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.mangoo.utils.TwoFactorUtils;
/**
 * 
 * @author WilliamDunne
 *
 */
public class TwoFactorUtilsTest {
	private static final int THIRTY_SECONDS = 1000 * 30;
	
	@Test
	public void testGenerateSecret() {
		//given
		String secret = TwoFactorUtils.generateBase32Secret();

		//that
		assertThat(secret, not(nullValue()));
		assertThat(secret.length(), equalTo(16));
	}
	
	@Test
	public void testCodeChecking (){
		//given
		String secret = TwoFactorUtils.generateBase32Secret();		
		int valid = Integer.parseInt(TwoFactorUtils.generateCurrentNumber(secret));		
		long time = System.currentTimeMillis() + THIRTY_SECONDS;

		//that
		assertThat(TwoFactorUtils.validateCurrentNumber(valid, secret, 2), equalTo(true));
	}
}
