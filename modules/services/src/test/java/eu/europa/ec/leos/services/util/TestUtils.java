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
package eu.europa.ec.leos.services.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeAllNameSpaces;

public class TestUtils {
    
    public static byte[] getBytesFromFile(String path, String fileName) {
        return getBytesFromFile(path + fileName);
    }
    
    public static byte[] getBytesFromFile(String path) {
        try {
            File file = new File(path);
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + path);
        }
    }
    
    public static byte[] getFileContent(String path, String fileName) {
        return getFileContent(path + fileName);
    }
    
    public static byte[] getFileContent(String fileName) {
        try {
            InputStream inputStream = TestUtils.class.getResource(fileName).openStream();
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + fileName);
        }
    }

    public static String squeezeXmlAndRemoveAllNS(String input) {
        input = removeAllNameSpaces(input);
        return squeezeXml(input);
    }

    public static String squeezeXml(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"")
                .replaceAll("id=\".+?\"", "id=\"dummyId\"")
                .replaceAll("xml:id=\".+?\"", "xml:id=\"dummyId\"");
    }
    
    public static String squeezeXmlRemoveNum(String input) {
        return squeezeXml(input)
                .replaceAll("<num(\\s)*?xml:id=\".+?\"(\\s)*?>", "<num>");
    }

    public static String squeezeXmlWithoutXmlIds(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"")
                .replaceAll("id=\".+?\"", "id=\"dummyId\"");
    }
}
