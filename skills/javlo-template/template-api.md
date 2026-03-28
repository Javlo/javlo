# Javlo Template API Reference

## ${info.*} Variables

| Expression | Type | Description |
|---|---|---|
| `${info.languageAndCountry}` | String | BCP-47 code: `fr-be`, `en-gb` |
| `${info.language}` | String | Language only: `fr`, `en` |
| `${info.pageTitle}` | String | Current page title |
| `${info.titleOrSubtitle}` | String | Page title or subtitle |
| `${info.globalTitle}` | String | Site/company name |
| `${info.pageName}` | String | Page slug: `home`, `about` |
| `${info.section}` | String | Section slug |
| `${info.page.depth}` | int | Depth in nav tree (1 = root) |
| `${info.rootURL}` | String | Site root URL |
| `${info.currentURL}` | String | Current page full URL |
| `${info.currentCanonicalURL}` | String | Canonical URL |
| `${info.year}` | int | Current year |
| `${info.userName}` | String | Logged-in username (empty if anonymous) |
| `${info.preview}` | boolean | True in preview/edit mode |
| `${info.device.pdf}` | boolean | True during PDF export |
| `${info.areaEmpty['blog']}` | boolean | True if area has no components |
| `${info.imageHeader}` | String | Page header image URL |
| `${info.logoUrl}` | String | Site logo URL |
| `${info.languageRealContentAbsoluteURLs}` | Map | `lang -> absolute URL` for hreflang |
| `${info.languageRealContentURLs}` | Map | `lang -> relative URL` |
| `${info.languageURLs[lg]}` | String | URL for specific language |
| `${info.languages}` | Collection | Available languages |
| `${info.otherLocaleForLanguage}` | Collection | Country variants |
| `${info.pageByName['pagename']}` | PageBean | Get any page by slug |
| `${info.page.contentAsMap.dynamicComponent.TYPE.FIELD.value}` | String | DynamicComponent field on current page |
| `${info.changeLangPopup}` | boolean | Should show country-select popup |
| `${info.changeLangMessage}` | String | Localized message for popup |
| `${info.countryDisplay}` | String | Current country display name |
| `${info.template.colorPalette}` | boolean | Template has color palette |
| `${info.template.colorsMap}` | List | Palette color values |
| `${info.rootTemplateURL}` | String | URL to template root folder |

## Component Renderer Variables

Available in all component `.html` and `.jsp` renderers:

| Variable | Type | Description |
|---|---|---|
| `${comp}` | IContentVisualComponent | Component instance |
| `${compPage}` | PageBean | Page containing the component |
| `${style}` | String | Editor-selected style class |
| `${compid}` | String | Unique component ID |
| `${value}` | String | Raw component value |
| `${renderer}` | String | Active renderer name |
| `${previewAttributes}` | String | `id="..." class="..."` for edit mode |
| `${previewCSS}` | String | Edit hook + component CSS class |
| `${previewClass}` | String | Edit-mode CSS class only |
| `${previewID}` | String | HTML id for edit mode |
| `${cssStyle}` | String | Inline CSS set by editor |
| `${cssClass}` | String | CSS class set by editor |
| `${manualCssClass}` | String | Manual CSS class from editor |
| `${previousSame}` | boolean | Previous component is same type |
| `${nextSame}` | boolean | Next component is same type |
| `${editPreview}` | boolean | True in page-preview mode |
| `${containerId}` | String | Component container ID |

**Heading-specific:** `${title}`, `${depth}` (1-6)
**Wysiwyg-specific:** `${text}` (HTML with resolved links)

## Field Type Reference

### Text types
| Type | Accessor | Notes |
|---|---|---|
| `h1`...`h6` | `.value`, `.html` | Heading at specified level |
| `text` | `.value`, `.html`, `.XHTMLValue` | Single-line plain text |
| `large-text` | `.value`, `.html` | Multi-line text |
| `wysiwyg-text` | `.html`, `.text` | Rich editor; modes: `soft`, `normal`, `middle`, `high` |

### Media types
| Type | Accessor | Notes |
|---|---|---|
| `image` | `.url`, `.alt`, `.viewURL`, `.previewUrl` | Image upload |
| `file` | `.resourceUrl`, `.previewUrl`, `.alt`, `.link` | File upload |

