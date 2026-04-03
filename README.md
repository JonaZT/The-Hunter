# 🎯 The Hunter — Job Vacancy Scraper

Automated system built with **Spring Boot + Selenium** that scrapes job boards every 30 minutes, filters relevant vacancies, persists them to avoid duplicates, and sends real-time notifications via **Telegram Bot**.

Built to solve a real problem: automating the search for software internships and junior positions in Tijuana, BC.

---

## 🚀 Features

- **Multi-source scraping** — OCC Mundial and Computrabajo scraped simultaneously
- **Smart filtering** — keyword-based engine filters by role, location, and relevance
- **Deduplication** — each vacancy is stored by external ID; already-seen vacancies are never re-notified
- **Telegram notifications** — instant alerts sent directly to your phone when a new match is found
- **Fully automated** — runs on a 30-minute schedule with no manual intervention required

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Browser automation | Selenium WebDriver 4.x |
| Persistence | Spring Data JPA + H2 (file-based) |
| Scheduling | Spring `@Scheduled` |
| Notifications | Telegram Bot API |
| Language | Java 17+ |

---

## 🏗️ Architecture

```
@Scheduled (every 30 min)
        │
        ▼
 ScraperService  ──────────────────────────────────┐
 ├── scrapeOCC()          Selenium navigates OCC    │
 └── scrapeComputrabajo() Selenium navigates CT     │
        │                                           │
        ▼                                           │
  FilterEngine                                      │
  ├── keyword filter  (software, backend, java...)  │
  ├── location filter (Tijuana, remoto, México)     │
  └── exclusion filter (CDMX, Monterrey, etc.)      │
        │                                           │
        ▼                                           │
 VacanteRepository                                  │
 └── existsByExternalId()  ── already seen? skip ──┘
        │ new vacancy
        ▼
  repository.save()
        │
        ▼
 NotifierService
 └── Telegram Bot API  →  📱 your phone
```

---

## ⚙️ Setup

### 1. Prerequisites

- Java 17+
- Chrome browser installed
- ChromeDriver matching your Chrome version
- A Telegram bot token (see below)

### 2. Create a Telegram Bot

1. Open Telegram and search for **@BotFather**
2. Send `/newbot` and follow the prompts
3. Copy the **token** BotFather gives you
4. Send any message to your new bot
5. Visit `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
6. Copy the `chat.id` value from the JSON response

### 3. Configure credentials

Edit `src/main/resources/application.properties`:

```properties
telegram.bot.token=YOUR_TOKEN_HERE
telegram.bot.chatId=YOUR_CHAT_ID_HERE
```

### 4. Run

```bash
./mvnw spring-boot:run
```

The scheduler fires automatically. Check your Telegram for alerts.

---

## 📁 Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Entry point, @EnableScheduling
├── scraper/
│   └── ScraperService.java       # Selenium scraping logic (OCC + Computrabajo)
├── filter/
│   └── FilterEngine.java         # Keyword & location filtering
├── model/
│   └── Vacante.java              # JPA entity
├── repository/
│   └── VacanteRepository.java    # Spring Data repository
├── notifier/
│   └── NotifierService.java      # Telegram Bot API integration
└── scheduler/
    └── HunterScheduler.java      # @Scheduled orchestrator
```

---

## 🔍 How Filtering Works

The `FilterEngine` applies three independent rules:

**Include** — title must contain at least one of:
`software`, `backend`, `frontend`, `fullstack`, `java`, `developer`, `desarrollador`, `programador`, `sistemas`, `web`

**Location accepted:**
`tijuana`, `baja california`, `remoto`, `remote`, `híbrido`, `home office`
or `México` (country-level, likely remote)

**Exclude** — title or location matching:
`ciudad de méxico`, `cdmx`, `monterrey`, `querétaro`, `arquitecto`, `firmware`, `obra`, `tooling`

---

## 📬 Sample Notification

```
🎯 Nueva vacante encontrada

📌 Desarrollador de Software Jr
🏢 Empresa Tijuana
📍 Tijuana, Baja California
💰 $ 18,000 - $ 22,000 Mensual
🔗 https://www.occ.com.mx/empleo/12345678/
```

---

## 🗄️ Database

Uses **H2 file-based** database stored locally at `./data/hunterdb.mv.db`.

The database persists between restarts — vacancies already notified are never re-sent.
To reset and re-trigger notifications, delete the `data/` folder.

To inspect the database while the app is running, open:
`http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:file:./data/hunterdb`

---

## 🔧 Extending to New Sources

Adding a new job board requires only one new method in `ScraperService`:

```java
public List<VacanteRaw> scrapeNewSite() {
    // navigate, extract, return VacanteRaw list
}
```

Then add it to `scrapeTodo()`. The filter, deduplication, and notification layers require zero changes.

---

## 📌 Roadmap

- [ ] Add UABC job board (local Tijuana source)
- [ ] LinkedIn Jobs scraping
- [ ] PostgreSQL for production persistence
- [ ] REST endpoint to query stored vacancies
- [ ] Docker support

