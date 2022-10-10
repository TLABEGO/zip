package com.mfs.client.equity.utils;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import lombok.extern.log4j.Log4j2;

/**
 * <p>This class handles all methods to be used for encrypt text</p>
 */
@Log4j2
public class MFSEquityEncryption {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String BC_PROVIDER = "BC";
    public static final int KEY_SIZE = 2048;

    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Signing of text input
     *
     * @param privateKeyPath path where the private key is situated
     * @param keyAlias  representation of the key being processed
     * @param pwd password provided for the data signing
     * @param text generated checksum
     * @return checksum string
     */
    public static String signData(String privateKeyPath,String keyAlias,String pwd,String text) {
        String signedText;
        try {
            log.debug("Private key path:"+ privateKeyPath);
            log.debug("Private key alias:"+ keyAlias);
            log.debug("Private key password:"+ pwd);
            log.debug("Generated Checksum:"+ text);
            
            Security.addProvider(new BouncyCastleProvider());
            FileInputStream is = new FileInputStream(privateKeyPath);

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, pwd.toCharArray());

            Key key = keystore.getKey(keyAlias,	pwd.toCharArray());
            PrivateKey privateKey = (PrivateKey) key;

            // Creating a Signature object
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM, BC_PROVIDER);

            // Initializing the signature
            sign.initSign(privateKey);
            byte[] bytes = text.getBytes();

            // Adding data to the signature
            sign.update(bytes);

            // Calculating the signature
            byte[] signature = sign.sign();

            signedText = Base64.getEncoder().encodeToString(signature);

            log.debug("SHA256withRSA signed text:"+signedText);

        } catch (Exception exception) {
        	log.error("Error MfsEquityEncryption {}", exception.getMessage());
            throw new RuntimeException(exception);
        }
        return signedText;
    }

}
