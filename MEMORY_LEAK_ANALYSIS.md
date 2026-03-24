# Analyse des fuites mémoire potentielles dans MenuElement.java

## Collections, Maps et Caches stockés comme champs d'instance

### 1. Collections/Maps initialisées (risque élevé)

#### `Set<String> userRoles` (ligne 710)
```java
Set<String> userRoles = new HashSet<String>();
```
- **Problème**: HashSet initialisé, jamais nettoyé explicitement
- **Risque**: Accumulation de rôles si jamais vidé
- **Solution**: Ajouter un `clear()` dans une méthode de nettoyage

#### `List<MenuElement> virtualParent` (ligne 730)
```java
List<MenuElement> virtualParent = new LinkedList<MenuElement>();
```
- **Problème**: LinkedList initialisé, contient des références vers d'autres MenuElement
- **Risque**: Références circulaires possibles, jamais vidé
- **Solution**: Vérifier `clearVirtualParent()` (ligne 1181) - existe mais pas toujours appelé

#### `Collection<String> compToBeDeleted` (ligne 814)
```java
private final Collection<String> compToBeDeleted = new LinkedList<String>();
```
- **Problème**: LinkedList final, vidé dans `deleteCompList()` mais peut s'accumuler si méthode non appelée
- **Risque**: Moyen - vidé après traitement mais peut s'accumuler entre les appels
- **Solution**: OK si `deleteCompList()` est toujours appelé

#### `Set<String> editGroups` (ligne 818)
```java
private Set<String> editGroups = new HashSet<String>();
```
- **Problème**: HashSet initialisé, jamais nettoyé explicitement
- **Risque**: Accumulation de groupes d'édition
- **Solution**: Existe `clearEditorGroups()` (ligne 1151) mais pas toujours appelé

### 2. Maps créées dynamiquement (risque très élevé)

#### `Map<String, ContentElementList> contentElementListMap` (ligne 746)
```java
transient Map<String, ContentElementList> contentElementListMap = null;
```
- **Création**: Ligne 1161 - `Collections.synchronizedMap(new HashMap<String, ContentElementList>())`
- **Problème**: Map créée à la demande, stocke des ContentElementList par langue
- **Risque**: TRÈS ÉLEVÉ - peut contenir beaucoup de données, vidé dans `releaseCache()` mais pas toujours appelé
- **Solution**: Vérifier que `releaseCache()` est appelé régulièrement

#### `Map<String, ContentElementList> localContentElementListMap` (ligne 748)
```java
transient Map<String, ContentElementList> localContentElementListMap = null;
```
- **Création**: Ligne 1168 - `new HashMap<String, ContentElementList>()`
- **Problème**: Map créée à la demande, stocke des ContentElementList par langue
- **Risque**: TRÈS ÉLEVÉ - peut contenir beaucoup de données, vidé dans `releaseCache()` mais pas toujours appelé
- **Solution**: Vérifier que `releaseCache()` est appelé régulièrement

#### `Map<String, MenuElement> pageCache` (ligne 836)
```java
private Map<String, MenuElement> pageCache = null;
```
- **Création**: Ligne 856 - `new TimeMap<String, MenuElement>(5 * 60, 2048)`
- **Problème**: TimeMap avec expiration de 5 minutes, mais peut s'accumuler si beaucoup de pages
- **Risque**: MOYEN - TimeMap expire automatiquement mais peut garder 2048 entrées
- **Solution**: Vérifier la taille et ajouter un nettoyage périodique

#### `Map<String, ComponentBean> contentToBeAdded` (ligne 816)
```java
private Map<String, ComponentBean> contentToBeAdded = Collections.EMPTY_MAP;
```
- **Création**: Ligne 4708 - `new HashMap<String, ComponentBean>()`
- **Problème**: Map créée dynamiquement, vidée dans `addPreparedContent()` (ligne 1087)
- **Risque**: MOYEN - vidé après traitement mais peut s'accumuler si méthode non appelée
- **Solution**: Vérifier que `addPreparedContent()` est toujours appelé

### 3. Caches (risque très élevé)

#### `ICache localCache` (ligne 830)
```java
private transient ICache localCache = null;
```
- **Création**: Ligne 3336 - `new MapCache(new HashMap(), "navigation")`
- **Problème**: Cache créé à la demande, stocke des PageDescription
- **Risque**: TRÈS ÉLEVÉ - peut contenir beaucoup de données, vidé dans `getCache()` si `releaseCache` est true
- **Solution**: Le cache est vidé conditionnellement, vérifier que `releaseCache` est bien géré

### 4. Collections optionnelles (risque faible)

#### `Set<String> taxonomy` (ligne 794)
```java
private Set<String> taxonomy = null;
```
- **Problème**: Peut être null ou contenir des données
- **Risque**: FAIBLE - peut être null, pas de création automatique
- **Solution**: OK si bien géré

## Méthodes de nettoyage existantes

### `releaseCache()` (ligne 4715)
```java
public void releaseCache() {
    releaseCache = true;
    getContentElementListMap().clear();
    getLocalContentElementListMap().clear();
    for (MenuElement child : getChildMenuElements()) {
        child.releaseCache();
    }
}
```
- **Problème**: Ne nettoie PAS `localCache`, `pageCache`, `virtualParent`, `userRoles`, `editGroups`
- **Solution**: Étendre cette méthode pour nettoyer tous les caches

### `clearVirtualParent()` (ligne 1181)
```java
public void clearVirtualParent() {
    for (MenuElement parent : virtualParent) {
        parent.removeVirtualChild(this);
    }
    virtualParent.clear();
}
```
- **OK**: Nettoie bien `virtualParent`

### `clearEditorGroups()` (ligne 1151)
```java
public void clearEditorGroups() {
    if (isChildrenOfAssociation() && getRootOfChildrenAssociation() != null) {
        getRootOfChildrenAssociation().clearEditorGroups();
    } else {
        editGroups.clear();
    }
}
```
- **OK**: Nettoie bien `editGroups`

## Recommandations

### 1. Étendre `releaseCache()` pour nettoyer tous les caches
```java
public void releaseCache() {
    releaseCache = true;
    getContentElementListMap().clear();
    getLocalContentElementListMap().clear();
    
    // Ajouter ces nettoyages:
    if (localCache != null) {
        localCache.removeAll();
    }
    if (pageCache != null) {
        pageCache.clear();
    }
    
    for (MenuElement child : getChildMenuElements()) {
        child.releaseCache();
    }
}
```

### 2. Ajouter une méthode de nettoyage complète
```java
public void clearAllCaches() {
    releaseCache();
    virtualParent.clear();
    compToBeDeleted.clear();
    if (contentToBeAdded != Collections.EMPTY_MAP) {
        contentToBeAdded.clear();
    }
    if (taxonomy != null) {
        taxonomy.clear();
    }
    localCache = null;
    pageCache = null;
}
```

### 3. Vérifier les références circulaires
- `virtualParent` et `virtualChild` peuvent créer des références circulaires
- `parent` peut créer des références circulaires si mal géré

### 4. Points d'attention
- `pageCache` utilise TimeMap avec expiration, mais peut quand même s'accumuler
- `localCache` n'est jamais explicitement vidé sauf si `releaseCache` est true
- Les Maps `contentElementListMap` et `localContentElementListMap` sont vidées mais peuvent se recréer rapidement
