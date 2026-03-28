---
name: javlo-template
description: Use when creating or modifying a Javlo CMS template — includes building index.html layouts, config.properties, dynamic components, i18n files, image filters, staging setup, and MCP-based content population. Triggers on Figma designs, site screenshots, or explicit template creation requests.
---

# Javlo Template Creation

## Overview

A Javlo template is a folder of plain HTML, CSS/SCSS, JS, and `.properties` files. Javlo converts each `.html` file into a `.jsp` at render time and replaces `[area-id]` placeholders with CMS-managed content. The template drives layout; the CMS drives content.

## Inputs Accepted

This skill handles:
- **Figma URL** — use `figma:figma-implement-design` skill to extract specs, then apply here
- **Site screenshots** — infer layout, colors, section structure
- **Existing site URL** — fetch HTML, extract structure and design tokens
- **Description** — brainstorm with user to define sections and components

---

## File Structure

```
my-template/
├── config.properties          # REQUIRED: areas, colors, fonts, grid
├── private-config.properties  # CMS-generated IDs (don't edit manually)
├── image-config.properties    # Image filter definitions
├── staging.properties         # Optional: staging server URL + token
├── index.html                 # Default layout (all pages)
├── home.html                  # Homepage-specific layout (optional)
├── css/
│   └── style.css              # Compiled CSS (or use scss/)
├── scss/                      # Sass — auto-compiled by Javlo or Live Sass Compiler
├── js/                        # Vanilla JS ES6+ (no jQuery)
├── img/                       # Static template images
├── images/                    # SVG/placeholder images
├── components/                # DynamicComponent definitions (.properties + .html)
├── components-config/         # Component renderer overrides
├── jsp/                       # JSP includes (menu.jsp, breadcrumb.jsp, etc.)
│   └── components/            # JSP renderers for built-in component types
└── i18n/                      # Translation files (view_fr.properties, etc.)
    ├── view_fr.properties
    ├── view_nl.properties
    └── view_en.properties
```

---

## config.properties

```properties
# Required
html=index.html
html.home=home.html        # Homepage override (if different layout)
parent=default             # Inherit from parent template

# Areas — map logical name to HTML element id
area.header=header
area.content=content
area.footer=footer
area.blog=blog             # Optional sections

# Area containers (the wrapper div id)
area-container.header=header-container
area-container.blog=blog-container

# Areas that hide their wrapper when empty
area-quietable=blog,aside,banner

# Design tokens — referenced as @@color.primary@@ in HTML/SCSS
data.color.primary=E43D30
data.color.secondary=1A1A1A
data.font.heading="Inter"
data.font.text="Inter"

# Bootstrap column classes
columnable.col.class.3=component-col col-lg-3 col-sm-6
columnable.col.class.4=component-col col-lg-4 col-sm-6
columnable.col.class.6=component-col col-lg-6
columnable.row.class=row

# Bootstrap version
bootstrap.verion=5

# Image filter names available in image-config.properties
images-filter=standard;hero-banner;card;square;thumbnail
```

---

## index.html

Write **plain HTML** — no JSP directives. Javlo adds them. Use `[area-id]` as the injection point for CMS content.

```html
<!DOCTYPE html>
<html lang="${info.languageAndCountry}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- Hreflang for multilingual SEO -->
  <c:forEach var="lg" items="${info.languageRealContentAbsoluteURLs.keySet()}">
    <link rel="alternate" hreflang="${lg}" href="${info.languageRealContentAbsoluteURLs[lg]}"/>
  </c:forEach>

  <title>${info.pageTitle}<c:if test="${info.pageTitle != info.globalTitle}"> – ${info.globalTitle}</c:if></title>

  <link rel="stylesheet" href="/css/style.css">
  <jsp:include page="jsp/social_meta.jsp"/>
</head>
<body class="section-${info.section} page-${info.pageName} depth-${info.page.depth}">

  <!-- Navigation -->
  <jsp:include page="jsp/menu.jsp"/>

  <!-- Hero / Header area -->
  <div id="header-container">
    <div id="header">[header]</div>
  </div>

  <!-- Main content -->
  <div id="content">[content]</div>

  <!-- Optional: blog section (hidden when empty) -->
  <div id="blog-container">
    <div id="blog">[blog]</div>
  </div>

  <!-- Footer -->
  <footer id="footer">[footer]</footer>

  <script src="/js/mobile-menu.js"></script>
  <script src="/js/animations.js"></script>
  <jsp:include page="jsp/end_body.jsp"/>
</body>
</html>
```

