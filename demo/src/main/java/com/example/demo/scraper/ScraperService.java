package com.example.demo.scraper;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    // En ScraperService
    public List<VacanteRaw> scrapeTodo() {
        List<VacanteRaw> todas = new ArrayList<>();

        System.out.println("\n── OCC ──────────────────────");
        todas.addAll(scrapeOCC());

        System.out.println("\n── Computrabajo ─────────────");
        todas.addAll(scrapeComputrabajo());

        return todas;
    }

    public List<VacanteRaw> scrapeOCC() {
        List<VacanteRaw> vacantes = new ArrayList<>();
        WebDriver driver = crearDriver();

        try {
            driver.get("https://www.occ.com.mx/empleos/de-ingeniero-software/?ubicacion=tijuana-baja-california");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-id]")
            ));

            // Agarramos cada tarjeta completa por su data-id
            List<WebElement> tarjetas = driver.findElements(
                    By.cssSelector("div[data-id][id^='jobcard-']")
            );

            System.out.println("📋 Tarjetas encontradas: " + tarjetas.size());

            for (WebElement tarjeta : tarjetas) {
                // Extraer data-id del div contenedor
                String dataId = tarjeta.getAttribute("data-id");

                // Extraer título con el selector exacto que inspeccionaste
                String titulo = "";
                try {
                    WebElement h2 = tarjeta.findElement(By.cssSelector("h2.text-grey-900"));
                    titulo = h2.getText().trim();
                } catch (NoSuchElementException e) {
                    continue; // tarjeta sin título, la saltamos
                }

                // Extraer salario (puede no existir)
                String salario = "";
                try {
                    WebElement salarioEl = tarjeta.findElement(By.cssSelector("span.font-light.mb-2, span.font-light.sm\\:mb-4"));
                    salario = salarioEl.getText().trim();
                } catch (NoSuchElementException e) {
                    salario = "No especificado";
                }

                // Extraer empresa
                String empresa = "";
                try {
                    WebElement empresaEl = tarjeta.findElement(By.cssSelector("a[href*='/empleos/bolsa-de-trabajo']"));
                    empresa = empresaEl.getText().trim();
                } catch (NoSuchElementException e) {
                    empresa = "No especificada";
                }

                // Extraer ubicación
                String ubicacion = "";
                try {
                    WebElement ubicacionEl = tarjeta.findElement(By.cssSelector("p.text-grey-900.text-sm"));
                    ubicacion = ubicacionEl.getText().trim();
                } catch (NoSuchElementException e) {
                    ubicacion = "No especificada";
                }

                // Construir URL con el data-id
                String url = "https://www.occ.com.mx/empleo/" + dataId + "/";

                if (!titulo.isEmpty() && dataId != null) {
                    VacanteRaw v = new VacanteRaw(titulo, url, salario, empresa, ubicacion, dataId);
                    vacantes.add(v);

                    System.out.println("─────────────────────────────");
                    System.out.println("  📌 " + titulo);
                    System.out.println("  🏢 " + empresa);
                    System.out.println("  📍 " + ubicacion);
                    System.out.println("  💰 " + salario);
                    System.out.println("  🔗 " + url);
                }
            }

        } catch (TimeoutException e) {
            System.out.println("⏱ Timeout esperando tarjetas");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return vacantes;
    }

    public List<VacanteRaw> scrapeComputrabajo() {
        List<VacanteRaw> vacantes = new ArrayList<>();
        WebDriver driver = crearDriver();

        try {
            driver.get("https://mx.computrabajo.com/trabajo-de-desarrollador-de-software-en-tijuana");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("article[data-id]")
            ));

            List<WebElement> tarjetas = driver.findElements(
                    By.cssSelector("article[data-id]")
            );

            System.out.println("📋 [Computrabajo] Tarjetas encontradas: " + tarjetas.size());

            for (WebElement tarjeta : tarjetas) {
                String dataId = tarjeta.getAttribute("data-id");

                String titulo = "";
                try {
                    titulo = tarjeta.findElement(By.cssSelector("h2 a, h3 a, a.js-o-link"))
                            .getText().trim();
                } catch (NoSuchElementException e) { continue; }

                String empresa = "";
                try {
                    empresa = tarjeta.findElement(
                            By.cssSelector("p.fs16, span.fs16, a[title]")).getText().trim();
                } catch (NoSuchElementException e) { empresa = "No especificada"; }

                String ubicacion = "";
                try {
                    ubicacion = tarjeta.findElement(
                            By.cssSelector("p.fs13, span.fs13")).getText().trim();
                } catch (NoSuchElementException e) { ubicacion = "No especificada"; }

                String url = "";
                try {
                    url = tarjeta.findElement(By.cssSelector("h2 a, h3 a, a.js-o-link"))
                            .getAttribute("href");
                    if (url != null && url.startsWith("/")) {
                        url = "https://mx.computrabajo.com" + url;
                    }
                } catch (NoSuchElementException e) { url = "sin-url"; }

                String salario = "";
                try {
                    salario = tarjeta.findElement(
                                    By.cssSelector("p.salary, span.salary, p[class*='salary']"))
                            .getText().trim();
                } catch (NoSuchElementException e) { salario = "No especificado"; }

                if (!titulo.isEmpty()) {
                    vacantes.add(new VacanteRaw(titulo, url, salario, empresa,
                            ubicacion, "ct-" + dataId));
                    System.out.println("  ✅ " + titulo + " | " + empresa);
                }
            }

        } catch (TimeoutException e) {
            System.out.println("⏱ [Computrabajo] Timeout — inspeccionemos el HTML");
            // Plan B: imprimir el HTML para ver qué llegó
            System.out.println(driver.getPageSource().substring(0, 2000));
        } catch (Exception e) {
            System.out.println("❌ [Computrabajo] Error: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return vacantes;
    }

    private WebDriver crearDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        return new ChromeDriver(options);
    }

    public record VacanteRaw(
            String titulo,
            String url,
            String salario,
            String empresa,
            String ubicacion,
            String externalId   // el data-id de OCC — clave para detectar duplicados
    ) {}
}
