# Configuration Java 21 pour IntelliJ IDEA

## ⚠️ Important
Votre système a Java 17 dans les variables d'environnement mais Java 21 est installé.
Pour compiler depuis IntelliJ IDEA, suivez ces étapes :

## ✅ Méthode recommandée : Configuration IntelliJ IDEA

### 1. Configurer le SDK du projet

1. **Ouvrir Project Structure :**
   - Menu : `File` → `Project Structure...` (ou `Ctrl+Alt+Shift+S`)

2. **Configurer Project SDK :**
   - Dans l'onglet `Project` :
     - `SDK` : Sélectionnez Java 21
     - Si Java 21 n'apparaît pas :
       - Cliquez sur `Edit`
       - Cliquez sur `+` puis `Add JDK...`
       - Naviguez vers votre installation Java 21 (ex: `C:\Program Files\Java\jdk-21`)
       - Ou cliquez sur `Download JDK...` et sélectionnez version 21
   
   - `Language level` : Sélectionnez `21 - Record patterns, pattern matching for switch`

### 2. Configurer le compilateur Maven

1. **Ouvrir les Settings :**
   - Menu : `File` → `Settings...` (ou `Ctrl+Alt+S`)

2. **Configurer Maven Runner :**
   - Allez dans : `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`
   - Dans `JRE` : Sélectionnez votre Java 21 (même SDK que le projet)

3. **Configurer le compilateur Java :**
   - Allez dans : `Build, Execution, Deployment` → `Compiler` → `Java Compiler`
   - `Project bytecode version` : Sélectionnez `21`

### 3. Recharger le projet Maven

1. **Clic droit sur `pom.xml`** → `Maven` → `Reload Project`
2. Ou utilisez le panneau Maven (à droite) et cliquez sur l'icône de rechargement 🔄

### 4. Compiler le projet

**Option A : Via le panneau Maven**
1. Ouvrez le panneau Maven (à droite)
2. Déroulez `todo-ddd-example` → `Lifecycle`
3. Double-cliquez sur `clean`
4. Puis double-cliquez sur `install`

**Option B : Via Run Configuration**
1. Créez une nouvelle configuration Maven :
   - Menu : `Run` → `Edit Configurations...`
   - Cliquez sur `+` → `Maven`
   - Name : `Maven Clean Install`
   - Working directory : `$ProjectFileDir$`
   - Command line : `clean install`
   - JRE : Sélectionnez Java 21
2. Exécutez cette configuration

**Option C : Via le terminal IntelliJ**
```cmd
mvn clean install
```
(Le terminal IntelliJ utilisera le JDK configuré dans les settings Maven)

## 🔍 Vérifier la configuration

### Dans IntelliJ :
1. Ouvrez le terminal IntelliJ (en bas)
2. Tapez : `java -version`
3. Vérifiez que c'est bien Java 21

### Vérifier le SDK du projet :
1. `File` → `Project Structure` → `Project`
2. Vérifiez que `SDK` est bien sur Java 21

## ❌ Si vous rencontrez toujours des erreurs

### Solution 1 : Invalider les caches
1. `File` → `Invalidate Caches...`
2. Cochez `Clear file system cache and Local History`
3. Cliquez sur `Invalidate and Restart`

### Solution 2 : Reimporter le projet Maven
1. Fermez le projet : `File` → `Close Project`
2. Dans l'écran d'accueil : `Open`
3. Sélectionnez le fichier `pom.xml`
4. Cochez `Trust Project`
5. Attendez que Maven télécharge toutes les dépendances

### Solution 3 : Utiliser le script batch (ligne de commande Windows)

Si vous voulez compiler en ligne de commande avec Java 21 :

```cmd
build-java21.bat
```

Ce script configure automatiquement `JAVA_HOME` pour Java 21 avant de lancer Maven.

## 📝 Notes

- **IntelliJ IDEA** peut utiliser un JDK différent de votre variable d'environnement système
- Le projet est configuré pour Java 21 dans le `pom.xml`
- Vous n'avez **pas besoin** de changer votre variable `JAVA_HOME` système
- IntelliJ gérera automatiquement le bon JDK une fois configuré

## 🎯 Résumé rapide

1. ✅ `File` → `Project Structure` → `Project` → SDK = Java 21
2. ✅ `Settings` → `Maven` → `Runner` → JRE = Java 21  
3. ✅ Recharger Maven (clic droit sur pom.xml)
4. ✅ Compiler : Panneau Maven → `clean` puis `install`

Après ces étapes, votre projet devrait compiler sans erreur ! 🚀