### Key EL Variables in index.html

See [template-api.md](template-api.md) for the full reference. Most used:

| Expression | Value |
|---|---|
| `${info.languageAndCountry}` | `fr-be`, `en-gb` ... |
| `${info.pageTitle}` | Current page title |
| `${info.globalTitle}` | Site name |
| `${info.pageName}` | Page slug (`home`, `about` ...) |
| `${info.section}` | Section slug |
| `${info.page.depth}` | Depth in nav tree (1 = root) |
| `${info.rootURL}` | Site root URL |
| `${info.currentURL}` | Current page URL |
| `${info.year}` | Current year |
| `${info.areaEmpty['blog']}` | True if area has no content |
| `${editPreview}` | True in CMS preview/edit mode |
| `${vi18n['key']}` | Translation string |

### Conditional layout by page

```html
<c:choose>
  <c:when test="${info.pageName == 'home'}">
    <!-- Homepage-specific markup -->
  </c:when>
  <c:otherwise>
    <!-- Inner page markup -->
  </c:otherwise>
</c:choose>
```

---

## Dynamic Components

**Rule**: logically related fields (e.g. title + image + text) belong in one dynamic component, not scattered across separate CMS components.

### Component file pair: `components/article.properties` + `components/article.html`

**article.properties**
```properties
component.type=article
component.label=Article card
component.renderer=components/article.html
component.wrapped=true

field.h2.title.type=h2
field.h2.title.label=Title
field.h2.title.order=100

field.image.photo.type=image
field.image.photo.label=Photo
field.image.photo.order=200

field.text.body.type=wysiwyg-text
field.text.body.label=Body text
field.text.body.order=300

field.internal-link.cta.type=internal-link
field.internal-link.cta.label=CTA button
field.internal-link.cta.order=400
```

**article.html**
```html
<div class="article-card ${previewCSS}" id="${previewID}">
  <div class="article-card__img">
    <img src="${field.image.photo.url}" alt="${field.image.photo.alt}">
  </div>
  <div class="article-card__body">
    <h2>${field.h2.title.value}</h2>
    <div>${field.text.body.html}</div>
    <c:if test="${not empty field.internal-link.cta.link}">
      <a href="${field.internal-link.cta.link}" class="btn btn-primary">
        ${field.internal-link.cta.linkLabel}
      </a>
    </c:if>
  </div>
</div>
```

### Grouping repeated items (e.g. a feature list)

```properties
field.text.feature.type=text
field.text.feature.group=item
field.text.feature.order=100

field.image.icon.type=image
field.image.icon.group=item
field.image.icon.order=200
```

```html
<!-- start-member -->
<li>
  <img src="${field.image.icon.url}" alt="">
  <span>${field.text.feature.value}</span>
</li>
<!-- end-member -->
```

### Batching adjacent same-type components (section wrapper)

When several instances of the same component appear in a row, wrap them in a single `<section>`:

```html
<c:if test="${!previousSame}">
  <section class="cards-section">
    <div class="container">
</c:if>

<div class="card ${previewCSS}" id="${previewID}">
  <!-- card content -->
</div>

<c:if test="${!nextSame}">
    </div>
  </section>
</c:if>
```

### Image filter in component

