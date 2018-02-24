package flowcoin;

import java.security.PublicKey;

import blockchain.Util;

public class TransactionOutput
{
    private final String id;
    private final PublicKey recipient; //also known as the new owner of these coins.
    private final double value; //the amount of coins they own
    private final String parentTransactionId; //the id of the transaction this output was created in

    /**
     * Constructor
     *
     * @param recipient The key the output belongs to
     * @param value The value
     * @param parentTransactionId The id of the parent transaction
     */
    public TransactionOutput(final PublicKey recipient, final double value, final String parentTransactionId)
    {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Util
            .applySha256(Util.getBase64StringFromKey(recipient) + Double.toString(value) + parentTransactionId);
    }

    /**
     * Checks if the output belongs to the given key
     *
     * @param publicKey The key
     * @return true if the recipient matches the given key
     */
    public boolean belongsTo(final PublicKey publicKey)
    {
        return (publicKey == getRecipient());
    }

    public double getValue()
    {
        return value;
    }

    public String getId()
    {
        return id;
    }

    public PublicKey getRecipient()
    {
        return recipient;
    }

}
