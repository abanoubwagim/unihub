package com.unihub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {

    @Test
    void verifyModules(){
        ApplicationModules.of(UniHubApplication.class).verify();
    }

}
