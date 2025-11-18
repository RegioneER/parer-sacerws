/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.xml.versReq.ComponenteType;
import it.eng.parer.ws.xml.versReq.DocumentoType;
import it.eng.parer.ws.xml.versReq.SottoComponenteType;
import it.eng.parer.ws.xml.versReq.TipoConservazioneType;
import it.eng.parer.ws.xml.versReq.UnitaDocAggAllegati;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;

/**
 *
 * @author Fioravanti_F
 */
public class VerificaVersione {

    boolean migrazione = false;
    boolean datiSpecExt = false;
    boolean profiliUdXsd = false;
    boolean parseOk = true;
    EsitiVerfica tmpEsitiVerfica = null;

    public class EsitiVerfica {

        boolean tagMigrazioneVersNonSupp = false;
        boolean tagMigrazioneNoSistMig = false;
        boolean tagSistMigNoMigrazione = false;
        boolean datiMigrazioneVersNonSupp = false;
        boolean datiSpecExtVersNonSupp = false;
        //
        boolean flagConfigVersNonSupp = false;
        //
        boolean profiliUdXsdVersNonSupp = false;
        //
        boolean tagEsteso15VersNonSupp = false;
        //
        ArrayList<Object> params = null;
        //
        boolean flgErrore = false;
        //
        boolean flgWarning = false;
        String messaggio;
        String codErrore;

        public boolean isDatiMigrazioneVersNonSupp() {
            return datiMigrazioneVersNonSupp;
        }

        public void setDatiMigrazioneVersNonSupp(boolean datiMigrazioneVersNonSupp) {
            this.datiMigrazioneVersNonSupp = datiMigrazioneVersNonSupp;
        }

        public boolean isDatiSpecExtVersNonSupp() {
            return datiSpecExtVersNonSupp;
        }

        public void setDatiSpecExtVersNonSupp(boolean datiSpecExtVersNonSupp) {
            this.datiSpecExtVersNonSupp = datiSpecExtVersNonSupp;
        }

        public boolean isTagMigrazioneNoSistMig() {
            return tagMigrazioneNoSistMig;
        }

        public void setTagMigrazioneNoSistMig(boolean tagMigrazioneNoSistMig) {
            this.tagMigrazioneNoSistMig = tagMigrazioneNoSistMig;
        }

        public boolean isTagSistMigNoMigrazione() {
            return tagSistMigNoMigrazione;
        }

        public void setTagSistMigNoMigrazione(boolean tagSistMigNoMigrazione) {
            this.tagSistMigNoMigrazione = tagSistMigNoMigrazione;
        }

        public boolean isTagMigrazioneVersNonSupp() {
            return tagMigrazioneVersNonSupp;
        }

        public void setTagMigrazioneVersNonSupp(boolean tagMigrazioneVersNonSupp) {
            this.tagMigrazioneVersNonSupp = tagMigrazioneVersNonSupp;
        }

        public String getCodErrore() {
            return codErrore;
        }

        public void setCodErrore(String codErrore) {
            this.codErrore = codErrore;
        }

        public boolean isFlgErrore() {
            return flgErrore;
        }

        public void setFlgErrore(boolean flgErrore) {
            this.flgErrore = flgErrore;
        }

        public String getMessaggio() {
            return messaggio;
        }

        public void setMessaggio(String messaggio) {
            this.messaggio = messaggio;
        }

        /**
         * @return the flagConfigVersNonSupp
         */
        public boolean isFlagConfigVersNonSupp() {
            return flagConfigVersNonSupp;
        }

        /**
         * @param flagConfigVersNonSupp the flagConfigVersNonSupp to set
         */
        public void setFlagConfigVersNonSupp(boolean flagConfigVersNonSupp) {
            this.flagConfigVersNonSupp = flagConfigVersNonSupp;
        }

        /**
         * @return the flgWarning
         */
        public boolean isFlgWarning() {
            return flgWarning;
        }

        /**
         * @param flgWarning the flgWarning to set
         */
        public void setFlgWarning(boolean flgWarning) {
            this.flgWarning = flgWarning;
        }

        /**
         * @return the params
         */
        public List<Object> getParams() {
            if (params == null) {
                params = new ArrayList<>();
            }
            return params;
        }

