#!/usr/bin/env node
/**
 * Javlo2 MCP Server
 *
 * Exposes navigation and content management tools via the Model Context Protocol.
 * Calls the Javlo2 AjaxServlet (/ajax/*) which returns JSON responses.
 *
 * Configuration — priority order (highest first):
 *   1. javlo_connect tool  — set at runtime by a skill or prompt
 *   2. javlo2.config.json  — file next to this script (gitignored), re-read on every call
 *   3. Environment variables (lowest priority / fallback)
 *
 * Environment variables:
 *   JAVLO_BASE_URL  — base URL of the Javlo2 instance (default: http://localhost/javlo2)
 *   JAVLO_TOKEN     — user token for authentication (sent as Authorization: Bearer header)
 *   JAVLO_LANG      — content language (default: fr)
 *
 * javlo2.config.json format (all fields optional):
 *   { "baseUrl": "https://mysite.com/javlo2", "token": "xxx", "lang": "fr" }
 *
 * Authentication priority (server-side):
 *   1. Authorization: Bearer <token>  (preferred)
 *   2. X-Javlo-Token: <token>
 *   3. j_token POST body parameter     (legacy fallback)
 *
 * Prerequisites in Javlo2:
 *   - loginWithToken must be enabled in static config
 *   - The token must belong to a user with the "content" role
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";
import { basename, dirname, join } from "path";

// ─── Configuration ────────────────────────────────────────────────────────────

const __dirname = dirname(fileURLToPath(import.meta.url));
const CONFIG_FILE = join(__dirname, "..", "javlo2.config.json");

interface JavloConfig {
  baseUrl?: string;
  token?:   string;
  lang?:    string;
}

/** Read javlo2.config.json next to package.json (silently ignored if absent). */
function readConfigFile(): JavloConfig {
  try {
    return JSON.parse(readFileSync(CONFIG_FILE, "utf8")) as JavloConfig;
  } catch {
    return {};
  }
}

/** Session override set by the javlo_connect tool (highest priority). */
let sessionConfig: JavloConfig = {};

/** Resolve active config: session > file > env > defaults. */
function getConfig(): Required<JavloConfig> {
  const file = readConfigFile();
  return {
    baseUrl: (sessionConfig.baseUrl ?? file.baseUrl ?? process.env.JAVLO_BASE_URL ?? "http://localhost/javlo2").replace(/\/$/, ""),
    token:   sessionConfig.token   ?? file.token   ?? process.env.JAVLO_TOKEN ?? "",
    lang:    sessionConfig.lang    ?? file.lang    ?? process.env.JAVLO_LANG  ?? "fr",
  };
}

// ─── HTTP helper ──────────────────────────────────────────────────────────────

interface JavloResponse {
  data?:        Record<string, unknown>;
  messageText?: string;
  messageType?: string;
}

async function callAction(
  webaction: string,
  params: Record<string, string>,
  file?: { field: string; path: string }
): Promise<Record<string, unknown>> {
  const { baseUrl, token, lang } = getConfig();
  const ajaxUrl = `${baseUrl}/ajax/${lang}/`;

  const headers: Record<string, string> = {};
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let body: string | FormData;
  if (file) {
    // Multipart : le Content-Type (avec boundary) est posé par fetch.
    const form = new FormData();
    form.append("webaction", webaction);
    for (const [key, value] of Object.entries(params)) {
      form.append(key, value);
    }
    form.append(file.field, new Blob([readFileSync(file.path)]), basename(file.path));
    body = form;
  } else {
    headers["Content-Type"] = "application/x-www-form-urlencoded";
    body = new URLSearchParams({ webaction, ...params }).toString();
  }

  const res = await fetch(ajaxUrl, {
    method:  "POST",
    headers,
    body,
  });

  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }

  const json = (await res.json()) as JavloResponse;

  // Les erreurs d'action (GenericMessage.ERROR) sont sérialisées en "danger".
  if (json.messageType === "error" || json.messageType === "danger") {
    throw new Error(json.messageText ?? "Javlo returned an error");
  }

  return json.data ?? {};
}

function ok(data: unknown): { content: [{ type: "text"; text: string }] } {
  return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
}

// ─── MCP Server ───────────────────────────────────────────────────────────────

