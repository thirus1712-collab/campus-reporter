package com.campusreporter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CampusReporterApplicationTests {

    @Test
    void applicationClassPresent() {
        CampusReporterApplication app = new CampusReporterApplication();
        assertNotNull(app);
    }
}
