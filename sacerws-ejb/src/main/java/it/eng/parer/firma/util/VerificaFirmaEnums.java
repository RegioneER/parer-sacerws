/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

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

    public enum SacerIndication {

        POSITIVO("Controllo OK"), NEGATIVO("Controllo fallito"), WARNING("Controllo con Warning"),
        NON_ESEGUITO("Non eseguito perchè il formato non è conforme"), FORMATO_NON_CONOSCIUTO("Formato non conosciuto"),
        FORMATO_NON_CONFORME("Formato non conforme"), NON_AMMESSO_DELIB_45_CNIPA("Non ammetto delib45 cnipa"),
        DISABILITATO("Il controllo è disabilitato"), NON_NECESSARIO("Il controllo non è necessario"),
        ERRORE("Controllo non svolto per errore del sistema"), CERTIFICATO_ERRATO("Errore su certificato"),
        CERTIFICATO_NON_VALIDO("Errore validità del certificato"),
        CERTIFICATO_REVOCATO("Il certificato è stato revocato o sospeso"),
        CERTIFICATO_SCADUTO("Il certificato è scaduto"),
        CERTIFICATO_SCADUTO_3_12_2009(
                "Il controllo non è svolto perché la CRL non è disponibile ed il certificato è scaduto prima del 3/12/2009"), /**/
        CRL_NON_SCARICABILE("Il controllo non è svolto perché la CRL non è scaricabile dal server"),
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

        private SacerIndication() {
            this.message = null;
        }

        private SacerIndication(final String message) {
            this.message = message;
        }

        public String message() {
            return this.message;
        }

    }

    public enum TipoRifTemporale {

        DATA_FIRMA, DATA_VERS, MT_VERS_NORMA, MT_VERS_SEMPLICE, RIF_TEMP_VERS
    }

}
