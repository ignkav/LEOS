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
package europa.edit.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/* 	Author: Satyabrata Das
 * 	Functionality: Password decryptor for security
 */
@Slf4j
public class Cryptor {

    private static final String UNICODE_FORMAT = "UTF8";
    private static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    private byte[] arrayBytes;
    private SecretKey key;

    public String decrypt(String encryptedString) {
        String decryptedText = null;
        Configuration config = new Configuration();

        String myEncryptionKey = config.getProperty("encKey");
        String myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;

        Boolean skipDecrypt = Boolean.valueOf(config.getProperty("skipDecrypt"));
        if (skipDecrypt) {
            return encryptedString;
        }

        try {
            arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        } catch (UnsupportedEncodingException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            ks = new DESedeKeySpec(arrayBytes);
        } catch (InvalidKeyException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        } catch (NoSuchAlgorithmException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            cipher = Cipher.getInstance(myEncryptionScheme);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            key = skf.generateSecret(ks);
        } catch (InvalidKeySpecException e1) {
            logger.error(e1.getMessage(), e1);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText = new String(plainText);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return decryptedText;
    }
}