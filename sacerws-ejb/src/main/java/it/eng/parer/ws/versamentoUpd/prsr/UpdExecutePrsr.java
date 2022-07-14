/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.prsr;

import it.eng.parer.ws.versamentoUpd.prsr.strategy.IUpdStrategyPrsr;

/*
 * Classe di supporto per l'esecuzione del parsing attraverso una strategia specifica 
 * A) verifica/parsing dei collegamenti
 * B)       -            su id documenti
 * C) ....               conteggio documenti
 */
public class UpdExecutePrsr {

    /*
     * TODO: per il momento "l'esecutore" prevede attraverso una "strategia" di controllo l'esecuzione dei controlli
     * previsti per quell'implementazione
     */
    public boolean eseguiControlli(IUpdStrategyPrsr strategy) {
        return strategy.eseguiControlli();
    }

}