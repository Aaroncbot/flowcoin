package blockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import flowcoin.Transaction;
import flowcoin.TransactionInput;
import flowcoin.TransactionOutput;
import flowcoin.Wallet;

public class BlockChain
{
    private static BlockChain instance;
    public static final float MINIMUM_AMOUNT = 0.1f;

    public static BlockChain getInstance()
    {
        if (instance == null)
        {
            instance = new BlockChain();
        }
        return instance;
    }

    /**
     * Singletons have to be accessed via getInstance()
     */
    private BlockChain()
    {
    }

    private final ArrayList<Block> blockchain = new ArrayList<>();
    private final int difficulty = 5;
    private final HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); //list of all unspent transactions.

    private Wallet walletA;
    private Wallet walletB;
    public static Transaction genesisTransaction;

    public void test()
    {
        //Setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();

        //Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        genesisTransaction.generateSignature(coinbase.getPrivateKey()); //manually sign the genesis transaction
        genesisTransaction.setTransactionId("0"); //manually set the transaction id
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(),
            genesisTransaction.getValue(), genesisTransaction.getTransactionId())); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block();
        addBlock(genesisBlock);

        //testing
        Block block1 = new Block(genesisBlock.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20f));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
    }

    /**
     * @param newBlock
     */
    public void addBlock(final Block newBlock)
    {
        newBlock.mineBlock(difficulty); // TODO no mining in here, client API!!
        blockchain.add(newBlock);
    }

    /**
     * Tests if the blockchain is valid<br>
     * - all hashes match<br>
     * - all blocks have been mined
     *
     * @return True if the blockchain is valid
     */
    public boolean isChainValid()
    {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++)
        {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calcHash()))
            {
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash()))
            {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget))
            {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++)
            {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifiySignature())
                {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue())
                {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.getInputs())
                {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null)
                    {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.getUTXO().getValue() != tempOutput.getValue())
                    {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs())
                {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient())
                {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender())
                {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public HashMap<String, TransactionOutput> getUTXOs()
    {
        return UTXOs;
    }
}
