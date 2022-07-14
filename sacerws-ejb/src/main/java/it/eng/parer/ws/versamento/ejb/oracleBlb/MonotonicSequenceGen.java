/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.ejb.oracleBlb;

/**
 *
 * @author Fioravanti_F
 */
public class MonotonicSequenceGen implements ISequenceGen {

    @Override
    public long newSequenceNum(long baseSequence) {
        return baseSequence;
    }

}
