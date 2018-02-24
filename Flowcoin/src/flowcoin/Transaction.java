package flowcoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import blockchain.BlockChain;
import blockchain.Util;

public class Transaction
{

    private String transactionId; // this is also the hash of the transaction.
    private final PublicKey sender; // senders address/public key.
    private final PublicKey recipient; // Recipients address/public key.
    private final double value;
    private byte[] signature; // this is to prevent anybody else from spending funds in our wallet.

    private List<TransactionInput> inputs = new ArrayList<>();
    private final List<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated.

    // Constructor:
    public Transaction(final PublicKey from, final PublicKey to, final double value,
        final List<TransactionInput> inputs)
    {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;

        // TODO maybe generate sig data here?
    }

    /**
     * Signs all the data we don't wish to be tampered with.
     *
     * @param privateKey The private key used to sign
     */
    public void generateSignature(final PrivateKey privateKey)
    {
        signature = Util.applyECDSASig(privateKey, getSignatureData());
    }

    /**
     * Verifies the data we signed hasnt been tampered with
     *
     * @return True if the data is valid
     */
    public boolean verifiySignature()
    {
        return Util.verifyECDSASig(getSender(), getSignatureData(), signature);
    }

    /**
     * Processes the transaction
     *
     * @return true if the transaction could be processed
     */
    public boolean process() //TODO dont access blockchain as singleton. research how blockchain access works
    {
        BlockChain blockchain = BlockChain.getInstance();
        if (!verifiySignature())
        {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent):
        for (TransactionInput i : getInputs())
        {
            i.setUTXO(blockchain.getUTXOs().get(i.getTransactionOutputId()));
        }

        //check if transaction is valid:
        if (getInputsValue() < BlockChain.MINIMUM_AMOUNT)
        {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        //generate transaction outputs:
        double leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        setTransactionId(calcHash());
        outputs.add(new TransactionOutput(this.getRecipient(), getValue(), getTransactionId())); //send value to recipient
        outputs.add(new TransactionOutput(this.getSender(), leftOver, getTransactionId())); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for (TransactionOutput o : outputs)
        {
            blockchain.getUTXOs().put(o.getId(), o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for (TransactionInput i : inputs)
        {
            if (i.getUTXO() == null)
            {
                continue; //if Transaction can't be found skip it
            }
            blockchain.getUTXOs().remove(i.getUTXO().getId());
        }

        return true;
    }

    /**
     * Generates a signature data string composed of all data to be signed
     *
     * @return The signature data stirng
     */
    private String getSignatureData()
    {
        // TODO also sign inputs and outputs, timestamp (when added)
        return Util.getBase64StringFromKey(getSender()) + Util.getBase64StringFromKey(getRecipient())
            + Double.toString(getValue());
    }

    /**
     * This Calculates the transaction hash (which will be used as its Id)
     *
     * @return The hash id
     */
    private String calcHash()
    {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return Util.applySha256(getSignatureData() + sequence);
    }

    /**
     * Calculates the sum of the unspent transaction output values (UTXO) of all inputs
     *
     * @return The sum of the value of all UTXOs
     */
    public double getInputsValue()
    {
        return getInputs().stream()
            .filter(input -> input.getUTXO() != null)
            .mapToDouble(input -> input.getUTXO().getValue())
            .sum();
    }

    /**
     * Calculates the sum of the value of all outputs
     *
     * @return The sum of the value of all outputs
     */
    public double getOutputsValue()
    {
        return getOutputs().stream().mapToDouble(TransactionOutput::getValue).sum();
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public List<TransactionOutput> getOutputs()
    {
        return outputs;
    }

    public PublicKey getRecipient()
    {
        return recipient;
    }

    public double getValue()
    {
        return value;
    }

    public void setTransactionId(final String transactionId)
    {
        this.transactionId = transactionId;
    }

    public PublicKey getSender()
    {
        return sender;
    }

    public List<TransactionInput> getInputs()
    {
        return inputs;
    }
}