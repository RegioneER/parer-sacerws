/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.util;

/*
 * Ereditato da "vecchio" modello (vedi CryptoEnums)
 * Vengono riportati gli esiti (con relativi messaggi hard coded) e le tipologie di riferimento temporale,
 * che non sono riportabili sul wrapper della verificare firma (modello dati per incapsulare il risultato
 * della verifica firma e quindi non legati a quelle logiche).
 */
public class VerificaFirmaEnums {

    public enum EsitoControllo {

        POSITIVO("Controllo OK"), NEGATIVO("Controllo fallito"), WARNING("Controllo con Warning"), /**/
        NON_ESEGUITO("Non eseguito perchè il formato non è conforme"), FORMATO_NON_CONOSCIUTO, /**/
        FORMATO_NON_CONFORME, NON_AMMESSO_DELIB_45_CNIPA, DISABILITATO("Il controllo è disabilitato"), /**/
        NON_NECESSARIO("Il controllo non è necessario",
                "Il controllo non è necessario in quanto avviene tramite OCSP"), /**/
        ERRORE("Controllo non svolto per errore del sistema"), /**/
        CERTIFICATO_ERRATO("Il certificato non è un certificato di firma",
                "Errore sul controllo delle revoche del certificato, informazioni non scaricabili o non affidabili"), /**/
        CERTIFICATO_NON_VALIDO("Il certificato non è ancora valido",
                "Il certificato non presenta alcuni degli attributi previsti dallo standard ('signing-certificate', 'cert-diget', ecc.)"), /**/
        CERTIFICATO_REVOCATO("Il certificato è stato revocato o sospeso"), /**/
        CERTIFICATO_SCADUTO("Il certificato è scaduto"), /**/
        CERTIFICATO_SCADUTO_3_12_2009(
                "Il controllo non è svolto perché la CRL non è disponibile ed il certificato è scaduto prima del 3/12/2009"), /**/
        CRL_NON_SCARICABILE("Il controllo non è svolto perché la CRL non è scaricabile dal server",
                "CRL non scaricabile dal server perché il controllo sulla revoca avviene tramite OCSP"), /*
                                                                                                          * esiste anche
                                                                                                          * un messaggio
                                                                                                          * alternativo
                                                                                                          * derivante
                                                                                                          * dal caso
                                                                                                          * OCSP
                                                                                                          */
        CRL_NON_VALIDA("Il controllo non è svolto perché la CRL non è valida"), /**/
        CRL_SCADUTA("Il controllo non è svolto perché la CRL scaricata è scaduta"), SCONOSCIUTO, /**/
        OCSP_NON_SCARICABILE("Il controllo non è svolto perché la risposta OCSP non è scaricabile dal server"), /**/
        OCSP_SCONOSCIUTO("La risposta OCSP fornita dal server non ha informazioni sul certificato"), /**/
        OCSP_VALIDO("La risposta OCSP fornita dal server è valida e aggiornata"), /**/
        OCSP_NON_AGGIORNATO("La risposta OCSP fornita dal server non è aggiornata"), /**/
        OCSP_REVOCATO("La risposta OCSP fornita dal server indica che il certificato è stato revocato"), /**/
        REVOCHE_NON_CONSISTENTI(
                "Errore sul controllo delle revoche del certificato, informazioni non scaricabili o non affidabili");

        private final String message;
        private final String[] alternative;

        private EsitoControllo() {
            this.message = null;
            this.alternative = null;
        }

        private EsitoControllo(final String message, final String... alternative) {
            this.message = message;
            this.alternative = alternative;
        }

        public String message() {
            return this.message;
        }

        public String[] alternative() {
            return this.alternative != null && alternative.length > 0 ? alternative : new String[] { this.message };
        }
    }

    public enum TipoRifTemporale {

        DATA_FIRMA, DATA_VERS, MT_VERS_NORMA, MT_VERS_SEMPLICE, RIF_TEMP_VERS
    }

}
