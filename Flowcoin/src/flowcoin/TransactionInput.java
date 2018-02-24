package flowcoin;

public class TransactionInput
{
    private final String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    private TransactionOutput UTXO; //Contains the Unspent transaction output

    /**
     * Constructor
     *
     * @param transactionOutputId Id of the relevant output to check ownership
     */
    public TransactionInput(final String transactionOutputId)
    {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUTXO()
    {
        return UTXO;
    }

    public void setUTXO(TransactionOutput uTXO)
    {
        UTXO = uTXO;
    }

    public String getTransactionOutputId()
    {
        return transactionOutputId;
    }
}
