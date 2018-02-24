package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import flowcoin.Transaction;

public class MerkleTree
{
    /**
     * constructor private for helper classes
     */
    private MerkleTree()
    {

    }

    /**
     * execute merkle_tree and set root.
     */
    public static String merkleRoot(final List<Transaction> transactions)
    {
        if (!transactions.isEmpty())
        {
            List<String> idList =
                transactions.stream().map(Transaction::getTransactionId).collect(Collectors.toList());

            List<String> curLevelList = getNextLevelList(idList);
            while (curLevelList.size() != 1)
            {
                curLevelList = getNextLevelList(curLevelList);
            }
            return curLevelList.get(0);
        }
        return "";
    }

    /**
     * returns the parent Node Hash List.
     *
     * @param tempTxList
     * @return The parent node hash list
     */
    private static List<String> getNextLevelList(final List<String> txList)
    {
        List<String> newTxList = new ArrayList<>();
        int index = 0;
        while (index < txList.size())
        {
            String left = txList.get(index);
            index++;

            String right = "";
            if (index != txList.size())
            {
                right = txList.get(index);
            }
            // add SHA256 of left+right on parent node
            newTxList.add(Util.applySha256(left + right));
            index++;
        }
        return newTxList;
    }
}
