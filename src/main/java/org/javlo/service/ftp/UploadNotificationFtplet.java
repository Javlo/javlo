package org.javlo.service.ftp;


import org.apache.ftpserver.ftplet.*;

import java.io.IOException;

public class UploadNotificationFtplet extends DefaultFtplet {

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        // Extract file name and user information
        String fileName = request.getArgument();
        String userName = session.getUser().getName();

        // Log or handle the upload notification
        System.out.println("File uploaded: " + fileName + " by user: " + userName);

        return super.onUploadEnd(session, request);
    }
}
