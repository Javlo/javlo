---
name: javlo-content
description: Use when populating pages with content in Javlo CMS via the MCP tools — covers component selection, heading hierarchy, page structure, news/blog patterns with page-reference, column layouts, and publishing. Triggers on requests to add content, create pages, build a site structure, or populate a Javlo site.
---

# Javlo Content Creation

## Overview

This skill guides content creation in Javlo CMS using the Javlo2 MCP server tools (`content_add`, `content_edit`, `nav_add`, etc.). It defines which components to use, how to structure pages, and best practices for building coherent site content.

**Documentation references** (accessible on the target Javlo instance):
- General: `/doc-javlo/index.html` (or https://www.javlo.org/doc-javlo/index.html)
- Templates: `/doc-javlo/template.html`
- Remote API: `/doc-javlo/api.html`

---

## Preferred Components

Use these standard components in priority order. Avoid custom or exotic types unless the template explicitly defines them.

| Component type | MCP `type` value | Purpose |
|---|---|---|
| Heading | `heading` | Page titles, section titles, subtitles |
| Wysiwyg Paragraph | `wysiwyg-paragraph` | Rich text blocks (HTML) |
| Global Image | `global-image` | Images from the media library |
| Internal Link | `internal-link` | Links to other pages of the site |
| External Link | `external-link` | Links to external URLs or emails |
| Page Reference | `page-reference` | Lists of page cards (news, blog, portfolio) |

### Template-specific components (priority)

**Always check the template's `components/` and `components-config/` directories first.** Templates define custom dynamic components tailored to the site's design (e.g., hero banners, service cards, testimonials, CTAs). These components should be **used in priority** over generic ones because:

- They match the template's visual design and responsive behavior
- They group related fields logically (title + image + text + link in one component)
- They have proper renderers with the template's CSS classes and layout

**How to identify them:**
- `components/*.properties` — defines the component type, fields, and their order
- `components/*.html` — the HTML renderer for the component
- `components-config/*.properties` — overrides rendering config for built-in component types (styles, renderers)

**Usage with MCP:** use the component `type` value from the `.properties` file (e.g., `component.type=hero-banner` means `type="hero-banner"` in `content_add`). Available styles and renderers are defined in the same config.

```
# Example: using a template-defined "service-card" component
content_add(page="services", type="service-card", area="content",
  value="title=Consulting|description=Strategic digital consulting|icon=strategy")
```

**Fallback order:**
1. Template dynamic components (`components/`) — use first if one matches the need
2. Template-configured built-in components (`components-config/`) — styled versions of standard types
3. Standard Javlo components (heading, wysiwyg-paragraph, etc.) — when no template component fits

---

## Heading — Titles & Subtitles

Headings are the **primary component for all titles and subtitles**. They render semantic `<h1>` to `<h6>` tags.

### Value format

**IMPORTANT:** The heading value uses **Java Properties format** — each property on its own line, separated by **newline characters (`\n`)**. Do NOT use spaces to separate properties.

```
depth=<1-6>
text=<Main title>
smtext=<Short text / subtitle>
link=<optional URL>
```

**Required:** `depth` and `text`. **Optional:** `smtext`, `link`.

**WRONG** (space-separated — will produce empty headings):
```
value="depth=1 text=Mon titre smtext=Titre"
```

**CORRECT** (newline-separated — Java Properties format):
```
value="depth=1\ntext=Mon titre\nsmtext=Titre"
```

### Rules

1. **One `h1` per page** — the main page title. Use `h2`-`h6` for section headings.
2. **Use `smtext` for the short page name** — this is the preferred place for a concise label (used in navigation, breadcrumbs). Keep it shorter than the main `text`.
3. **Heading hierarchy must be logical** — never skip levels (e.g., `h2` then `h4`).

### Examples

```
# Page title with short name for navigation
content_add(page="about", type="heading", area="content",
  value="depth=1\ntext=A propos de notre entreprise\nsmtext=A propos")

# Section subtitle
content_add(page="about", type="heading", area="content",
  value="depth=2\ntext=Notre histoire")

# Sub-section with smtext
content_add(page="about", type="heading", area="content",
  value="depth=3\ntext=Les fondateurs\nsmtext=Equipe")

# Heading with link
content_add(page="about", type="heading", area="content",
  value="depth=2\ntext=Contactez-nous\nlink=/contact")
```

### When to use smtext

- **Always** on `h1` — provide a concise page name (used for navigation/breadcrumb)
- **Optionally** on `h2`/`h3` — when the full title is long and a short label is useful
- **Rarely** on `h4`-`h6` — usually not needed at deep heading levels

---

## Wysiwyg Paragraph — Rich Text

Use `wysiwyg-paragraph` for any body text. The value is raw HTML.

```
content_add(page="about", type="wysiwyg-paragraph", area="content",
  value="<p>Notre entreprise a ete fondee en 2010 avec la mission de...</p>")
```

### Rules

- Always wrap text in `<p>` tags
- Use semantic HTML: `<strong>` for emphasis, `<ul>`/`<ol>` for lists
- Do NOT use headings (`<h1>`-`<h6>`) inside wysiwyg — use the heading component instead
- Keep paragraphs focused: one topic per component for easy reordering

---

## Global Image

Use `global-image` for displaying images from the Javlo media library.

```
content_add(page="about", type="global-image", area="content",
  value="images/team-photo.jpg")
```

The image path is relative to the site's media library. The template's image filters (defined in `image-config.properties`) control rendering sizes.

---

## Internal Link & External Link

### Internal Link
Links to another page within the same Javlo site:

```
content_add(page="home", type="internal-link", area="content",
  value="about", style="title")
```

**Style options:** `title`, `title+image`, `title+description`, `title+image+description`, `image`, `hidden`

### External Link
Links to an external URL or email:

```
content_add(page="contact", type="external-link", area="content",
  value="https://www.example.com", style="title")
```

---

## Page Reference — Page Lists & News

`page-reference` is the component for displaying lists of sub-pages. It is the **key component for news, blog, portfolio, and any listing pattern**.

### Basic usage

```
content_add(page="home", type="page-reference", area="content")
```

### How renderers work

The available renderers are **defined in the template** configuration. Each template provides its own set of renderers for page-reference (e.g., cards grid, list, carousel, news feed). Check the template's component configuration to see which renderers are available.

```
# With a specific renderer defined in the template
content_add(page="home", type="page-reference", area="content", renderer="news-cards")
```

### News / Blog pattern

For a news or article structure:

1. **Create the parent page** (e.g., `news` or `blog`)
2. **Create child pages** for each article (e.g., `news/article-1`, `news/article-2`)
3. **On each article page**: add a heading (h1 + smtext), a date component, wysiwyg paragraphs, and images
4. **On the home page**: add a `page-reference` component that displays the news children as cards/teasers

```
# Step 1: Create navigation structure
nav_add(name="news")
nav_add(name="article-premier-evenement", parent="news")
nav_add(name="article-nouveau-produit", parent="news")

# Step 2: Populate article pages
content_add(page="article-premier-evenement", type="heading", area="content",
  value="depth=1 text=Premier evenement de l'annee smtext=Evenement 2026")
content_add(page="article-premier-evenement", type="wysiwyg-paragraph", area="content",
  value="<p>Nous sommes ravis de vous annoncer notre premier evenement...</p>")

# Step 3: Add page-reference on home to show news teasers
content_add(page="home", type="page-reference", area="content", renderer="news-cards")
```

### Page Reference properties

- **Automatic mode** (default): displays children of the current page or configured source
- **Manual mode**: specific pages can be referenced by ID
- **Ordering**: by priority, creation date, modification date, or name
- **Pagination**: handled automatically by the template renderer

---

## Column Layout

Components can be placed in a grid layout using the `columnSize` parameter. Javlo uses a **12-column Bootstrap grid**.

### Column sizes

| `columnSize` | Width | Use case |
|---|---|---|
| `12` | Full width | Default, single column |
| `6` | Half width | Two-column layout |
| `4` | Third width | Three-column layout |
| `3` | Quarter width | Four-column layout |
| `8` + `4` | Two-thirds + one-third | Asymmetric layout |

### Example: Two-column layout

```
# Left column: image (half width)
content_add(page="about", type="global-image", area="content",
  value="images/office.jpg", columnSize=6)

# Right column: text (half width)
content_add(page="about", type="wysiwyg-paragraph", area="content",
  value="<p>Notre bureau est situe au coeur de Bruxelles...</p>", columnSize=6)
```

### Example: Three cards

```
content_add(page="home", type="heading", area="content",
  value="depth=2 text=Nos services", columnSize=4)

content_add(page="services", type="wysiwyg-paragraph", area="content",
  value="<p>Conseil en strategie digitale</p>", columnSize=4)

content_add(page="services", type="wysiwyg-paragraph", area="content",
  value="<p>Developpement sur mesure</p>", columnSize=4)

content_add(page="services", type="wysiwyg-paragraph", area="content",
  value="<p>Formation et accompagnement</p>", columnSize=4)
```

### columnStyle

Use `columnStyle` to add CSS classes to the column wrapper:

```
content_add(page="home", type="wysiwyg-paragraph", area="content",
  value="<p>Texte centre</p>", columnSize=6, columnStyle="offset-3")
```

---

## Page Structure Patterns

### Simple page (About, Contact)

```
heading h1 (+ smtext for nav name)
wysiwyg-paragraph (introduction)
heading h2 (section)
wysiwyg-paragraph (body)
global-image (illustration)
wysiwyg-paragraph (more content)
```

### Home page

```
heading h1 (+ smtext for site name)
global-image (hero banner)                    # area: header
heading h2 ("Nos services")                   # area: content
[service cards in columns]
heading h2 ("Actualites")
page-reference (renderer: news-cards)         # shows latest news
heading h2 ("A propos")
wysiwyg-paragraph (short intro)
internal-link (link to about page)
```

### News / Blog listing page

```
heading h1 ("Actualites" smtext="News")
page-reference (shows child articles, with pagination)
```

### News article page

```
heading h1 ("Article title" smtext="Short title")
date (publication date)
global-image (featured image)
wysiwyg-paragraph (article body - multiple paragraphs)
internal-link (back to news listing)
```

---

## Content Workflow

### Typical sequence for building a site

1. **Create navigation** — `nav_add` for all pages (parent/child structure)
2. **Populate content** — `content_add` for each page, top to bottom
3. **Review** — check the preview at `{JAVLO_BASE_URL}/preview/{lang}/{page}`
4. **Publish** — `content_publish` to push changes live

### Ordering components

Components are added in sequence. Use the `previous` parameter to control insertion order:
- `previous="0"` — insert at the beginning of the area (default)
- `previous="{component-id}"` — insert after a specific component

The response from `content_add` returns the new component's `id`, which can be used as `previous` for the next insertion.

### Editing existing content

Use `content_edit` with the component ID:

```
content_edit(id="comp-123", value="depth=1 text=Nouveau titre smtext=Titre")
```

### Clearing and rebuilding

To rebuild a page from scratch:

```
content_clearPage(page="home")
# Then re-add all components
```

---

## Common Mistakes

| Mistake | Fix |
|---|---|
| Using wysiwyg for titles | Use `heading` component with proper depth |
| Missing `smtext` on h1 | Always set a short navigation name via smtext |
| Putting `<h2>` tags inside wysiwyg | Use a separate heading component instead |
| No page-reference on home for news | Add a page-reference with the template's news renderer |
| Forgetting to publish | Always call `content_publish` after changes |
| Column sizes not summing to 12 | Ensure each row's columnSize values total 12 |
| Skipping heading levels | Go h1 > h2 > h3, never h1 > h3 |
| All content in one big wysiwyg | Split into multiple components for reorderability |

---

## MCP Tools Quick Reference

| Tool | Key Parameters | When to use |
|---|---|---|
| `nav_add` | name, parent?, top? | Creating pages |
| `nav_remove` | path | Deleting pages |
| `nav_move` | path, parent | Reorganizing navigation |
| `content_add` | page, type, area, value?, style?, columnSize?, renderer? | Adding components |
| `content_edit` | id, value?, style?, columnSize?, renderer? | Modifying components |
| `content_remove` | id | Deleting a component |
| `content_move` | id, previous, area?, page? | Reordering components |
| `content_clearPage` | page | Wiping a page before rebuild |
| `content_publish` | — | Making changes live |
