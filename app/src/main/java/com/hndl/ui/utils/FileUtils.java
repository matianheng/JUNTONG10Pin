package com.hndl.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    // 将List<byte[]>写入文件
    public static void writeListToFile(List<byte[]> byteArrayList, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (byte[] bytes : byteArrayList) {
                fos.write(bytes);
            }
        }
    }

    // 从文件读取并转换为List<byte[]>
    public static List<byte[]> readFileToList(File file) throws IOException {
        List<byte[]> byteArrayList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                byteArrayList.add(chunk);
            }
        }
        return byteArrayList;
    }
}
