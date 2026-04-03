package com.example.demo.scheduler;

import com.example.demo.filter.FilterEngine;
import com.example.demo.model.Vacante;
import com.example.demo.notifier.NotifierService;
import com.example.demo.repository.VacanteRepository;
import com.example.demo.scraper.ScraperService;
import com.example.demo.scraper.ScraperService.VacanteRaw;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HunterScheduler {

    private final ScraperService    scraper;
    private final FilterEngine      filter;
    private final VacanteRepository repository;
    private final NotifierService   notifier;

    public HunterScheduler(ScraperService scraper, FilterEngine filter,
                           VacanteRepository repository, NotifierService notifier) {
        this.scraper    = scraper;
        this.filter     = filter;
        this.repository = repository;
        this.notifier   = notifier;
    }

    // Corre cada 30 minutos
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void hunt() {
        System.out.println("🔍 [Hunter] Iniciando búsqueda...");

        List<VacanteRaw> filtradas = filter.filtrar(scraper.scrapeTodo());
        int nuevas = 0;

        for (VacanteRaw raw : filtradas) {
            if (!repository.existsByExternalId(raw.externalId())) {

                // Guardar en DB
                Vacante v = new Vacante(
                        raw.externalId(),
                        FilterEngine.limpiar(raw.titulo()),
                        FilterEngine.limpiar(raw.empresa()),
                        raw.ubicacion(),
                        raw.salario(),
                        raw.url()
                );
                repository.save(v);

                // Notificar por Telegram
                notifier.enviar(v.getTitulo(), v.getEmpresa(),
                        v.getUbicacion(), v.getSalario(), v.getUrl());
                nuevas++;
            }
        }

        System.out.println("✅ [Hunter] Ciclo completo — nuevas: " + nuevas
                + " | total en DB: " + repository.count());
    }
}