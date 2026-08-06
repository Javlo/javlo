# List-Unsubscribe one-click dans Javlo — design

Date : 2026-08-06
Statut : validé, prêt pour le plan d'implémentation

## Objectif

Générer automatiquement les en-têtes `List-Unsubscribe` (RFC 2369) et
`List-Unsubscribe-Post` (RFC 8058, one-click) sur les mailings Javlo, et rendre le
désabonnement réellement effectif quelle que soit l'origine du destinataire.

Gmail et Yahoo exigent ces en-têtes des expéditeurs de masse (plus de 5000 messages
par jour vers leurs domaines) depuis février 2024, et imposent que le désabonnement
soit honoré sous deux jours sans intervention humaine.

## État des lieux

L'infrastructure existe déjà en grande partie.

- Le composant `unsubscribe-link` pose un lien de désabonnement dans le corps du
  mail : `?webaction=mailing.unsubscribe&roles=##roles##`
  (`org.javlo.component.links.UnsubsribeLink:34-36`).
- `MailingAction.performUnsubscribe:318-356` décode le token `_mfb`, retire les rôles
  du mailing à l'utilisateur, ou prévient l'administrateur si l'adresse n'a pas de compte.
- Un token par destinataire est généré à chaque envoi via `DataToIDService`
  (`MailingThread:150-152`).
- L'en-tête `List-Unsubscribe` n'est posé aujourd'hui que si un champ texte du site est
  rempli à la main (`MailService:355-357`, alimenté par `GlobalContext.getUnsubscribeLink()`
  puis `Mailing.manualUnsubscribeLink`).
- `Mailing.getUnsubscribeURL(String mail):137-140` construit une URL de désabonnement
  automatique mais n'est appelé nulle part : vestige d'une implémentation abandonnée.

Trois manques bloquent la conformité.

1. Aucun en-tête n'est généré automatiquement, et aucun `List-Unsubscribe-Post`.
2. Le désabonnement ne fonctionne que pour les destinataires ayant un compte. Les
   adresses collées librement dans le champ texte du module mailing
   (`MailingModuleContext:232-246`) ne sont jamais réellement désabonnées.
3. Le token repose sur `StringHelper.getRandomId():1310-1318`, soit
   `System.currentTimeMillis()` concaténé à huit chiffres tirés de `Math.random()`.
   Prédictible, donc inadapté à un endpoint qui désabonne sans confirmation.

## Décisions

| Sujet | Décision |
| --- | --- |
| Niveau de conformité | One-click complet, RFC 8058 |
| Endpoint | Webaction existant, pas de nouveau servlet |
| Sémantique | Liste de suppression persistante **et** retrait des rôles |
| Granularité | Par rôle, avec un pseudo-rôle `*` valant « tous les mailings du site » |
| Champ manuel du site | Conservé comme override ; génération automatique si vide |
| Canal `mailto:` | Hors périmètre |

## Architecture

### Endpoint

Aucun nouveau servlet. L'en-tête pointe vers l'URL absolue de la page du mailing :

```
List-Unsubscribe: <https://site/fr/page?webaction=unsecure.unsubscribe&lut=TOKEN>
List-Unsubscribe-Post: List-Unsubscribe=One-Click
```

L'action est déclarée dans le groupe **unsecure** : `UnsecureAction.performUnsubscribe`
délègue à `MailingAction.performUnsubscribe`, exactement comme
`UnsecureAction.performAskChangePassword:28-30` délègue à `UserAction`.

Ce choix est structurant. `ActionManager:220-232` n'applique le contrôle de droits sur le
module courant qu'aux actions implémentant `IModuleAction`. `MailingAction` en est une, donc
un POST sans session déclencherait la création d'une session vierge et la résolution d'un
module par défaut, avec un résultat imprévisible. `UnsecureAction` n'implémente que `IAction`
et son `haveRight` renvoie `true` : le contrôle est court-circuité par construction, sans
affaiblir quoi que ce soit d'autre. C'est le groupe prévu pour les actions publiques à token,
et il héberge déjà la réinitialisation de mot de passe, qui a le même profil de sécurité.