const server = new McpServer({
  name:    "javlo2",
  version: "2.3.6.1",
});

// ── Connection tool ───────────────────────────────────────────────────────────

server.registerTool(
  "javlo_connect",
  {
    description: "Définit le serveur Javlo2 et le token d'authentification pour toutes les requêtes suivantes de cette session. Surcharge les variables d'environnement et le fichier javlo2.config.json. Appeler en début de session quand le serveur cible n'est pas localhost.",
    inputSchema: {
      baseUrl: z.string().optional().describe("URL de base du serveur Javlo2, ex: 'https://monsite.com/javlo2'. Laisser vide pour réinitialiser."),
      token:   z.string().optional().describe("Token d'authentification Bearer. Laisser vide pour réinitialiser."),
      lang:    z.string().optional().describe("Langue du contexte de contenu (défaut: 'fr')."),
    },
  },
  async ({ baseUrl, token, lang }) => {
    sessionConfig = {
      ...(baseUrl !== undefined ? { baseUrl } : {}),
      ...(token   !== undefined ? { token   } : {}),
      ...(lang    !== undefined ? { lang    } : {}),
    };
    const active = getConfig();
    return ok({ connected: true, baseUrl: active.baseUrl, lang: active.lang, tokenSet: !!active.token });
  }
);

// ── Navigation tools ──────────────────────────────────────────────────────────

server.registerTool(
  "nav_add",
  {
    description: "Ajoute une nouvelle page dans la navigation Javlo2.",
    inputSchema: {
      name:   z.string().describe("Nom (slug) de la nouvelle page, ex: 'ma-page'"),
      parent: z.string().optional().describe("ID, nom ou chemin de la page parente (défaut = racine)"),
      top:    z.boolean().optional().describe("Insérer en tête de la liste enfant (défaut: false)"),
    },
  },
  async ({ name, parent, top }) => {
    const params: Record<string, string> = { name };
    if (parent) params.parent = parent;
    if (top !== undefined) params.top = String(top);
    const data = await callAction("nav.add", params);
    return ok(data);
  }
);

server.registerTool(
  "nav_remove",
  {
    description: "Supprime une page et tous ses enfants (opération irréversible).",
    inputSchema: {
      path: z.string().describe("ID, nom ou chemin de la page à supprimer"),
    },
  },
  async ({ path }) => {
    const data = await callAction("nav.remove", { path });
    return ok(data);
  }
);

server.registerTool(
  "nav_move",
  {
    description: "Déplace une page vers un autre parent dans l'arborescence.",
    inputSchema: {
      path:            z.string().describe("ID, nom ou chemin de la page à déplacer"),
      parent:          z.string().describe("ID, nom ou chemin du nouveau parent"),
      previousSibling: z.string().optional().describe("Insérer après ce sibling (optionnel, défaut = en premier)"),
    },
  },
  async ({ path, parent, previousSibling }) => {
    const params: Record<string, string> = { path, parent };
    if (previousSibling) params.previousSibling = previousSibling;
    const data = await callAction("nav.move", params);
    return ok(data);
  }
);

server.registerTool(
  "nav_get",
  {
    description: "Retourne les propriétés d'une page (visibilité, type, template, dates de publication, rôles, taxonomie…).",
    inputSchema: {
      path: z.string().describe("ID, nom ou chemin de la page"),
    },
  },
  async ({ path }) => {
    const data = await callAction("nav.get", { path });
    return ok(data);
  }
);

