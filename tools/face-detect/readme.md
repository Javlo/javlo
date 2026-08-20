# face-detect

Détection de visages en Java, **sur CPU uniquement** — pas de GPU, pas d'appel réseau, pas de service
externe. Le modèle [YuNet](https://github.com/opencv/opencv_zoo/tree/main/models/face_detection_yunet)
(227 ko, ONNX) est embarqué dans le jar et tourne via ONNX Runtime.

Module Maven autonome : il ne fait pas partie du war de Javlo. Il se construit et s'utilise séparément.

## Utilisation depuis Java

```java
try (FaceDetector detector = new FaceDetector()) {
    BufferedImage photo = ImageIO.read(new File("photo.jpg"));

    Rectangle cadre = detector.detectMainFace(photo);   // le plus grand visage, null si aucun
    Rectangle tous   = detector.detectFacesBounds(photo); // cadre englobant tous les visages

    for (Face face : detector.detect(photo)) {
        System.out.println(face.getBounds() + " " + face.getScore());
        System.out.println("oeil droit : " + face.getRightEye());
    }
}
```

Construire un `FaceDetector` charge le modèle : **garder une seule instance et la partager**. La
détection est thread-safe.

### API

| Méthode | Retour |
|---|---|
| `detect(BufferedImage)` | `List<Face>`, triée par confiance décroissante, vide si aucun visage |
| `detectMainFace(BufferedImage)` | `Rectangle` du plus grand visage, `null` si aucun |
| `detectFacesBounds(BufferedImage)` | `Rectangle` englobant tous les visages, `null` si aucun |

`Face` donne `getBounds()`, `getScore()` (0 à 1), `getCenter()` et les cinq repères :
`getRightEye()`, `getLeftEye()`, `getNose()`, `getRightMouthCorner()`, `getLeftMouthCorner()`.
Droite et gauche sont celles du sujet : l'œil droit apparaît à gauche sur l'image.

### Réglages

```java
detector.setScoreThreshold(0.5f); // défaut 0.6 — baisser pour attraper plus de visages
detector.setNmsThreshold(0.3f);   // recouvrement au-delà duquel deux boîtes sont le même visage
detector.setMaxFaces(500);
```

## Utilisation en ligne de commande

```bash
mvn package
java -jar target/face-detect.jar photo.jpg
java -jar target/face-detect.jar photo.jpg --annotate marque.jpg --threshold 0.5
```

Le JSON part sur la sortie standard, les messages sur la sortie d'erreur :

```json
{
  "image": "photo.jpg",
  "width": 512,
  "height": 512,
  "elapsedMs": 21,
  "faceCount": 1,
  "faces": [
    {"x": 179, "y": 63, "width": 91, "height": 111, "score": 0.9434,
     "landmarks": [[201, 105], [245, 105], [221, 127], [204, 145], [242, 145]]}
  ]
}
```

`--annotate` écrit une copie de l'image avec les cadres en vert et les repères en rouge.

## Performances

Mesuré sur cette machine, une image 512×512, monothread, sans GPU : **35 ms en moyenne, 17 ms au
mieux** sur 30 passes après chauffe. Le chargement du modèle prend environ une seconde, une seule fois.

## Limite à connaître

L'export ONNX du modèle a une entrée figée à 640×640. Toute image est donc réduite dans ce carré en
conservant ses proportions (letterbox, calage en haut à gauche). Conséquence : sur une photo de 4000 px
de large, un visage de moins de ~50 px devient trop petit après réduction et passe inaperçu. Pour de
petits visages dans une grande image, découper l'image en tuiles et détecter tuile par tuile.

## Intégration dans Javlo

Le module est délibérément hors du war : le jar ONNX Runtime pèse 93 Mo (il embarque les binaires
natifs Windows, Linux et macOS). Pour l'utiliser depuis Javlo — par exemple un recadrage centré sur le
visage dans `ImageEngine` — ajouter au `pom.xml` principal :

```xml
<dependency>
    <groupId>org.javlo</groupId>
    <artifactId>face-detect</artifactId>
    <version>1.0.0</version>
</dependency>
```

après un `mvn install` dans ce répertoire.

## Tests

```bash
mvn test
```

`YuNetDecoderTest` vérifie le décodage et le NMS sur des tenseurs construits à la main, sans charger le
modèle. `FaceDetectorTest` fait tourner le vrai modèle sur `astronaut.png` (portrait NASA du domaine
public d'Eileen Collins) : un visage attendu, repères aux bons endroits, plus un montage 2×2 qui doit
en donner quatre.

## Structure

```
src/main/java/org/javlo/face/
    FaceDetector.java           API publique, session ONNX, letterbox, remise à l'échelle
    Face.java                   un visage : cadre, score, cinq repères
    YuNetDecoder.java           décodage des tenseurs et NMS — logique pure, testable seule
    RawDetection.java           détection en pixels de l'entrée réseau, avant remise à l'échelle
    FaceDetectorCLI.java        ligne de commande, sortie JSON, annotation
    FaceDetectionException.java
src/main/resources/models/
    face_detection_yunet_2023mar.onnx
```