Vérifié : aucun filtre CSRF global dans `web.xml`, donc un POST cross-origin venant des
serveurs Gmail passe. Le drapeau `unsecure` de `ServletHelper.execAction` vaut `false` sur le
chemin `/view` (`AccessServlet:724`), ce qui autorise tous les groupes d'actions — la garde
`unsecure && !UnsecureAction.TYPE.equals(group)` de `ActionManager:201` ne s'applique qu'au
chemin `/edit` d'un utilisateur non connecté.

Le POST one-click arrive sur `AccessServlet`, qui exécute l'action puis rend la page. Le corps
de la réponse est ignoré par le client ; seul le `200` compte. Le GET affiche une vraie page
Javlo thémable, avec message de confirmation via `MessageRepository`.

### Portée des en-têtes

- Émis pour les **mailings uniquement**, jamais pour les mails transactionnels
  (formulaires, notifications) qui passent aussi par `MailService`.
- Si le champ manuel du site est rempli, sa valeur est utilisée telle quelle et
  `List-Unsubscribe-Post` n'est pas émis : on ne peut pas garantir qu'une URL externe
  respecte le one-click.
- `List-Unsubscribe-Post` n'est émis que si l'URL du site est en HTTPS, le RFC 8058
  l'exigeant. Sinon, en-tête simple et avertissement journalisé.

### Générateur de token

Nouvelle classe `org.javlo.service.UnsubscribeTokenService`, deux méthodes publiques :
`create(...)` et `read(token)` renvoyant `null` si le token est invalide.

**Primitive : AES-256-GCM** (`AES/GCM/NoPadding`, natif JDK, aucune dépendance nouvelle).
Chiffrement authentifié, donc deux propriétés d'un coup :

- *Intégrité* — un token modifié d'un bit échoue au déchiffrement. Ni forge, ni mutation du
  token d'autrui. C'est ce qui manque à `getRandomId()` et à `StringSecurityUtil.encode:62-73`,
  qui utilise AES en mode ECB sans authentification.
- *Confidentialité* — l'adresse email n'apparaît pas en clair dans l'URL, donc ni dans les
  logs d'accès, ni dans l'historique du navigateur, ni dans un `Referer`.

**Charge utile** : `v1|contextKey|mailingId|email|roles|timestamp`. Le `contextKey` est vérifié
à la lecture, ce qui empêche de rejouer sur un site un token émis pour un autre site de la même
instance. Le préfixe de version permet de faire évoluer le format sans invalider les tokens en
circulation.

**Clé par site**, dérivée en SHA-256 d'un secret propre au site stocké dans ses propriétés
(`mailing.unsubscribe-secret`), généré à la première utilisation avec `SecureRandom`.

**Encodage** : `base64url(IV 12 octets ‖ ciphertext ‖ tag)`, sans padding — aucun caractère à
échapper dans une URL.

**Pas d'expiration par défaut.** Gmail peut afficher le bouton des mois après réception ; un
token périmé produirait un désabonnement silencieusement cassé, précisément ce que les
exigences de délivrabilité sanctionnent. Le timestamp reste dans la charge utile pour permettre
une politique d'expiration ultérieure sans changer le format.

Le mécanisme `_mfb` existant n'est pas modifié : tracking et liens dans le corps continuent de
fonctionner à l'identique. Le nouveau token ne sert qu'à l'en-tête.

### Lien de désabonnement dans le corps du mail

Le composant `unsubscribe-link` pointe aujourd'hui vers `mailing.unsubscribe`, donc vers une
`IModuleAction` soumise au contrôle de droits sur le module courant décrit plus haut. Un
visiteur anonyme arrivant sans session est à la merci du module par défaut de sa session : le
désabonnement peut échouer silencieusement.