server.registerTool(
  "nav_edit",
  {
    description: "Modifie les propriétés d'une page (équivalent du panneau 'page properties'). Seuls les paramètres fournis sont modifiés. Retourne les propriétés mises à jour.",
    inputSchema: {
      path:                z.string().describe("ID, nom ou chemin de la page"),
      name:                z.string().optional().describe("Nouveau nom (slug) de la page, sans espace ni '.'"),
      visible:             z.boolean().optional().describe("Visible dans la navigation"),
      active:              z.boolean().optional().describe("Page active (une page inactive n'est pas affichée). Dans la réponse, 'active' est l'état effectif (dates de publication et parents compris)."),
      model:               z.boolean().optional().describe("Page modèle"),
      admin:               z.boolean().optional().describe("Page d'administration"),
      breakRepeat:         z.boolean().optional().describe("Bloque les composants répétés des pages parentes"),
      childrenAssociation: z.boolean().optional().describe("Les enfants forment une seule page (association)"),
      type:                z.string().optional().describe("Type de page (ex: 'default', 'article'…)"),
      seoWeight:           z.number().int().min(-1).max(3).optional().describe("Poids SEO : -1 hérité, 0 noindex, 1 faible, 2 normal, 3 élevé"),
      sharedName:          z.string().optional().describe("Nom de partage de la page comme contenu partagé. Chaîne vide pour retirer."),
      template:            z.string().optional().describe("ID du template. Chaîne vide = hérité du parent."),
      startPublish:        z.string().optional().describe("Date de début de publication (ex: '2026-10-01' ou '01/10/2026 08:00'). Chaîne vide pour effacer."),
      endPublish:          z.string().optional().describe("Date de fin de publication. Chaîne vide pour effacer."),
      ipSecurity:          z.string().optional().describe("Nom de la page d'erreur de sécurité IP. Chaîne vide pour effacer."),
      userRoles:           z.array(z.string()).optional().describe("Rôles visiteurs requis pour voir la page (liste vide = page publique). Désactive l'héritage des rôles du parent sauf si userRolesInherited est fourni."),
      userRolesInherited:  z.boolean().optional().describe("true = la page reprend les rôles visiteurs de sa page parente"),
      noValidation:        z.boolean().optional().describe("Pas de validation requise (admin uniquement)"),
      taxonomy:            z.array(z.string()).optional().describe("Nœuds de taxonomie (ID, chemin 'categories > food' ou nom unique). Liste vide + mode 'replace' = aucune."),
      taxonomyMode:        z.enum(["replace", "add", "remove"]).optional().describe("Mode pour 'taxonomy' : replace (défaut), add, remove"),
    },
  },
  async ({ path, userRoles, taxonomy, ...props }) => {
    const params: Record<string, string> = { path };
    for (const [key, value] of Object.entries(props)) {
      if (value !== undefined) params[key] = String(value);
    }
    if (userRoles !== undefined) params.userRoles = userRoles.join(",");
    if (taxonomy  !== undefined) params.taxonomy  = taxonomy.join(",");
    const data = await callAction("nav.edit", params);
    return ok(data);
  }
);

// ── Content (component) tools ─────────────────────────────────────────────────

server.registerTool(
  "content_add",
  {
    description: "Ajoute un composant sur une page Javlo2.",
    inputSchema: {
      page:        z.string().describe("ID, nom ou chemin de la page cible"),
      type:        z.string().describe("Type du composant. Types standards : 'heading', 'wysiwyg-paragraph', 'global-image', 'internal-link', 'external-link', 'page-reference'. Types dynamiques définis dans components/<type>.properties du template (ex: 'large-title')."),
      area:        z.string().describe("Clé de la zone du template, ex: 'main', 'header'"),
      previous:    z.string().optional().describe("ID du composant après lequel insérer ('0' = début, défaut: '0')"),
      value:       z.string().optional().describe("Valeur initiale du composant. Pour les composants dynamiques (DynamicComponent) : format Java Properties, une entrée par ligne — 'field.<name>.value=valeur'. Exemple: 'field.layout.value=main\\nfield.title_step1.value=Mon titre\\nfield.punchline.value=Description'. Pour les champs external-link : 'field.<name>.value.link=https://...' et 'field.<name>.value.label=Texte'. Pour un lien vers une page interne du site : 'field.<name>.value.link=page:#nom-de-la-page#' (jamais une URL /chemin). Les champs disponibles sont définis par 'field.<name>.type' dans le fichier components/<type>.properties du template."),
      style:       z.string().optional().describe("Classe CSS de style"),
      layout:      z.string().optional().describe("Flags de mise en page : l=gauche r=droite c=centre j=justifié b=gras i=italique u=souligné t=barré ; ajouter #font pour la police (ex: 'lcb#Arial')"),
      renderer:    z.string().optional().describe("Clé du renderer défini dans la config du composant"),
      columnSize:  z.number().int().optional().describe("Largeur en colonnes de grille (ex: 6 pour demi-largeur sur 12 colonnes)"),
      columnStyle: z.string().optional().describe("Classe CSS appliquée au wrapper de colonne"),
    },
  },
  async ({ page, type, area, previous, value, style, layout, renderer, columnSize, columnStyle }) => {
    const params: Record<string, string> = { page, type, area };
    if (previous    !== undefined) params.previous    = previous;
    if (value       !== undefined) params.value       = value;
    if (style       !== undefined) params.style       = style;
    if (layout      !== undefined) params.layout      = layout;
    if (renderer    !== undefined) params.renderer    = renderer;
    if (columnSize  !== undefined) params.columnSize  = String(columnSize);
    if (columnStyle !== undefined) params.columnStyle = columnStyle;
    const data = await callAction("content.add", params);
    return ok(data);
  }
);