        public boolean isProfiliUdXsdVersNonSupp() {
            return profiliUdXsdVersNonSupp;
        }

        public void setProfiliUdXsdVersNonSupp(boolean profiliUdXsdVersNonSupp) {
            this.profiliUdXsdVersNonSupp = profiliUdXsdVersNonSupp;
        }

        public boolean isTagEsteso15VersNonSupp() {
            return tagEsteso15VersNonSupp;
        }

        public void setTagEsteso15VersNonSupp(boolean tagEsteso15VersNonSupp) {
            this.tagEsteso15VersNonSupp = tagEsteso15VersNonSupp;
        }

    }

    public EsitiVerfica getEsitiVerfica() {
        return tmpEsitiVerfica;
    }

    public void verifica(RispostaWS rispostaWS, VersamentoExt versamento) {
        UnitaDocumentaria parsedUnitaDoc = versamento.getVersamento();
        EsitoVersamento myEsito = rispostaWS.getIstanzaEsito();
        parseOk = true;
        migrazione = false;
        datiSpecExt = false;
        profiliUdXsd = false;

        tmpEsitiVerfica = new EsitiVerfica();

        if (TipoConservazioneType.MIGRAZIONE.name()
                .equals(myEsito.getConfigurazione().getTipoConservazione())) {
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)) {
                migrazione = true;
            } else {
                parseOk = false;
                tmpEsitiVerfica.setTagMigrazioneVersNonSupp(true);
            }
        }

        if (parseOk && migrazione && (myEsito.getConfigurazione().getSistemaDiMigrazione() == null
                || myEsito.getConfigurazione().getSistemaDiMigrazione().trim().length() == 0)) {
            parseOk = false;
            tmpEsitiVerfica.setTagMigrazioneNoSistMig(true);
        }

        if (parseOk && !migrazione && myEsito.getConfigurazione().getSistemaDiMigrazione() != null
                && myEsito.getConfigurazione().getSistemaDiMigrazione().trim().length() > 0) {
            parseOk = false;
            tmpEsitiVerfica.setTagSistMigNoMigrazione(true);
        }

        if (parseOk) {
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_DATISPEC_EXT)) {
                datiSpecExt = true;
            }

            if (parsedUnitaDoc.getDatiSpecifici() != null && datiSpecExt == false) {
                parseOk = false;
                tmpEsitiVerfica.setDatiSpecExtVersNonSupp(true);
            }

            if (parsedUnitaDoc.getDatiSpecificiMigrazione() != null && migrazione == false) {
                parseOk = false;
                tmpEsitiVerfica.setDatiMigrazioneVersNonSupp(true);
            }
        }

        if (parseOk) {
            this.verificaDocumento(parsedUnitaDoc.getDocumentoPrincipale());
        }

        if (parseOk && parsedUnitaDoc.getAllegati() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = parsedUnitaDoc.getAllegati()
                    .getAllegato().iterator();

            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                this.verificaDocumento(tmpDocumentoType);
            }
        }

        if (parseOk && parsedUnitaDoc.getAnnessi() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = parsedUnitaDoc.getAnnessi().getAnnesso()
                    .iterator();

            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                this.verificaDocumento(tmpDocumentoType);
            }
        }

        if (parseOk && parsedUnitaDoc.getAnnotazioni() != null) {
            Iterator<? extends DocumentoType> tmpEnumDoc = parsedUnitaDoc.getAnnotazioni()
                    .getAnnotazione().iterator();
            while (tmpEnumDoc.hasNext()) {
                DocumentoType tmpDocumentoType = (DocumentoType) tmpEnumDoc.next();
                this.verificaDocumento(tmpDocumentoType);
            }
        }
        //
        if (parseOk && parsedUnitaDoc.getConfigurazione() != null
                && !versamento.getModificatoriWSCalc()
                        .contains(ModificatoriWS.TAG_ABILITA_FORZA_1_5)
                && (parsedUnitaDoc.getConfigurazione().isForzaHash() != null
                        || parsedUnitaDoc.getConfigurazione().isForzaFormatoFile() != null
                        || parsedUnitaDoc.getConfigurazione().isForzaFormatoNumero() != null)) {
            parseOk = false;
            tmpEsitiVerfica.setFlagConfigVersNonSupp(true);
            // which one
            List<String> tags = new ArrayList<>();
            if (parsedUnitaDoc.getConfigurazione().isForzaHash() != null) {
                tags.add("<ForzaHash>");
            }
            if (parsedUnitaDoc.getConfigurazione().isForzaFormatoNumero() != null) {
                tags.add("<ForzaFormatoNumero>");
            }
            if (parsedUnitaDoc.getConfigurazione().isForzaFormatoFile() != null) {
                tags.add("<ForzaFormatoFile>");
            }
            //
            tmpEsitiVerfica.getParams().add(String.join(",", tags));
        }
        //
        if (parseOk) {
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_PROFILI_1_5)) {
                profiliUdXsd = true;
            }

            // profilo normativo (UNI_DOC)
            if (!profiliUdXsd && parsedUnitaDoc.getProfiloNormativo() != null) {
                parseOk = false;
                tmpEsitiVerfica.setProfiliUdXsdVersNonSupp(true);
                // params (see MessaggiWSBundle.XSD_006_007)
                tmpEsitiVerfica.getParams()
                        .addAll(Arrays.asList(TipiEntitaSacer.UNI_DOC.descrivi(),
                                parsedUnitaDoc.getIntestazione().getTipologiaUnitaDocumentaria(),
                                "<ProfiloNormativo>"));
            }
        }
        //
        if (parseOk
                && !versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_ESTESI_1_5_OUT)
                && parsedUnitaDoc.getIntestazione().getVersatore().getUtente() != null) {
            parseOk = false;
            tmpEsitiVerfica.setTagEsteso15VersNonSupp(true);

            // which one
            List<String> tags = new ArrayList<>();
            if (parsedUnitaDoc.getIntestazione().getVersatore().getUtente() != null) {
                tags.add("<Intestazione><Versatore><Utente>");
            }
            //
            tmpEsitiVerfica.getParams().add(String.join(",", tags));
        }
        this.generaMessaggioErrore(tmpEsitiVerfica.getParams().stream().toArray(Object[]::new));
        //
        this.generaMessaggioWarning();

    }

    public void verifica(RispostaWSAggAll rispostaWS, VersamentoExtAggAll versamento) {
        UnitaDocAggAllegati parsedUnitaDoc = versamento.getVersamento();
        EsitoVersAggAllegati myEsito = rispostaWS.getIstanzaEsito();
        parseOk = true;
        migrazione = false;
        datiSpecExt = false;

        tmpEsitiVerfica = new EsitiVerfica();

        if (TipoConservazioneType.MIGRAZIONE.name()
                .equals(myEsito.getConfigurazione().getTipoConservazione())) {
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)) {
                migrazione = true;
            } else {
                parseOk = false;
                tmpEsitiVerfica.setTagMigrazioneVersNonSupp(true);
            }
        }

        if (parseOk && migrazione && (myEsito.getConfigurazione().getSistemaDiMigrazione() == null
                || myEsito.getConfigurazione().getSistemaDiMigrazione().trim().length() == 0)) {
            parseOk = false;
            tmpEsitiVerfica.setTagMigrazioneNoSistMig(true);
        }

        if (parseOk && !migrazione && myEsito.getConfigurazione().getSistemaDiMigrazione() != null
                && myEsito.getConfigurazione().getSistemaDiMigrazione().trim().length() > 0) {
            parseOk = false;
            tmpEsitiVerfica.setTagSistMigNoMigrazione(true);
        }

        if (parseOk) {
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_DATISPEC_EXT)) {
                datiSpecExt = true;
            }
        }

        if (parseOk) {
            if (parsedUnitaDoc.getAllegato() != null) {
                this.verificaDocumento(parsedUnitaDoc.getAllegato());
            }
            if (parsedUnitaDoc.getAnnesso() != null) {
                this.verificaDocumento(parsedUnitaDoc.getAnnesso());
            }
            if (parsedUnitaDoc.getAnnotazione() != null) {
                this.verificaDocumento(parsedUnitaDoc.getAnnotazione());
            }
        }

        //
        if (parseOk && parsedUnitaDoc.getConfigurazione() != null
                && !versamento.getModificatoriWSCalc()
                        .contains(ModificatoriWS.TAG_ABILITA_FORZA_1_5)
                && (parsedUnitaDoc.getConfigurazione().isForzaHash() != null
                        || parsedUnitaDoc.getConfigurazione().isForzaFormatoFile() != null
                        || parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento() != null)) {
            parseOk = false;
            tmpEsitiVerfica.setFlagConfigVersNonSupp(true);
            // which one
            if (parsedUnitaDoc.getConfigurazione().isForzaHash() != null) {
                tmpEsitiVerfica.getParams().add("<ForzaHash>");
            }
            if (parsedUnitaDoc.getConfigurazione().isForzaFormatoFile() != null) {
                tmpEsitiVerfica.getParams().add("<ForzaFormatoFile>");
            }
            if (parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento() != null) {
                tmpEsitiVerfica.getParams().add("<ForzaAggiuntaDocumento>");
            }
        }

        this.generaMessaggioErrore(tmpEsitiVerfica.getParams().stream().toArray(Object[]::new));
        //
        this.generaMessaggioWarning();
    }

    public void verifica(RispostaWSFascicolo rispostaWS, VersFascicoloExt versamento) {
        IndiceSIPFascicolo parsedFascicolo = versamento.getVersamento();
        CompRapportoVersFascicolo myEsito = rispostaWS.getCompRapportoVersFascicolo();
        parseOk = true;

        tmpEsitiVerfica = new EsitiVerfica();

        /**
         * #################################### TODO : logica da implementare
         * ####################################
         */
        this.generaMessaggioErrore();
        //
        this.generaMessaggioWarning();
    }

    public void verifica(RispostaWSUpdVers rispostaWS, UpdVersamentoExt versamento) {
        IndiceSIPAggiornamentoUnitaDocumentaria parsedUpd = versamento.getVersamento();
        CompRapportoUpdVers myEsito = rispostaWS.getCompRapportoUpdVers();
        parseOk = true;

        tmpEsitiVerfica = new EsitiVerfica();

        /**
         * #################################### TODO : logica da implementare
         * ####################################
         */
        this.generaMessaggioErrore();
        //
        this.generaMessaggioWarning();

    }

    private void verificaDocumento(DocumentoType doc) {

        // non devo testare la presenza di dati specifici tradizionali perché questi
        // sono permessi da sempre

        if (doc.getDatiSpecificiMigrazione() != null && migrazione == false) {
            parseOk = false;
            tmpEsitiVerfica.setDatiMigrazioneVersNonSupp(true);
        }

        if (parseOk) {
            Iterator<? extends ComponenteType> tmpEnumCompo = doc.getStrutturaOriginale()
                    .getComponenti().getComponente().iterator();
            while (tmpEnumCompo.hasNext()) {
                ComponenteType tmpComponente = (ComponenteType) tmpEnumCompo.next();

                if (tmpComponente.getDatiSpecifici() != null && datiSpecExt == false) {
                    parseOk = false;
                    tmpEsitiVerfica.setDatiSpecExtVersNonSupp(true);
                }

                if (tmpComponente.getDatiSpecificiMigrazione() != null && migrazione == false) {
                    parseOk = false;
                    tmpEsitiVerfica.setDatiMigrazioneVersNonSupp(true);
                }

                if (parseOk && tmpComponente.getSottoComponenti() != null) {
                    Iterator<? extends SottoComponenteType> tmpEnumSottoCompo = tmpComponente
                            .getSottoComponenti().getSottoComponente().iterator();
                    while (tmpEnumSottoCompo.hasNext()) {
                        SottoComponenteType tmpSottoComponente = (SottoComponenteType) tmpEnumSottoCompo
                                .next();

                        if (tmpSottoComponente.getDatiSpecifici() != null && datiSpecExt == false) {
                            parseOk = false;
                            tmpEsitiVerfica.setDatiSpecExtVersNonSupp(true);
                        }

                        if (tmpSottoComponente.getDatiSpecificiMigrazione() != null
                                && migrazione == false) {
                            parseOk = false;
                            tmpEsitiVerfica.setDatiMigrazioneVersNonSupp(true);
                        }
                    }
                }
            }
        }
    }

    private void generaMessaggioErrore(Object... params) {
        if (!parseOk) {
            tmpEsitiVerfica.setFlgErrore(true);
            if (tmpEsitiVerfica.isTagMigrazioneVersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_001);
                tmpEsitiVerfica
                        .setMessaggio(MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_001));
            }
            if (tmpEsitiVerfica.isTagMigrazioneNoSistMig()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_002);
                tmpEsitiVerfica
                        .setMessaggio(MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_002));
            }
            if (tmpEsitiVerfica.isTagSistMigNoMigrazione()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_003);
                tmpEsitiVerfica
                        .setMessaggio(MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_003));
            }
            if (tmpEsitiVerfica.isDatiMigrazioneVersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_004);
                tmpEsitiVerfica
                        .setMessaggio(MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_004));
            }
            if (tmpEsitiVerfica.isDatiSpecExtVersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_005);
                tmpEsitiVerfica
                        .setMessaggio(MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_005));
            }
            if (tmpEsitiVerfica.isFlagConfigVersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_011_003);
                tmpEsitiVerfica.setMessaggio(
                        MessaggiWSBundle.getString(MessaggiWSBundle.XSD_011_003, params));
            }
            if (tmpEsitiVerfica.isProfiliUdXsdVersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_006_007);
                tmpEsitiVerfica.setMessaggio(
                        MessaggiWSBundle.getString(MessaggiWSBundle.XSD_006_007, params));
            }
            if (tmpEsitiVerfica.isTagEsteso15VersNonSupp()) {
                tmpEsitiVerfica.setCodErrore(MessaggiWSBundle.XSD_011_003);
                tmpEsitiVerfica.setMessaggio(
                        MessaggiWSBundle.getString(MessaggiWSBundle.XSD_011_003, params));
            }
        }
    }

    // TODO : al momento non implementato (-> eventualmente lo fosse da cambiare
    // l'XSD in risposta)
    private void generaMessaggioWarning(Object... params) {
        if (!parseOk) {
            tmpEsitiVerfica.setFlgWarning(true);
        }
    }

    public static String elabWsKey(String versioniWsName) {
        return ParametroApplDB.VERSIONI_WS_PREFIX.concat(versioniWsName);
    }

    public static List<String> getWsVersionList(String versioniWsName,
            Map<String, String> mapWsVersion) {
        // key name on map
        String versioniWsKey = elabWsKey(versioniWsName);
        if (mapWsVersion == null || !mapWsVersion.containsKey(versioniWsKey)) {
            return new ArrayList<>();// empty list
        } else {
            return Arrays.asList(mapWsVersion.get(versioniWsKey).split("\\|")); // FIXME : separator
            // on code
        }
    }

    public static String latestVersion(String versioniWsName, Map<String, String> mapWsVersion) {
        List<String> versioniWs = getWsVersionList(versioniWsName, mapWsVersion);
        if (versioniWs.isEmpty()) {
            /**
             * Di norma questo caso non dovrebbe mai verificarsi in quanto all'atto
             * dell'inizializzazione del ws la mappa contenente i valori è già stata testata @link
             * ControlliWS.caricaVersioniWSDefault
             */
            return StringUtils.EMPTY;
        }
        Collections.sort(versioniWs, new Comparator<String>() {
            @Override
            public int compare(String v1, String v2) {
                String[] v1nodot = v1.split("\\."); // FIXME : dot sep on code
                String[] v2nodot = v2.split("\\."); // FIXME : dot sep on code
                int major1 = major(v1nodot);
                int major2 = major(v2nodot);
                if (major1 == major2) {
                    return minor(v1nodot).compareTo(minor(v2nodot));
                }
                return major1 > major2 ? 1 : -1;
            }

            private int major(String[] version) {
                return Integer.parseInt(version[0]);
            }

            private Integer minor(String[] version) {
                // right padding 0 from right (comparable digits)
                return version.length > 1
                        ? Integer.parseInt(StringUtils.rightPad(version[1], 4, "0"))
                        : 0;
            }

        });

        return versioniWs.get(versioniWs.size() - 1);// the last one
    }

}
