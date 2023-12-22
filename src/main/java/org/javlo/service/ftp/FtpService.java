package org.javlo.service.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.config.spring.factorybeans.ListenerFactoryBean;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

public class FtpService {

    public static void main(String[] args) throws FtpException {

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactoryBean factory = new ListenerFactoryBean();

        // Définissez le port d'écoute (par défaut: 21)
        factory.setPort(21);

        // Remplacez par votre configuration de sécurité si nécessaire

        // Ajoutez le listener
        serverFactory.addListener("default", factory.createListener());

        // Configuration des utilisateurs
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager um = userManagerFactory.createUserManager();

        // Créez un utilisateur avec des droits d'accès
        BaseUser user = new BaseUser();
        user.setName("user"); // Nom d'utilisateur
        user.setPassword("password"); // Mot de passe
        user.setHomeDirectory("c:/trans"); // Répertoire racine de l'utilisateur

        try {
            um.save(user); // Enregistrez l'utilisateur
            serverFactory.setUserManager(um);

            // Démarrer le serveur
            FtpServer server = serverFactory.createServer();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
