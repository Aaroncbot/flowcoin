package blockchain;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class Util
{
    /**
     * To prevent instatiation
     */
    private Util()
    {
    }

    /**
     * Applies SHA-256 to a string and returns the result
     *
     * @param input The input data
     * @return The SHA-256 encoded value
     */
    public static String applySha256(final String input)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // apply SHA-256
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for (byte element : hash)
            {
                String hex = Integer.toHexString(0xff & element);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies ECDSA Signature and returns the result ( as bytes )
     *
     * @param privateKey The provate key
     * @param input The input string
     * @return The ECDSA signature bytes
     */
    public static byte[] applyECDSASig(final PrivateKey privateKey, final String input)
    {
        Signature dsa;
        byte[] output = new byte[0];
        try
        {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * Verifies an ECDSA signature.
     *
     * @param publicKey The public key
     * @param data The string payload
     * @param signature The signature to verify
     * @return true when the signature is valid, false otherwise
     */
    public static boolean verifyECDSASig(final PublicKey publicKey, final String data, final byte[] signature)
    {
        try
        {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the base64 encoded signature of a key
     *
     * @param key The key to encode
     * @return The base64 encoded key
     */
    public static String getBase64StringFromKey(final Key key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Creates a difficulty string as hashing target to compare to.
     *
     * @param difficulty The mining difficulty
     * @return A string containing difficulty * "0"
     */
    public static String getHashTarget(final int difficulty)
    {
        return new String(new char[difficulty]).replace('\0', '0');
    }
}