server.registerTool(
  "content_edit",
  {
    description: "Modifie la valeur, le style, le layout, le renderer ou le colonnage d'un composant existant.",
    inputSchema: {
      id:          z.string().describe("ID du composant à modifier"),
      value:       z.string().optional().describe("Nouvelle valeur du composant. Pour les composants dynamiques : format Java Properties — 'field.<name>.value=valeur' par ligne. Voir description de content_add pour le détail."),
      style:       z.string().optional().describe("Nouvelle classe CSS de style"),
      layout:      z.string().optional().describe("Flags de mise en page (voir content_add). Chaîne vide pour effacer."),
      renderer:    z.string().optional().describe("Clé du renderer. Chaîne vide pour réinitialiser."),
      columnSize:  z.number().int().optional().describe("Largeur en colonnes de grille (ex: 6 pour demi-largeur sur 12 colonnes)"),
      columnStyle: z.string().optional().describe("Classe CSS du wrapper de colonne. Chaîne vide pour effacer."),
      repeat:      z.boolean().optional().describe("Répéter le composant sur toutes les pages enfants"),
    },
  },
  async ({ id, value, style, layout, renderer, columnSize, columnStyle, repeat }) => {
    const params: Record<string, string> = { id };
    if (value       !== undefined) params.value       = value;
    if (style       !== undefined) params.style       = style;
    if (layout      !== undefined) params.layout      = layout;
    if (renderer    !== undefined) params.renderer    = renderer;
    if (columnSize  !== undefined) params.columnSize  = String(columnSize);
    if (columnStyle !== undefined) params.columnStyle = columnStyle;
    if (repeat      !== undefined) params.repeat      = String(repeat);
    const data = await callAction("content.edit", params);
    return ok(data);
  }
);

server.registerTool(
  "content_remove",
  {
    description: "Supprime un composant d'une page.",
    inputSchema: {
      id: z.string().describe("ID du composant à supprimer"),
    },
  },
  async ({ id }) => {
    const data = await callAction("content.remove", { id });
    return ok(data);
  }
);

server.registerTool(
  "content_move",
  {
    description: "Déplace un composant vers une nouvelle position (page, zone, ordre).",
    inputSchema: {
      id:       z.string().describe("ID du composant à déplacer"),
      previous: z.string().describe("ID du composant après lequel insérer ('0' = première position)"),
      area:     z.string().optional().describe("Clé de la zone cible (défaut: zone actuelle du composant)"),
      page:     z.string().optional().describe("ID, nom ou chemin de la page cible (défaut: page actuelle)"),
    },
  },
  async ({ id, previous, area, page }) => {
    const params: Record<string, string> = { id, previous };
    if (area) params.area = area;
    if (page) params.page = page;
    const data = await callAction("content.move", params);
    return ok(data);
  }
);

server.registerTool(
  "content_publish",
  {
    description: "Publie le site : synchronise l'arbre de navigation preview → view et met à jour les fichiers de contenu. À appeler après toute modification de contenu ou de navigation pour rendre les changements visibles aux visiteurs.",
    inputSchema: {},
  },
  async () => {
    const data = await callAction("content.publish", {});
    return ok(data);
  }
);

