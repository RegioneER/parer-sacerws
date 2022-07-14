/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.ejb.oracleBlb;

import java.security.SecureRandom;

/**
 *
 * @author Fioravanti_F
 */
public class NonMonotonicSequenceGen implements ISequenceGen {

    private static SecureRandom rand = new SecureRandom();

    private static final int MAX = 9999;
    private static final int MIN = 1000;

    @Override
    public long newSequenceNum(long baseSequence) {
        long newval = Long.parseLong(randInt() + "" + baseSequence);
        return newval;
    }

    /**
     * Returns a pseudo-random number between MIN and MAX, inclusive. The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @return Integer between min and max, inclusive.
     * 
     * @see java.util.Random#nextInt(int)
     */
    private static int randInt() {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((MAX - MIN) + 1) + MIN;

    }

}