### Input types
| Type | Accessor | Notes |
|---|---|---|
| `boolean` | `.value` -> `"true"/"false"` | Checkbox |
| `number` | `.value` | Numeric; `.min`, `.max` for range |
| `color` | `.value` | Color picker |
| `date` | `.displayValue`, `.sortableDate`, `.shortDate`, `.mediumDate` | Date picker |

### Link types
| Type | Accessor | Notes |
|---|---|---|
| `internal-link` | `.link`, `.linkLabel` | Link to site page |
| `external-link` | `.link`, `.linkLabel` | External URL + title |

### Selection types
| Type | Notes |
|---|---|
| `list-one` | Single-select from `list.*` keys |
| `open-list` | Site content + custom values |
| `open-multi-list` | Multi-select open list |

### Field Accessor Reference

```jsp
${field.text.FIELDNAME.value}          <!-- Raw stored value -->
${field.text.FIELDNAME.html}           <!-- HTML output -->
${field.text.FIELDNAME.XHTMLValue}     <!-- Fully rendered XHTML -->
${field.text.FIELDNAME.displayValue}   <!-- Formatted for display -->
${field.h2.FIELDNAME.value}            <!-- Heading text -->
${field.image.FIELDNAME.url}           <!-- Image URL -->
${field.image.FIELDNAME.previewUrl}    <!-- Image preview URL -->
${field.image.FIELDNAME.alt}           <!-- Alt text -->
${field.internal-link.FIELDNAME.link}  <!-- Link href -->
${field.internal-link.FIELDNAME.linkLabel}  <!-- Link text -->
${field.wysiwyg-text.FIELDNAME.html}   <!-- Rich text HTML -->
${field.boolean.FIELDNAME.value}       <!-- "true" or "false" -->
${field.number.FIELDNAME.value}        <!-- Number as string -->
${field.list-one.FIELDNAME.value}      <!-- Selected list key -->
```

## Page Reference Renderer Variables

Used in `jsp/components/page-reference/RENDERER.jsp`:

| Variable | Type | Description |
|---|---|---|
| `${pages}` | Collection<SmartPageBean> | Selected pages |
| `${title}` | String | Component title |
| `${linkTitle}` | String | "See all" link label |
| `${pagination}` | PaginationContext | Pagination state |
| `${tags}` | Collection<String> | Available tags |
| `${months}` | List<String> | Available months |
| `${interactive}` | boolean | AJAX mode |
| `${jsonUrl}` | String | JSON endpoint URL |

### SmartPageBean Properties

```jsp
${page.title}          <!-- Plain title -->
${page.htmlTitle}      <!-- HTML title -->
${page.description}    <!-- Meta description -->
${page.url}            <!-- Full URL -->
${page.date}           <!-- Publication date (DateBean) -->
${page.imagePath}      <!-- Main image path -->
${page.images}         <!-- Collection of Image objects (.url, .description) -->
${page.tags}           <!-- Collection of tag strings -->
${page.firstTag}       <!-- First tag key -->
${page.firstTagLabel}  <!-- First tag display label -->
${page.category}       <!-- Page category -->
${page.authors}        <!-- Author info -->
${page.children}       <!-- Child SmartPageBean objects -->
${page.realContent}    <!-- True if page has actual content -->
${page.contentAsMap.dynamicComponent.TYPE.FIELD.value}
```

## MCP Tools (Javlo2 MCP Server)

Config: `JAVLO_BASE_URL`, `JAVLO_TOKEN`, `JAVLO_LANG` env vars.

| Tool | Parameters | Description |
|---|---|---|
| `nav_add` | `name`, `parent?`, `top?` | Add page to nav tree |
| `nav_remove` | `path` | Delete page and children |
| `nav_move` | `path`, `parent`, `previousSibling?` | Move page |
| `content_add` | `page`, `type`, `area`, `previous?`, `value?`, `style?` | Add component |
| `content_edit` | `id`, `value?`, `style?` | Edit component |
| `content_remove` | `id` | Delete component |
| `content_move` | `id`, `previous`, `area?`, `page?` | Move component |
| `content_publish` | — | Publish: sync preview to live |
| `content_clearPage` | `page` | Remove all components from page |
| `template_upload` | `name`, `url` or `file` | Install template from zip |
| `template_commit` | `name` | Clear render cache |
| `template_commitAll` | `name` | Commit parent + all children |