```html
<jv:changeFilter var="imgUrl"
  url="${field.image.photo.url}"
  filter="standard"
  newFilter="card"/>
<img src="${imgUrl}" alt="${field.image.photo.alt}">
```

---

## Image Filters (`image-config.properties`)

```properties
# Hero banner
hero-banner.width=1920
hero-banner.height=600
hero-banner.crop-resize=true

hero-banner.mobile.width=768
hero-banner.mobile.height=600
hero-banner.mobile.crop-resize=true

# Cards
card.width=800
card.height=500
card.crop-resize=true

# Squared thumbnails
square.width=600
square.height=600
square.crop-resize=true

# Standard (generic)
standard.width=1200
standard.height=800
standard.crop-resize=true
```

---

## i18n Files

**Format**: `i18n/view_<lang>.properties` (Java ResourceBundle)

```properties
# view_fr.properties
nav.home=Accueil
nav.about=A propos
nav.find-dealer=Trouver un revendeur
nav.quote=Devis
footer.copyright=Tous droits reserves
hero.cta=Decouvrir
aria.open-menu=Ouvrir le menu
aria.close-menu=Fermer le menu
url.dealer-locator=/fr/revendeurs
url.quote=/fr/devis
```

Access in templates:
```html
${vi18n['nav.quote']}

<!-- With null-safe fallback -->
${vi18n['nav.quote'] != null ? vi18n['nav.quote'] : 'Devis'}
```

Primary language: `view_fr.properties`. Other languages inherit missing keys from primary.

---

## staging.properties

Place in the template root to connect a staging Javlo instance. Used with the Javlo2 MCP server to populate content via Claude.

```properties
# staging.properties
javlo.url=http://staging.example.com/javlo2/context-name/
javlo.token=YOUR_TOKEN_HERE
javlo.lang=fr
```

The Javlo2 MCP server (located in `mcp/` at the root of the Javlo repository) reads these and exposes tools for content management:
- `nav_add` / `nav_remove` / `nav_move` — page tree management
- `content_add` / `content_edit` / `content_remove` / `content_move` — component management
- `content_publish` — sync preview to live
- `template_upload` / `template_commit` — deploy template via zip

**MCP env vars** (used by `.mcp.json`):
```json
{
  "JAVLO_BASE_URL": "http://staging.example.com/javlo2/context-name/",
  "JAVLO_TOKEN": "YOUR_TOKEN_HERE",
  "JAVLO_LANG": "fr"
}
```

---

## Code Conventions

- **No jQuery** — vanilla JS ES6+ only
- **CSS**: CSS custom properties for design tokens, BEM-like naming, mobile-first
- **JS**: `const`/`let`, event-driven, `data-*` attributes as JS hooks (not classes)
- **HTML**: `lang="fr-be"` default, ARIA attributes throughout
- **Naming**: CSS classes + files in kebab-case; JS variables in camelCase
- **No inline styles** in template HTML
- SCSS files in `components/` are auto-merged into the main stylesheet by Javlo
- **No line breaks inside HTML tags** — keep each tag on a single line, even if long:
  ```html
  <%-- WRONG --%>
  <div class="${info.areaEmpty['sidebar'] ? 'col-12' : 'col-xl-9'}"
       id="content">

  <%-- CORRECT --%>
  <div class="${info.areaEmpty['sidebar'] ? 'col-12' : 'col-xl-9'}" id="content">
  ```

---

## Common Mistakes

| Mistake | Fix |
|---|---|
| JSP directives in `.html` files | Remove them — Javlo adds `<%@ taglib %>` automatically |
| `${info.currentCanonicalURL}` not resolving | Use `${info.currentURL}` instead |
| Image not filtered correctly | Check `image-config.properties` filter name matches `<jv:changeFilter newFilter="...">` |
| Area content not showing | Verify `area.<name>` in config.properties matches the `id` on the HTML element |
| Mobile menu not working | Ensure `id="nav-burger-btn"` and `id="nav-mobile-panel"` match what `mobile-menu.js` expects |
| Logo not visible | Check path: `/img/logo.png` (absolute) not `img/logo.png` (relative) |
| `previousSame` / `nextSame` wrapper broken | The open and close `<c:if>` blocks must wrap **all** instances without extra conditions between them |
| `private-config.properties` IDs wrong | Let Javlo regenerate it via `template.commit` — don't edit manually |

