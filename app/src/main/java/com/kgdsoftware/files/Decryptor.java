package com.kgdsoftware.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hank on 6/23/17.
 */

public class Decryptor {
    public static final int AES_KEY_SIZE = 128;    // 256

    private Cipher pkCipher;
    private Cipher aesCipher;

    private byte[] aesKey;
    SecretKeySpec aesKeySpec;

    public Decryptor() throws GeneralSecurityException {
        Provider[] providers = Security.getProviders();
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        pkCipher = Cipher.getInstance("RSA");
        aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    private IvParameterSpec getIV() {
        byte[] ivByteArray = new byte[16];
        for (int i = 0; i < 16; i++) {
            ivByteArray[i] = (byte) i;
        }
        return new IvParameterSpec(ivByteArray);
    }

    public void loadKey(File in, File privateKeyFile)
            throws GeneralSecurityException, IOException {
        byte[] encodedKey = new byte[(int) privateKeyFile.length()];
        new FileInputStream(privateKeyFile).read(encodedKey);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(privateKeySpec);

        pkCipher.init(Cipher.DECRYPT_MODE, privateKey);
        aesKey = new byte[AES_KEY_SIZE / 8];
        CipherInputStream is = new CipherInputStream(new FileInputStream(in), pkCipher);
        is.read(aesKey);
        aesKeySpec = new SecretKeySpec(aesKey, "AES");
    }

    public void loadKey(ZipInputStream zip, File privateKeyFile)
            throws GeneralSecurityException, IOException {
        byte[] encodedKey = new byte[(int) privateKeyFile.length()];
        new FileInputStream(privateKeyFile).read(encodedKey);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(privateKeySpec);

        pkCipher.init(Cipher.DECRYPT_MODE, privateKey);
        ZipEntry zipEntry = zip.getNextEntry(); // key.dat
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] byteBuff = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = zip.read(byteBuff)) != -1) {
            baos.write(byteBuff, 0, bytesRead);
        }
        baos.close();
        byte[] byteArray = baos.toByteArray();
        CipherInputStream is = new CipherInputStream(new ByteArrayInputStream(byteArray), pkCipher);
        aesKey = new byte[AES_KEY_SIZE / 8];
        is.read(aesKey);
        aesKeySpec = new SecretKeySpec(aesKey, "AES");
    }

    public void decrypt(File in, String outputFileName) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {
        aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec, getIV());

        CipherInputStream is = new CipherInputStream(new FileInputStream(in), aesCipher);

        File unencryptedFile = new File(outputFileName);

        FileOutputStream os = new FileOutputStream(unencryptedFile);

        copy(is, os);

        is.close();
        os.close();
    }

    public String decrypt(ZipInputStream zip, File directory) throws IOException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec, getIV());

        ZipEntry zipEntry = zip.getNextEntry();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] byteBuff = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = zip.read(byteBuff)) != -1) {
            baos.write(byteBuff, 0, bytesRead);
        }
        baos.close();

        // Have the bytes from the zip file, now decrypt...

//        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//        CipherInputStream is = new CipherInputStream(bais, aesCipher);
//        FileOutputStream os = new FileOutputStream(unencryptedFile);
//        copy(is, os);
//        is.close();
//        os.close();

        byte[] decrypted = aesCipher.doFinal(baos.toByteArray());
        File unencryptedFile = new File(directory, zipEntry.getName());
        FileOutputStream fos = new FileOutputStream(unencryptedFile);
        fos.write(decrypted);
        fos.close();

        return zipEntry.getName();
    }

    private void copy(InputStream is, OutputStream os) throws IOException {
        int i;
        byte[] b = new byte[1024];
        while ((i = is.read(b)) != -1) {
            os.write(b, 0, i);
        }
    }
}
