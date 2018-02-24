package flowcoin;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockchain.BlockChain;

public class Wallet
{
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final Map<String, TransactionOutput> UTXOs = new HashMap<>(); //only UTXOs owned by this wallet.

    /**
     * Constructor for a new wallet
     */
    public Wallet()
    {
        generateKeyPair();
    }

    public Transaction sendFunds(final PublicKey recipient, final double amount)
    {
        if (getBalance() < amount)
        { //gather balance and check funds.
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        List<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet())
        {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > amount)
            {
                break;
            }
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, amount, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs)
        {
            UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }

    private void generateKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); //256 bytes provides an acceptable security level
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public PrivateKey getPrivateKey()
    {
        // TODO only for testing of course ;)
        return privateKey;
    }

    public double getBalance()
    {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : BlockChain.getInstance().getUTXOs().entrySet())
        {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.belongsTo(publicKey))
            { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.getId(), UTXO); //add it to our list of unspent transactions.
                total += UTXO.getValue();
            }
        }
        return total;
    }
}
