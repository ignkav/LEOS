/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.digit.leos.pilot.export.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ZipUtils {

    private static Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    public static File zipFiles(String zipFileName, Map<String, Object> contentToZip) throws IOException {
        String fileExtension = "." + FilenameUtils.getExtension(zipFileName);
        String fileName = FilenameUtils.getBaseName(zipFileName);
        File zipFile = null;
        ZipOutputStream zipOutputStream = null;
        try {
            zipFile = File.createTempFile(fileName + "_", fileExtension);
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            addContentToOutputStream(zipOutputStream, contentToZip);
            return zipFile;
        } catch (IOException e) {
            LOG.error("Error creating zip package: {}", e.getMessage());
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
            throw new IOException(e.getMessage());
        } finally {
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
        }
    }

    public static byte[] zipByteArray(Map<String, Object> contentToZip) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(bos)) {
            addContentToOutputStream(zos, contentToZip);
            zos.close();
            return bos.toByteArray();
        }
    }

    private static void addContentToOutputStream(ZipOutputStream zipOutputStream, Map<String, Object> contentToZip) throws IOException {
        for (Map.Entry<String, Object> entry : contentToZip.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof File) {
                File fileValue = (File) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                FileInputStream fileInputStream = new FileInputStream(fileValue);
                IOUtils.copy(fileInputStream, zipOutputStream);
                fileInputStream.close();
                zipOutputStream.closeEntry();
            } else if (value instanceof ByteArrayOutputStream) {
                ByteArrayOutputStream byteArrayOutputStreamValue = (ByteArrayOutputStream) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(byteArrayOutputStreamValue.toByteArray());
                zipOutputStream.closeEntry();
                byteArrayOutputStreamValue.close();
            } else if (value instanceof String) {
                String stringValue = (String) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(stringValue.getBytes(UTF_8));
                zipOutputStream.closeEntry();
            } else if (value instanceof byte[]) {
                byte[] byteArrayValue = (byte[]) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(byteArrayValue);
                zipOutputStream.closeEntry();
            }
        }
    }

    public static Map<String, Object> unzipByteArray(byte[] zipppedData) throws IOException {
        Map<String, Object> unzippedFiles = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipppedData))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                unzippedFiles.put(ze.getName(), IOUtils.toByteArray(zis));
            }
            zis.closeEntry();
        }
        return unzippedFiles;
    }

    public static Map<String, Object> unzipFiles(File file, String unzipPath) {
        Map<String, Object> unzippedFiles = new HashMap<>();
        final File destDir = new File(System.getProperty("java.io.tmpdir") + unzipPath +
                file.getName() + "_" + System.currentTimeMillis());
        // get the zip file content with try-with-resources
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            // get the zipped file list entry
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                final File newFile = newFile(destDir, ze);
                if (ze.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    final File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    final FileOutputStream fos = new FileOutputStream(newFile);
                    IOUtils.copy(zis, fos);
                    fos.close();
                    unzippedFiles.put(newFile.getName(), newFile);
                }
            }
            // closeEntry should not be required. In the next step the stream will be closed.
            // close will be done by the try-with-resources block
        } catch (IOException ex) {
            LOG.error("Error unzipping the file {} : {}", file.getName(), ex.getMessage());
        }
        return unzippedFiles;
    }

    /**
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
