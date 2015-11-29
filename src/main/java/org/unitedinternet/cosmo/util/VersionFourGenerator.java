package org.unitedinternet.cosmo.util;

import carldav.service.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Class used to generate pseudo randomly generated UUID
 * @author iulia
 * @author Kamill Sokol
 *
 */
@Component("idGenerator")
public class VersionFourGenerator implements IdGenerator {

    @Override
    public String nextStringIdentifier() {
        return UUID.randomUUID().toString();
    }
}