server.registerTool(
  "content_uploadFile",
  {
    description: "Envoie un fichier local (image, PDF…) dans un champ image/fichier d'un composant dynamique existant. Le fichier est stocké dans le dossier d'import de la page du composant et sélectionné dans le champ. Créer d'abord le composant avec content_add, puis appeler cet outil avec son ID.",
    inputSchema: {
      id:    z.string().describe("ID du composant dynamique (retourné par content_add)"),
      field: z.string().describe("Nom du champ image/fichier : le <nom> de field.image.<nom> dans le HTML du composant, ex: 'image'"),
      file:  z.string().describe("Chemin local du fichier à envoyer"),
      label: z.string().optional().describe("Libellé du fichier (texte alternatif pour une image)"),
    },
  },
  async ({ id, field, file, label }) => {
    const params: Record<string, string> = { id, field };
    if (label !== undefined) params.label = label;
    const data = await callAction("content.uploadFile", params, { field: "file", path: file });
    return ok(data);
  }
);

server.registerTool(
  "content_clearPage",
  {
    description: "Supprime tous les composants d'une page. Utile avant de reconstruire entièrement le contenu d'une page.",
    inputSchema: {
      page: z.string().describe("ID, nom ou chemin de la page à vider"),
    },
  },
  async ({ page }) => {
    const data = await callAction("content.clearPage", { page });
    return ok(data);
  }
);

// ── Taxonomy tools ────────────────────────────────────────────────────────────
// Une référence de nœud peut être son ID, son chemin ('geo > be' ou 'geo/be') ou son nom s'il est unique.
// L'ID du nœud racine est '0'.

const labelsSchema = z.record(z.string(), z.string()).optional()
  .describe("Libellés par langue, ex: {\"fr\":\"Belgique\",\"en\":\"Belgium\"}");

