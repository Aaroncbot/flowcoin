package flowcoin;

import blockchain.BlockChain;

public class Test
{

    public static void main(final String[] args)
    {
        BlockChain blockChain = BlockChain.getInstance();
        blockChain.test();
    }

}
