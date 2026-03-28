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
import { dirname, join } from "path";

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
  params: Record<string, string>
): Promise<Record<string, unknown>> {
  const { baseUrl, token, lang } = getConfig();
  const ajaxUrl = `${baseUrl}/ajax/${lang}/`;
  const body = new URLSearchParams({ webaction, ...params });

  const headers: Record<string, string> = {
    "Content-Type": "application/x-www-form-urlencoded",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(ajaxUrl, {
    method:  "POST",
    headers,
    body:    body.toString(),
  });

  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }

  const json = (await res.json()) as JavloResponse;

  if (json.messageType === "error") {
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

// ── Content (component) tools ─────────────────────────────────────────────────

server.registerTool(
  "content_add",
  {
    description: "Ajoute un composant sur une page Javlo2.",
    inputSchema: {
      page:        z.string().describe("ID, nom ou chemin de la page cible"),
      type:        z.string().describe("Type du composant, ex: 'text', 'title', 'image'"),
      area:        z.string().describe("Clé de la zone du template, ex: 'main', 'header'"),
      previous:    z.string().optional().describe("ID du composant après lequel insérer ('0' = début, défaut: '0')"),
      value:       z.string().optional().describe("Valeur initiale du composant (texte brut)"),
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
      value:       z.string().optional().describe("Nouvelle valeur brute du composant"),
      style:       z.string().optional().describe("Nouvelle classe CSS de style"),
      layout:      z.string().optional().describe("Flags de mise en page (voir content_add). Chaîne vide pour effacer."),
      renderer:    z.string().optional().describe("Clé du renderer. Chaîne vide pour réinitialiser."),
      columnSize:  z.number().int().optional().describe("Largeur en colonnes de grille (ex: 6 pour demi-largeur sur 12 colonnes)"),
      columnStyle: z.string().optional().describe("Classe CSS du wrapper de colonne. Chaîne vide pour effacer."),
    },
  },
  async ({ id, value, style, layout, renderer, columnSize, columnStyle }) => {
    const params: Record<string, string> = { id };
    if (value       !== undefined) params.value       = value;
    if (style       !== undefined) params.style       = style;
    if (layout      !== undefined) params.layout      = layout;
    if (renderer    !== undefined) params.renderer    = renderer;
    if (columnSize  !== undefined) params.columnSize  = String(columnSize);
    if (columnStyle !== undefined) params.columnStyle = columnStyle;
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

// ── Template tools ────────────────────────────────────────────────────────────

server.registerTool(
  "template_upload",
  {
    description: "Installe un template Javlo2 depuis une URL zip. Crée le dossier template si absent, écrase les fichiers existants. Appeler template_commit ensuite pour déployer.",
    inputSchema: {
      name: z.string().describe("Nom / ID cible du template (= nom du dossier)"),
      url:  z.string().describe("URL publique d'un fichier .zip contenant le template"),
    },
  },
  async ({ name, url }) => {
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
  if (!TOKEN) {
    console.error("[javlo2-mcp] WARNING: JAVLO_TOKEN is not set — requests will likely fail.");
  } else {
    console.error("[javlo2-mcp] Auth: Authorization: Bearer header");
  }
  console.error(`[javlo2-mcp] Connecting to ${BASE_URL} (lang: ${LANG})`);

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("[javlo2-mcp] Server ready.");
}

main().catch((err) => {
  console.error("[javlo2-mcp] Fatal:", err);
  process.exit(1);
});
