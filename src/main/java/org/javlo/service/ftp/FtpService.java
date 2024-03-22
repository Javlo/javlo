package org.javlo.service.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.config.spring.factorybeans.ListenerFactoryBean;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission("/"));
        BaseUser user = new BaseUser();
        user.setName("user");
        user.setPassword("password");
        user.setHomeDirectory("c:/trans");
        user.setAuthorities(authorities); // Appliquer les autorités à l'utilisateur

        try {
            um.save(user); // Enregistrez l'utilisateur
            serverFactory.setUserManager(um);

            // Add the custom Ftplet to the server
            Map<String, Ftplet> ftplets = new HashMap<>();
            ftplets.put("uploadNotifier", new UploadNotificationFtplet());
            serverFactory.setFtplets(ftplets);

            // Start the server
            FtpServer server = serverFactory.createServer();

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