server.registerTool(
  "taxonomy_get",
  {
    description: "Retourne l'arbre de taxonomie (id, name, path, labels, decoration, children). Utiliser avant toute modification pour connaître les IDs.",
    inputSchema: {
      id: z.string().optional().describe("Référence du nœud racine du sous-arbre (défaut: arbre complet)"),
    },
  },
  async ({ id }) => {
    const params: Record<string, string> = {};
    if (id) params.id = id;
    const data = await callAction("taxo.get", params);
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_add",
  {
    description: "Ajoute un nœud dans la taxonomie. Le nom est normalisé (minuscules, '_' au lieu de '-'). Un nom préfixé '#' définit une source réutilisable, '>' un lien vers une source.",
    inputSchema: {
      name:       z.string().describe("Nom technique du nœud, ex: 'belgique'"),
      parent:     z.string().optional().describe("Référence du nœud parent (défaut: racine)"),
      id:         z.string().optional().describe("ID imposé (défaut: aléatoire)"),
      labels:     labelsSchema,
      decoration: z.string().optional().describe("Décoration (classe CSS / couleur) héritée par les enfants"),
      previous:   z.string().optional().describe("Insérer après ce frère ('0' = en premier, défaut: en dernier)"),
    },
  },
  async ({ name, parent, id, labels, decoration, previous }) => {
    const params: Record<string, string> = { name };
    if (parent)                   params.parent     = parent;
    if (id)                       params.id         = id;
    if (labels)                   params.labels     = JSON.stringify(labels);
    if (decoration !== undefined) params.decoration = decoration;
    if (previous)                 params.previous   = previous;
    const data = await callAction("taxo.add", params);
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_edit",
  {
    description: "Modifie un nœud de taxonomie : nom, ID, libellés (fusionnés, valeur vide = suppression) ou décoration.",
    inputSchema: {
      id:         z.string().describe("Référence du nœud à modifier"),
      name:       z.string().optional().describe("Nouveau nom technique"),
      newId:      z.string().optional().describe("Nouvel ID (attention : les pages référencent les nœuds par ID)"),
      labels:     labelsSchema,
      decoration: z.string().optional().describe("Nouvelle décoration. Chaîne vide pour effacer."),
    },
  },
  async ({ id, name, newId, labels, decoration }) => {
    const params: Record<string, string> = { id };
    if (name)                     params.name       = name;
    if (newId)                    params.newId      = newId;
    if (labels)                   params.labels     = JSON.stringify(labels);
    if (decoration !== undefined) params.decoration = decoration;
    const data = await callAction("taxo.edit", params);
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_remove",
  {
    description: "Supprime un nœud de taxonomie et tous ses enfants (opération irréversible).",
    inputSchema: {
      id: z.string().describe("Référence du nœud à supprimer"),
    },
  },
  async ({ id }) => {
    const data = await callAction("taxo.remove", { id });
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_move",
  {
    description: "Déplace un nœud de taxonomie. Avec 'parent' seul : devient premier enfant. Avec 'previous' : inséré après ce frère.",
    inputSchema: {
      id:       z.string().describe("Référence du nœud à déplacer"),
      parent:   z.string().optional().describe("Référence du nouveau parent"),
      previous: z.string().optional().describe("Insérer après ce nœud ('0' = en premier sous 'parent')"),
    },
  },
  async ({ id, parent, previous }) => {
    const params: Record<string, string> = { id };
    if (parent)   params.parent   = parent;
    if (previous) params.previous = previous;
    const data = await callAction("taxo.move", params);
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_export",
  {
    description: "Exporte toute la taxonomie au format texte (une ligne par nœud : '>' répété selon la profondeur, puis 'id|name[lang=libellé,...]'). La première ligne est la racine '0|root'.",
    inputSchema: {},
  },
  async () => {
    const data = await callAction("taxo.export", {});
    return ok(data);
  }
);

server.registerTool(
  "taxonomy_import",
  {
    description: "Remplace TOUTE la taxonomie par un texte au format de taxonomy_export. Utiliser '?' comme ID pour en générer un aléatoire. Exemple:\n0|root\n>geo|geo[fr=Géographie,en=Geography]\n>>?|be[fr=Belgique]\n>>?|fr[fr=France]",
    inputSchema: {
      text: z.string().describe("Arbre complet au format texte"),
    },
  },
  async ({ text }) => {
    const data = await callAction("taxo.import", { text });
    return ok(data);
  }
);

// ── Template tools ────────────────────────────────────────────────────────────

server.registerTool(
  "template_upload",
  {
    description: "Installe un template Javlo2 depuis un zip (fichier local envoyé en multipart, ou URL publique). Crée le dossier template si absent, écrase les fichiers existants. Appeler template_commit ensuite pour déployer.",
    inputSchema: {
      name: z.string().describe("Nom / ID cible du template (= nom du dossier)"),
      file: z.string().optional().describe("Chemin local d'un fichier .zip contenant le template (prioritaire sur url)"),
      url:  z.string().optional().describe("URL publique d'un fichier .zip contenant le template"),
    },
  },
  async ({ name, file, url }) => {
    if (file) {
      const data = await callAction("template.upload", { name }, { field: "file", path: file });
      return ok(data);
    }
    if (!url) {
      throw new Error("template_upload: fournir 'file' ou 'url'");
    }
    const data = await callAction("template.upload", { name, url });
    return ok(data);
  }
);

server.registerTool(
  "template_commit",
  {
    description: "Redéploie un template depuis son dossier source vers le webapp (vide le cache renderer). Équivalent au macro commit-template.",
    inputSchema: {
      name: z.string().describe("Nom ou ID du template à commiter"),
    },
  },
  async ({ name }) => {
    const data = await callAction("template.commit", { name });
    return ok(data);
  }
);

server.registerTool(
  "template_commitAll",
  {
    description: "Commite un template ET tous ses templates enfants (descendants). Utile quand un template parent change et doit propager aux thèmes dérivés.",
    inputSchema: {
      name: z.string().describe("Nom ou ID du template parent"),
    },
  },
  async ({ name }) => {
    const data = await callAction("template.commitAll", { name });
    return ok(data);
  }
);

// ─── Start ────────────────────────────────────────────────────────────────────

async function main() {
  const { baseUrl, token, lang } = getConfig();
  if (!token) {
    console.error("[javlo2-mcp] WARNING: JAVLO_TOKEN is not set — requests will likely fail.");
  } else {
    console.error("[javlo2-mcp] Auth: Authorization: Bearer header");
  }
  console.error(`[javlo2-mcp] Connecting to ${baseUrl} (lang: ${lang})`);

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("[javlo2-mcp] Server ready.");
}

main().catch((err) => {
  console.error("[javlo2-mcp] Fatal:", err);
  process.exit(1);
});
