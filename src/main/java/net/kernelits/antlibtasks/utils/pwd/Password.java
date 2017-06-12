package net.kernelits.antlibtasks.utils.pwd;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

// criptografa ou descriptografa uma senha
public class Password {

    private static final String key = "54hdf29kdjfiwmzskf301l23iADff44";

    public static String getPasswordCrypto(String passwd) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {

        byte[] encryptKey = key.getBytes("UTF-8");
        Cipher cipher = Cipher.getInstance("DESede");
        DESedeKeySpec keySpec = new DESedeKeySpec(encryptKey);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
        SecretKey secret = factory.generateSecret(keySpec);

        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(passwd.getBytes("UTF-8"));

        return Base64.encodeBase64String(cipherText);
    }

    public static String getPasswordText(String passwd) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {

        byte[] encryptKey = key.getBytes("UTF-8");
        Cipher cipher = Cipher.getInstance("DESede");
        DESedeKeySpec keySpec = new DESedeKeySpec(encryptKey);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
        SecretKey secret = factory.generateSecret(keySpec);

        byte[] cipherText = Base64.decodeBase64(passwd);

        cipher.init(Cipher.DECRYPT_MODE, secret);
        byte[] decipherText = cipher.doFinal(cipherText);

        return new String(decipherText);
    }

    public static void main(String[] args) throws Exception {

        // realiza a criptografia da senha
        if (args.length == 0)
            throw new Exception("Favor informar uma senha para criptografia");

        // obtem a senha
        String password = args[0];
        String passwdC = getPasswordCrypto(password);

        // imprime no console
        System.out.println("Senha criptografada: " + getPasswordCrypto(password));
        System.out.println("Senha descriptografada: " + getPasswordText(passwdC));
    }
}
