# Javlo2 - CLAUDE.md

## Deployment

- Le projet Javlo est déployé dans : `C:\opt\tomcat10\webapps\javlo2`
- Les modifications peuvent y être copiées directement pour les tester.
- Le site est accessible via Chrome (et le plugin Claude in Chrome) à : http://localhost/javlo2/sexy/preview/fr/
- Identifiants : login `admin` / password `admin`

## SCSS / CSS

- Javlo compile automatiquement les fichiers SCSS. Si on charge un fichier `.css` (ex: `javlo.css`) et qu'il n'existe pas mais que le fichier `.scss` correspondant existe (ex: `javlo.scss`), le fichier `.css` sera généré automatiquement.
- **Lors du déploiement** : après avoir copié un fichier `.scss` dans le dossier de déploiement, il faut **supprimer le fichier `.css` correspondant** pour forcer Javlo à le recompiler. Exemple : si on déploie `andro_grey.scss`, supprimer `andro_grey.css`, `global.css`, etc. selon les fichiers qui importent le SCSS modifié.

## Composants — bonnes pratiques

- Pour les pages d'**actualités/news** : utiliser le composant `page-reference` (et non `internal-link`) pour lister les articles.
