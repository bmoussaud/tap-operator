package org.moussaud.tanzu.tapoperator.controller;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.moussaud.tanzu.tapoperator.resource.TapResource;
import org.moussaud.tanzu.tapoperator.resource.TapSpec;

public class UtilsTest {

    @Test
    void testKnownVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.7.0");
        resource.setSpec(spec);
        assertEquals("1.7.0", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testUnknownVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.9.x");
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testUnknownRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.9.4-rc.3"); // 1.7.2-rc.6
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.7.0-rc.8");
        resource.setSpec(spec);
        assertEquals("1.7.0", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testRoundedLowerRCVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.4.12-rc.8 ");
        resource.setSpec(spec);
        assertEquals("1.4.7", Utils.getClusterEssentialsBundleVersion(resource));
    }

    @Test
    void testBuildVersionGetClusterEssentialsBundleVersion() {
        TapResource resource = new TapResource();
        TapSpec spec = new TapSpec();
        spec.setVersion("1.8.0-build.38");
        resource.setSpec(spec);
        assertEquals("1.7.1", Utils.getClusterEssentialsBundleVersion(resource));
    }

}