`UnsubsribeLink.getParam():34-36` est donc repointé vers `unsecure.unsubscribe`, comme
l'en-tête. Les deux chemins d'entrée partagent la même implémentation dans
`MailingAction.performUnsubscribe`, qui reçoit le nouveau comportement — liste de suppression
comprise. Le composant continue d'utiliser le token `_mfb` : il vit dans le corps d'un mail
déjà destiné à cette personne, et le changer casserait les mailings déjà envoyés.

### Liste de suppression

Service par site, `UnsubscribeService`, adossé à `<data>/private/unsubscribe.csv` — même
emplacement que `token_page.properties` (`GlobalContext:3834-3836`).

Format une ligne par entrée, `email⇥rôles⇥date-iso`, et non un fichier de propriétés. Deux
raisons : les adresses email contiennent des points, que `ConfigurationProperties` (Apache
Commons) interprète comme une hiérarchie de clés ; et une ligne s'ajoute en `O(1)` par append,
là où `DataToIDService.setData()` réécrit tout le fichier à chaque appel.

En mémoire, un `Map<String, Set<String>>` chargé à la première lecture. Adresses normalisées
en minuscules et sans espaces de bordure, à l'écriture comme à la comparaison.

API :

- `boolean isUnsubscribed(String email, Collection<String> roles)` — vrai si l'adresse porte
  `*` ou l'un des rôles passés
- `void unsubscribe(String email, Collection<String> roles)` — idempotent
- `Collection<UnsubscribeEntry> getAll()`
- `void resubscribe(String email)`

### Comportement du désabonnement

À la réception d'un token valide :

1. Ajouter `(email, rôles du mailing)` à la liste de suppression, les rôles étant ceux de
   `Mailing.getRoles()`. Si cette collection est vide, enregistrer sous `*`. Elle l'est dans
   deux cas, tous deux couverts par le pseudo-rôle global : un mailing envoyé uniquement à des
   adresses collées librement, et un mailing ne ciblant que des groupes d'administrateurs —
   `MailingModuleContext:320` ne transmet à `setRoles` que les groupes utilisateurs, jamais
   `adminGroups`.
2. Retirer les rôles du compte utilisateur s'il existe : comportement actuel conservé.
3. Enregistrer un `FeedBackMailingBean` comme le fait `DefaultMailingFeedback:40-53`.
   `Mailing.getCountUnsubscribe():769-780` compte les retours dont le webaction se termine par
   `unsubscribe` ; le nom `unsecure.unsubscribe` satisfait ce test, donc la statistique
   existante du module mailing continue de fonctionner sans modification.
4. Opération idempotente : un second POST ne produit ni erreur ni doublon.

Un token invalide, expiré ou destiné à un autre site produit un `200` sans effet. On ne renvoie
pas d'erreur : un client mail n'en ferait rien, et distinguer les cas d'échec renseignerait un
attaquant sur la validité des tokens.

### Filtrage à l'envoi

Deux points de contrôle, délibérément.

- À la constitution des destinataires (`MailingModuleContext:205-246`), pour que le nombre
  affiché à l'administrateur avant envoi soit juste.
- En défense dans `MailingThread`, juste avant chaque `sendMail`. Un mailing s'étale dans le
  temps ; quelqu'un qui se désabonne pendant l'envoi ne doit pas recevoir la suite. Le
  destinataire ignoré est journalisé comme les envois échoués, donc visible dans le rapport de
  fin de mailing.

Le filtrage s'applique aussi aux mailings de test, sans exception : une exception créerait un
chemin de code non testé pour un bénéfice nul.

### Propagation dans MailService

