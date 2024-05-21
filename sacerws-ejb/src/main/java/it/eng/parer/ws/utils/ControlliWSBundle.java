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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
// Last update: 2018-02-08 17:52:30.148111
package it.eng.parer.ws.utils;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;

/**
 *
 * @author sinatti_s
 */

public class ControlliWSBundle {

    public static final String DEFAULT_LOCALE = "it";

    /*
     * Metodi statici, implementazione causata dalla necessità di mantenere invariata l'interfaccia della classe
     * originale: un normalissimo Bundle con un file di properties
     */
    public static String getString(String cdControllo) {
        // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
        return lookupCacheRef().getString(cdControllo);
    }

    public static String getString(String key, Object... params) {
        // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
        return lookupCacheRef().getString(key, params);
    }

    // completo di tutti i campi (TODO: verificare se serve)
    public static ControlloWSResp getControllo(String cdControllo) {
        // recupero il dto con il codice di controllo
        return lookupCacheRef().getControllo(cdControllo);
    }

    public static List<ControlloWSResp> getControlli() {
        // recupero il dto con il codice di controllo
        return lookupCacheRef().getControlli();
    }

    public static List<ControlloWSResp> getControlliByCdFamiglia(String cdFamiglia) {
        // recupero il dto con il codice di controllo
        return lookupCacheRef().getControlliByCdFamiglia(cdFamiglia);
    }

    private static ControlliWSCache lookupCacheRef() {
        try {
            return (ControlliWSCache) new InitialContext().lookup("java:app/sacerws-ejb/ControlliWSCache");
        } catch (NamingException ex) {
            throw new SacerWsRuntimeException(
                    "Errore lookup singleton dei messaggi " + ExceptionUtils.getRootCauseMessage(ex),
                    SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    // erroriSistema

    public static final String CTRL_ERRORIDB = "persistenzaDB";

    public static final String CTRL_GENERIC_ERROR = "generico";

    // controlliGenerali

    public static final String CTRL_GENERALI_CHIAMATAWS = "controlloChiamataWS";

    public static final String CTRL_GENERALI_ESISTEVERSIONEXSD = "controlloEsisteVersioneXSD";

    public static final String CTRL_GENERALI_UTENTE = "controlloUtente";

    public static final String CTRL_GENERALI_XMLVSXSD = "controlloXMLvsXSD";

    // controlliXSD

    public static final String CTRL_XSD_UNIVOCITADOCUMENTI = "controlloUnivocitaDocumenti";

    public static final String CTRL_XSD_CORRISPONDENZAALLEGATI = "controlloCorrispondenzaAllegati";

    public static final String CTRL_XSD_CORRISPONDENZAANNESSI = "controlloCorrispondenzaAnnessi";

    public static final String CTRL_XSD_CORRISPONDENZAANNOTAZIONI = "controlloCorrispondenzaAnnotazioni";

    public static final String CTRL_XSD_CONTROLLOTIPOAGGIORNAMENTO = "controlloTipoAggiornamento";

    // controlliIntestazione

    public static final String CTRL_INTS_VERSIONEXSD = "controlloVersioneXSD";

    public static final String CTRL_INTS_AMBIENTE = "controlloAmbiente";

    public static final String CTRL_INTS_ENTE = "controlloEnte";

    public static final String CTRL_INTS_STRUTTURA = "controlloStruttura";

    public static final String CTRL_INTS_ABILITAZIONEUTENTE = "controlloAbilitazioniUtente";

    public static final String CTRL_INTS_CHIAVEUD = "controlloChiaveUd";

    public static final String CTRL_INTS_PARTIZIONI = "controlloPartizioni";

    // controlliUnitaDoc

    public static final String CTRL_UD_HASHSIP = "controlloHashSIP";

    public static final String CTRL_UD_TIPO = "controlloTipoUd";

    public static final String CTRL_UD_ABILUTENTE = "controlloAbilitazioniUtenteUnitaDoc";

    public static final String CTRL_UD_ABILAGGIORNAMENTO = "controlloAbilitazioneAggiornamento";

    public static final String CTRL_UD_STATOCONSERVAZIONE = "controlloStatoConservazione";

    public static final String CTRL_UD_COLLEGAMENTI = "controlloCollegamenti";

    public static final String CTRL_UD_PROFILOUD = "controlloProfiloUnitaDoc";

    public static final String CTRL_UD_PROFILONORMATIVO = "controlloProfiloNormativo";

    public static final String CTRL_UD_DATISPECIFICI = "controlloDatiSpecUnitaDoc";

    public static final String CTRL_UD_DATISPECIFICIMIGRAZ = "controlloDatiSpecUnitaDocMigraz";

    public static final String CTRL_UD_DOCUMENTI = "controlloDocumenti";

    public static final String CTRL_UD_COMPONENTI = "controlloComponenti";

    public static final String CTRL_UD_FASCICOLISECONDARIVSPRINCIPALE = "controlloFascicoliSecondariVsPrincipale";

    // controlliCollegamento

    public static final String CTRL_COLL_ESISTEUDOCCOLLEGATA = "controlloEsisteUnitaDocCollegata";

    public static final String CTRL_COLL_REGUDCOLLEGATA = "controlloRegistroUnitaDocCollegata";

    public static final String CTRL_COLL_DESCR = "controlloDescrCollegamento";

    // controlliChiaveDocumento

    public static final String CTRL_KEYDOC_CHIAVEDOC = "controlloChiaveDoc";

    // controlliChiaveComponente

    public static final String CTRL_KEYDOC_CHIAVECOMP = "controlloChiaveComp";

    // controlliDocumento

    public static final String CTRL_DOC_TIPODOC = "controlloTipoDoc";

    public static final String CTRL_DOC_ABILITAZIONEUTENTETIPO = "controlloAbilitazioniUtenteDoc";

    public static final String CTRL_DOC_DATISPECIFICI = "controlloDatiSpecDoc";

    public static final String CTRL_DOC_DATISPECIFICIMIGRAZ = "controlloDatiSpecDocMigraz";

    // controlliComponente

    public static final String CTRL_COMP_ORDINE = "controlloOrdineComp";

    public static final String CTRL_COMP_TIPO = "controlloTipoComp";

    public static final String CTRL_COMP_NOME = "controlloNomeComp";

    public static final String CTRL_COMP_DATISPEC = "controlloDatiSpecComp";

    public static final String CTRL_COMP_DATISPECMIGRAZ = "controlloDatiSpecCompMigraz";

}
