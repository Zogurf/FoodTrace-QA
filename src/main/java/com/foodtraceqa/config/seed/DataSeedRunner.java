package com.foodtraceqa.config.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("seed")
@EnableConfigurationProperties(SeedProperties.class)
@RequiredArgsConstructor
public class DataSeedRunner implements ApplicationRunner {
    private final DataSeedService service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando carga simulada; cualquier error revierte toda la carga.");
        log.info("Carga terminada y confirmada: {}", service.generate());
        log.info("Puedes detener este proceso. Arranca normalmente sin el perfil seed para usar la aplicación.");
    }
}