`unsubscribeLink` circule aujourd'hui comme `String` à travers toutes les surcharges de
`sendMail` (`MailService:237,286,315,618`). Pour porter le drapeau one-click sans multiplier
les paramètres, on introduit un petit objet de valeur immuable `UnsubscribeInfo` (URL +
booléen `oneClick`), porté par `EMail` et par la méthode privée centrale. Les surcharges
publiques existantes conservent une variante acceptant une `String`, qui construit un
`UnsubscribeInfo` sans one-click : aucun appelant actuel n'est cassé.

## Correctifs inclus

Deux défauts rencontrés sur le chemin de cette fonctionnalité, corrigés au passage.

- `GlobalContext.setUnsubscribeLink:4342-4344` n'appelle pas `save()`, contrairement à tous ses
  voisins (`setMailingSenders`, `setMailingSubject`, `setHelpURL`). La valeur n'est persistée
  aujourd'hui que par effet de bord du `setDKIMDomain()` appelé juste après dans
  `AdminAction:535`. Toute modification de l'ordre du formulaire ferait perdre silencieusement
  la valeur.
- `Mailing.getUnsubscribeURL(String mail):137-140` est du code mort. Supprimé.

## Périmètre exclu

- Le canal `mailto:` du `List-Unsubscribe`. Il exigerait une boîte dédiée et du parsing de mail
  entrant dans `POPThread`, pour un canal très peu utilisé en pratique.
- Une page de confirmation sur le GET. Le GET désabonne immédiatement, comme le fait déjà le
  composant `unsubscribe-link`. Diverger créerait deux sémantiques pour un même geste.
  *Risque assumé et documenté* : un antivirus ou un préchargeur de liens peut désabonner
  quelqu'un à son insu. Le POST de Gmail n'est pas concerné.
- Le remplacement de `DataToIDService` pour les liens du corps de mail, et la réécriture
  complète du fichier à chaque destinataire qu'il implique. Défaut réel mais préexistant et
  hors sujet ici.
- Toute correction de `StringSecurityUtil` (AES-ECB non authentifié), utilisé ailleurs dans le
  code. Le nouveau service ne s'en sert pas.

## Interface d'administration

Une section dans le module mailing listant les adresses désabonnées, avec leur date et leurs
rôles, et une action de réinscription. Sans elle, toute demande de réinscription impose une
édition manuelle de fichier sur le serveur.

## Tests

**`UnsubscribeTokenService`**

- Aller-retour : `read(create(x))` restitue la charge utile
- Token modifié d'un caractère : rejeté
- Token émis pour un autre `contextKey` : rejeté
- Chaîne tronquée, vide, `null`, base64 invalide : rejeté sans exception
- Deux appels à `create` avec les mêmes entrées produisent des tokens différents (IV aléatoire)

**`UnsubscribeService`**

- Désabonnement avec rôles, puis `isUnsubscribed` vrai pour ces rôles et faux pour les autres
- Désabonnement sans rôle : `*` enregistré, `isUnsubscribed` vrai pour n'importe quel rôle
- Persistance : après rechargement depuis le fichier, l'état est identique
- Normalisation : `Foo@Bar.COM` et ` foo@bar.com ` sont la même adresse
- Idempotence : deux désabonnements identiques ne créent pas de doublon

**En-têtes**

- Mailing sur site HTTPS : `List-Unsubscribe` et `List-Unsubscribe-Post` présents
- Site HTTP : `List-Unsubscribe` présent, `List-Unsubscribe-Post` absent
- Champ manuel rempli : sa valeur exacte, sans `List-Unsubscribe-Post`
- Mail transactionnel : aucun des deux en-têtes

**Intégration**

- POST sans session ni cookie sur l'URL du token : l'adresse est désabonnée
- Second POST identique : `200`, sans doublon
- GET sur la même URL : désabonne et rend la page
- Token invalide : `200` sans effet
- Le lien du composant `unsubscribe-link` dans le corps du mail alimente la même liste de
  suppression que l'en-tête
- Un destinataire désabonné est absent du mailing suivant, pour les trois origines de
  destinataires (rôles utilisateurs, rôles admin, adresses collées)
