package blockchain;

import java.util.ArrayList;
import java.util.Date;

import flowcoin.Transaction;

/**
 * A blockchain block contains a number of transactions as payload
 *
 * @author Florian Koelbleitner
 */
public class Block
{

    private String hash;
    private final String previousHash;
    public String merkleRoot = "";
    public ArrayList<Transaction> transactions = new ArrayList<>();
    public Long timeStamp; //as number of milliseconds since 1/1/1970.
    public Integer nonce = 0;

    /**
     * Genesis block constructor
     */
    public Block()
    {
        this("0");
    }

    /**
     * Block constructor
     *
     * @param previousHash The hash of the previous block
     */
    public Block(final String previousHash)
    {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calcHash();
    }

    /**
     * Getter for hash
     *
     * @return hash
     */
    public String getHash()
    {
        return hash;
    }

    /**
     * Setter for previousHash
     *
     * @return previousHash
     */
    public String getPreviousHash()
    {
        return previousHash;
    }

    /**
     * Adds a transaction to this block and checks if it is valid
     *
     * @param transaction The transaction
     * @return true on success
     */
    public boolean addTransaction(final Transaction transaction)
    {
        if (transaction == null || !isGenesisBlock() && !transaction.process())
        {
            System.out.println("Transaction processing failed. Discarded.");
            return false;
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to block");
        return true;
    }

    /**
     * This method mines this block
     *
     * @param difficulty The difficulty to mine
     */
    public void mineBlock(final int difficulty)
    {
        System.out.println("Attempting to mine block..");
        merkleRoot = MerkleTree.merkleRoot(transactions);
        String target = Util.getHashTarget(difficulty);
        while (!hash.substring(0, difficulty).equals(target))
        {
            nonce++;
            hash = calcHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    /**
     * Calculates the block hash
     *
     * @return The block hash
     */
    public String calcHash()
    {
        return Util.applySha256(previousHash + timeStamp.toString() + nonce.toString() + merkleRoot);
    }

    private boolean isGenesisBlock()
    {
        return previousHash == "0";
    }
}