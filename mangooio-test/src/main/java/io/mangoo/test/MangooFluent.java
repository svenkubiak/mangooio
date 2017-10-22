package io.mangoo.test;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.configuration.FluentConfiguration;

/**
 *
 * @author svenkubiak
 * @deprecated As of 4.7.0, replace by FluentTestRunner
 */
@FluentConfiguration(webDriver="htmlunit")
@Deprecated
public class MangooFluent extends FluentTest {
}