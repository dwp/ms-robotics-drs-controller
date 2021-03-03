package uk.gov.dwp.health.esao.verified.info;

import java.util.Properties;

@FunctionalInterface
public interface InfoProvider {

  Properties getInfo();
}