---

## Quick Checklist for a New Template

1. `config.properties` — areas, colors, fonts, grid classes
2. `image-config.properties` — hero, card, square, thumbnail filters
3. `index.html` — layout with `[area-id]` placeholders, EL variables in `<html lang>` and `<title>`
4. `jsp/menu.jsp` — navigation with `${vi18n[...]}` keys
5. `components/` — one `.properties` + `.html` per logical content block
6. `i18n/view_fr.properties` — all nav, footer, aria, url keys
7. `css/style.css` or `scss/style.scss` — design tokens, layout, components
8. `js/mobile-menu.js` + `js/animations.js` — interaction layer
9. `staging.properties` — if staging server available for MCP-based content work
10. **`home.html`** — standalone browser-previewable page with fictional content (no EL/JSP, all CDN assets)
11. **`visual.png`** — 1440x900 screenshot of `home.html` for template catalogue

---

## home.html — Static Preview Page

`home.html` is a **standalone HTML file** (no JSP/EL) that can be opened directly in a browser. It serves as:
- A visual reference while building the template
- A demo for the client / CMS admin
- The source for `visual.png`

**Rules:**
- No `${info.*}`, no `<c:if>`, no `<jsp:include>` — plain HTML only
- All assets via CDN (same URLs as `index.html`)
- Realistic fictional content: real section titles, plausible data, SVG placeholders for images
- Must match `index.html` layout exactly (same sidebar, nav, areas)

**Minimal structure:**
```html
<!DOCTYPE html>
<html lang="fr">
<head>
  <!-- same CDN links as index.html -->
</head>
<body>
  <!-- sidebar with hardcoded nav items -->
  <!-- main-content with top navbar -->
  <!-- content area with fictional data (cards, table, chart SVG...) -->
  <!-- same scripts as index.html -->
</body>
</html>
```

---

## visual.png — Template Screenshot

A **1440x900 PNG** screenshot of `home.html`, used as a catalogue thumbnail in the Javlo CMS template picker.

**Generate with Puppeteer (Node.js):**

```js
// screenshot.js (delete after use)
const puppeteer = require('puppeteer');
const http = require('http');
const fs = require('fs');
const path = require('path');

function createServer(root, port) {
  const mime = { '.html':'text/html','.css':'text/css','.js':'application/javascript',
                 '.png':'image/png','.svg':'image/svg+xml' };
  return http.createServer((req, res) => {
    const file = path.join(root, req.url === '/' ? '/home.html' : req.url);
    fs.readFile(file, (err, data) => {
      if (err) { res.writeHead(404); res.end(); return; }
      res.writeHead(200, { 'Content-Type': mime[path.extname(file)] || 'application/octet-stream' });
      res.end(data);
    });
  }).listen(port);
}

(async () => {
  const server = createServer(__dirname, 9876);
  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900, deviceScaleFactor: 1.5 });
  await page.goto('http://localhost:9876/home.html', { waitUntil: 'networkidle0', timeout: 45000 });
  await page.evaluate(() => {
    document.querySelectorAll('#loading, .loader, [id*="load"]').forEach(el => el.style.display = 'none');
  });
  await new Promise(r => setTimeout(r, 1000));
  await page.screenshot({ path: 'visual.png', clip: { x:0, y:0, width:1440, height:900 } });
  await browser.close();
  server.close();
  console.log('visual.png created');
})();
```

**Steps:**
```bash
npm init -y && npm install puppeteer
node screenshot.js
rm -rf node_modules package.json package-lock.json screenshot.js
```
