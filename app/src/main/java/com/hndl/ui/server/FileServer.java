package com.hndl.ui.server;

import android.os.Environment;

import com.hndl.ui.utils.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class FileServer extends NanoHTTPD {
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/hndl/"; // 要共享的文件路径

    public FileServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        LogUtil.e("XJW","uri:"+session.getUri());
        if (method == Method.GET && session.getUri().equals("/")) {
            return newFixedLengthResponse("Hello from the server!");
        } else if (method == Method.GET && session.getUri().startsWith("/files/")) {
            String fileName = session.getUri().substring("/files/".length());
            LogUtil.e("XJW","fileName:"+fileName);
            try {
                FileInputStream fis = new FileInputStream(filePath + fileName);

                if (fis != null) {
                    return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fis, fis.available());
                } else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND,"<h1>404 Not Found</h1>", MIME_HTML);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse( Response.Status.INTERNAL_ERROR,"<h1>500 Internal Server Error</h1>", MIME_HTML);
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND,"<h1>404 Not Found</h1>",  MIME_HTML);
    }

    private byte[] readFileData(String fileName) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(filePath + fileName);

        if (inputStream != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];

            while ((bytesRead = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }

        return null;
    }
}
