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
package it.eng.parer.ws.versFascicoli.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.entity.DecLivelloTitol;
import it.eng.parer.entity.DecVoceTitol;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.RomanConverter;
import it.eng.parer.ws.versFascicoli.dto.ConfigNumFasc;
import it.eng.parer.ws.versFascicoli.dto.ConfigNumFasc.ParteNumero;
import it.eng.parer.ws.versFascicoli.ejb.ControlliFascicoli;

/**
 *
 * @author sinatti_s
 */
public class KeyOrdFascUtility {

    public enum TipiCalcolo {

        ALFABETICO("{Alfabetico}"), ALFANUMERICO("{Alfanumerico}"), NUMERICO("{Numerico}"),
        NUMERICO_GENERICO("{Numerico Generico}"), NUMERI_ROMANI("{Numeri Romani}"), PARTE_GENERICO("{Generico}"),
        GENERICO("{Formato Generico}");

        private String descrizione;

        private TipiCalcolo(String descrizione) {
            this.descrizione = descrizione;
        }

        public String descrivi() {
            return descrizione;
        }
    }

    //
    private final ConfigNumFasc configNumFasc;
    private final Pattern patternCompilato;
    private final Pattern patternNumericoGen;
    private String descKeyDaVerificare;
    private String descKeyVersata;
    private int maxLenNumero;

    private final int PAD_NUM_4 = 4;
    private final int PAD_NUM_20 = 20;

    // questa regexp ^[^\d]*(\d+)[^\d]*$
    private final String REGEXP_NUMERICO_GEN = "^[^\\d]*(\\d+)[^\\d]*$";

    // stateless ejb per i controlli specifici del fascicolo
    private ControlliFascicoli controlliFascicoli = null;

    // TODO: desc da controllare
    private final String MESS_ERR_CHIAVE_TOOLONG = "La lunghezza del tag [numero] della chiave non può superare %s caratteri";
    private final String MESS_ERR_FORMATO_CHIAVE = "Il numero non rispetta il formato previsto ";
    private final String MESS_ERR_FORMATO_PARTE = "La parte %s non coincide con i valori ammessi (%s)";
    private final String MESS_ERR_FORMATO_MIN_MAX = "La parte %s indicata non rispetta il numero di caratteri previsti";
    private final String MESS_ERR_ANNO_PARTE = "La parte %s non coincide con l'anno del fascicolo";
    private final String MESS_ERR_TITOLARIO_IN_SIP = "La parte %s non coincide con l'indice di classificazione del fascicolo";
    private final String MESS_ERR_TITOLARIO_NOT_IN_DB = "Il titolario di classificazione non è presente alla data di apertura del fascicolo o esiste ma non è presente una voce con codice composito %s";
    private final String MESS_ERR_NUMERO_TROPPO_GRANDE = "La parte %s definisce un numero troppo grande (max 9.223.372.036.854.775.807)";
    private final String MESS_ERR_NUMERO_NON_TROVATO = "La parte %s non contiene una sequenza di numeri contigui";
    private final String MESS_ERR_CHIAVECALC_TOOLONG = "La dimensione di \"<anno>-\" + dimensione del numero deve essere minore uguale a 100 byte";
    private final String MESS_ERR_666 = "Errore generico %s";

    public KeyOrdFascUtility(ConfigNumFasc config, int maxLenNumero) {
        this.configNumFasc = config;
        this.maxLenNumero = maxLenNumero;
        // prepara il test globale sulla regexp
        String regex = configNumFasc.getRegExpCalc();
        // questa compilazione nel costruttore è utile nel job di controllo,
        // in cui il metodo verificaChiave viene invocato molte volte usando
        // sempre lo stesso pattern
        patternCompilato = Pattern.compile(regex);
        // compilo questa regexp in ogni caso:
        // il costo iniziale è relativamente basso, ma se la devo usare in modo
        // ripetuto come nel job di controllo, il risparmio può essere sensibile
        patternNumericoGen = Pattern.compile(REGEXP_NUMERICO_GEN);

    }

    public RispostaControlli verificaChiave(CSChiaveFasc chiave, long idStrutt, Date dtApertura,
            String indiceClassifSIP) {
        return this.verificaChiave(chiave, null, idStrutt, dtApertura, indiceClassifSIP);
    }

    public RispostaControlli verificaChiave(CSChiaveFasc chiaveDaVerificare, String descChiaveSIPVersata, long idStrutt,
            Date dtApertura, String indiceClassifSIP) {
        RispostaControlli tmpControlli = new RispostaControlli();
        //
        descKeyDaVerificare = MessaggiWSFormat.formattaUrnPartFasc(chiaveDaVerificare);
        descKeyVersata = descChiaveSIPVersata;

        // recupero dell'EJB per verifica classificazione
        try {
            controlliFascicoli = (ControlliFascicoli) new InitialContext().lookup("java:module/ControlliFascicoli");
        } catch (NamingException ex) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_666, ex.getMessage()));
            return tmpControlli;// sempre in caso di errore 666
        }

        String numero = chiaveDaVerificare.getNumero();
        Long progressivoCalc = null;
        StringBuilder tmpKeyOrdCalc = new StringBuilder();
        // test solo per parti di tipo CLASSIF
        StringBuilder cdCompositoVoceTitolsb = new StringBuilder(0);
        String cdCompositoVoceTitolLastSep = null;

        if (!this.verificaLunghezzaNumChiave(numero)) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_CHIAVE_TOOLONG, maxLenNumero));
            return tmpControlli;
        }

        // test per parti, dopo aver verificato la regexp globale
        Matcher m = patternCompilato.matcher(numero);
        if (m.find() && m.groupCount() == configNumFasc.getParti().size()) {
            // inizio loop sulle parti trovate
            for (int iteratore = 0; iteratore < m.groupCount(); iteratore++) {
                String parte = m.group(iteratore + 1);
                ConfigNumFasc.ParteNumero prNumero = configNumFasc.getParti().get(iteratore);

                // test valido solo per parti classificazione
                tmpControlli = this.verificaClassificazione(chiaveDaVerificare, parte, prNumero, cdCompositoVoceTitolsb,
                        idStrutt, dtApertura);
                if (!tmpControlli.isrBoolean()) {
                    return tmpControlli;
                } else if (tmpControlli.getrString() != null) {
                    cdCompositoVoceTitolLastSep = tmpControlli.getrString();// separatore per verifica
                }

                // test validi per tutti i tipi
                tmpControlli = this.verificheGeneriche(chiaveDaVerificare, parte, prNumero);
                if (!tmpControlli.isrBoolean()) {
                    return tmpControlli;
                }

                // TEST specifici per i vari tipi,
                tmpControlli = this.verificheSpecifiche(chiaveDaVerificare, tmpKeyOrdCalc, parte, prNumero);
                if (!tmpControlli.isrBoolean()) {
                    return tmpControlli;
                }

                // se la parte è un numero (decimale o romano) e la devo tenere come prog calcolato...
                if (prNumero.isUsaComeProgressivo() && tmpControlli.getrLong() != -1) {
                    progressivoCalc = tmpControlli.getrLong();
                }
            }
        } else {
            // questo caso (regexp che match-a ma numero di gruppi trovato diverso dall'atteso)
            // si presenta se la configurazione del periodo ha definito un criterio ambiguo.
            // tecnicamente sarebbe un errore di configurazione (interno, con 666)
            // ma così il messaggio è più bello...
            this.impostaErrore(tmpControlli, MESS_ERR_FORMATO_CHIAVE + configNumFasc.getDescRegExp());
            return tmpControlli;
        }

        // sono uscito vivo dai test, sulla base del codice composito calcolato verifico su SIP
        tmpControlli = this.verificaClassificazioneSIP(indiceClassifSIP, cdCompositoVoceTitolsb,
                cdCompositoVoceTitolLastSep);
        if (!tmpControlli.isrBoolean()) {
            // codice composito calcolato NON coincide con indice SIP
            return tmpControlli;
        }

        // sono uscito vivo dai test, calcolo e verifico la chiave calcolata
        tmpControlli = this.produciChiaveOrd(chiaveDaVerificare, tmpKeyOrdCalc);
        if (!tmpControlli.isrBoolean()) {
            // se la chiave calcolata è troppo lunga, rendo errore
            return tmpControlli;
        }
        //
        KeyOrdResult keyOrdResult = new KeyOrdResult();
        tmpControlli.setrObject(keyOrdResult);
        keyOrdResult.setProgressivoCalcolato(progressivoCalc);
        keyOrdResult.setKeyOrdCalcolata(tmpControlli.getrString());
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaClassificazioneSIP(String indiceClassifSIP, StringBuilder cdCompositoVoceTitolsb,
            String cdCompositoVoceTitolLastSep) {
        RispostaControlli tmpControlli = new RispostaControlli();
        /*
         * al termine dell'iterazione sulle parti di tipo classificazione dovrei avere l'indice completo a questo punto
         * si effettuano le verifiche su SIP
         */
        if (cdCompositoVoceTitolsb != null && cdCompositoVoceTitolsb.length() > 0) {
            /*
             * se l'ultimo carattere coincide con il separatore lo rimuovo (valido per la verifica)
             */
            if (cdCompositoVoceTitolLastSep != null && cdCompositoVoceTitolLastSep.length() > 0
                    && cdCompositoVoceTitolsb.toString().endsWith(cdCompositoVoceTitolLastSep)) {
                cdCompositoVoceTitolsb = cdCompositoVoceTitolsb.deleteCharAt(cdCompositoVoceTitolsb.length() - 1);
            }

            tmpControlli = this.verificheClassificazioneSIP(cdCompositoVoceTitolsb.toString(), indiceClassifSIP);
            if (!tmpControlli.isrBoolean()) {
                return tmpControlli;
            }
        }
        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaClassificazione(CSChiaveFasc chiave, String parte, ParteNumero prNumero,
            StringBuilder cdCompositoVoceTitolsb, long idStrutt, Date dtApertura) {

        String cdCompositoVoceTitolLastSep = null;
        RispostaControlli tmpControlli = new RispostaControlli();

        if (prNumero.isMatchClassif()) {
            /*
             * se tipo calcolo è PARTE_GENERICO significa che la parte estratta è il codice composito, non si rende
             * necessaria la verifica sul codice della singola voce ma direttamente solo sulla parte
             */
            if (prNumero.getTipoCalcolo().compareTo(TipiCalcolo.PARTE_GENERICO) == 0) {
                // recupero parte
                cdCompositoVoceTitolsb.append(parte);

                // test valido per tipo parte classificazione
                tmpControlli = this.verificaCdCompClassificazione(cdCompositoVoceTitolsb.toString(), prNumero, idStrutt,
                        dtApertura);
                if (!tmpControlli.isrBoolean()) {
                    return tmpControlli;
                }
            } else {

                /*
                 * obiettivo -> ricomporre l'indice recuperando il separtore su singola parte (o voce) per fare la
                 * verifica con il tag <IndiceClassificazione> e cd_composito_voce_titol
                 */

                // recupero per codice voce su db e ricompongo l'intera chiave composta per fare
                // la doppia verifica su tag+db
                // skip if is empty
                if (StringUtils.isBlank(parte)) {
                    // remove last part of composite code ... (separator)
                    if (cdCompositoVoceTitolsb != null && cdCompositoVoceTitolsb.length() > 0) {
                        cdCompositoVoceTitolsb = cdCompositoVoceTitolsb
                                .deleteCharAt(cdCompositoVoceTitolsb.length() - 1);
                    }
                    // continue;// next (potrebbero esserci N livelli di cui una parte opzionale) mi baso quindi
                    // su quello che c'è
                    tmpControlli.setrBoolean(true);// skip next
                    return tmpControlli;
                }
                // da confrontare con i codici compositi
                // parte
                cdCompositoVoceTitolsb.append(parte);

                // test valido per tipo parte classificazione
                tmpControlli = this.recuperoSepVoceClassificazione(parte, cdCompositoVoceTitolsb.toString(), prNumero,
                        idStrutt, dtApertura);
                if (!tmpControlli.isrBoolean()) {
                    return tmpControlli;
                }

                // separatore ottenuto (se esiste)
                if (prNumero.getSeparatoreClassif() != null && prNumero.getSeparatoreClassif().length() > 0) {
                    cdCompositoVoceTitolsb.append(prNumero.getSeparatoreClassif());
                    cdCompositoVoceTitolLastSep = prNumero.getSeparatoreClassif();
                } /*
                   * altrimenti se non esiste non ha trovato il livello sucessivo e certamente andrà in errore ma al
                   * fine di dare un codice composto sensato si utilizza il separatore da configurazione principale
                   */
                else if (prNumero.getSeparatore() != null && prNumero.getSeparatore().length() > 0) {
                    cdCompositoVoceTitolsb.append(prNumero.getSeparatore());
                    cdCompositoVoceTitolLastSep = prNumero.getSeparatore();
                } /*
                   * else { cdCompositoVoceTitolsb.append(" ");//spazio se non presente TODO: da verificare }
                   */
            }
        }

        //
        tmpControlli.setrString(cdCompositoVoceTitolLastSep);
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaCdCompClassificazione(String indiceClassif, ParteNumero prNumero, long idStrutt,
            Date dtApertura) {

        RispostaControlli tmpControlli = new RispostaControlli();

        // se ti_parte è valorizzato con CLASSIF e la regola di normalizzazione è FORMAT_CLASSIF verifica:
        tmpControlli = controlliFascicoli.checkDecVoceTitolWithComp(indiceClassif, idStrutt, dtApertura);
        if (!tmpControlli.isrBoolean()) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_TITOLARIO_NOT_IN_DB, indiceClassif));
            return tmpControlli;
        }
        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;

    }

    private RispostaControlli recuperoSepVoceClassificazione(String parte, String indiceClassif, ParteNumero prNumero,
            long idStrutt, Date dtApertura) {

        RispostaControlli tmpControlli = null;

        // se ti_parte è valorizzato con CLASSIF e la regola di normalizzazione è FORMAT_CLASSIF verifica:
        tmpControlli = controlliFascicoli.checkDecVoceTitolWithCompAndVoce(parte, indiceClassif, idStrutt, dtApertura);
        if (!tmpControlli.isrBoolean()) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_TITOLARIO_NOT_IN_DB, indiceClassif));
            return tmpControlli;
        }

        // se esiste recupero il livello successivo per ottenere il separatore da usare alla prossima iterazione
        DecVoceTitol decVoceTitol = (DecVoceTitol) tmpControlli.getrObject();
        long nextniLivello = decVoceTitol.getDecLivelloTitol().getNiLivello().longValue() + 1;
        tmpControlli = controlliFascicoli.getDecLvlVoceTitolWithNiLivello(nextniLivello,
                decVoceTitol.getDecTitol().getIdTitol());
        if (tmpControlli.isrBoolean()) {
            DecLivelloTitol decLivelloTitol = (DecLivelloTitol) tmpControlli.getrObject();
            prNumero.setSeparatoreClassif(decLivelloTitol.getCdSepLivello());// setto anche il separatore in quanto mi
                                                                             // servirà per il padding!
        }
        // altrimenti ... abbiamo terminato i livelli
        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificheClassificazioneSIP(String cdCompositoVoceTitol,
            String indiceClassificazioneSIP) {

        RispostaControlli tmpControlli = new RispostaControlli();

        //
        // Nota : equals case insensitive
        if (StringUtils.isBlank(indiceClassificazioneSIP)
                || !cdCompositoVoceTitol.trim().equalsIgnoreCase(indiceClassificazioneSIP.trim())) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_TITOLARIO_IN_SIP, cdCompositoVoceTitol));
            return tmpControlli;
        }
        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    /**
     * Produce una chiave di ordinamento generica e "sicura", considerando il numero come se avesse il formato GENERICO
     * e troncando il risultato nel caso superasse i limiti di lunghezza. In ogni caso non rende un errore di
     * validazione della chiave ma, ovviamente, il risultato potrebbe essere impreciso.
     *
     * @param chiave
     *            la chiave del Fascicolo di cui calcolare la chiave ordinamento
     * 
     * @return istanza di RispostaControlli contenente un'istanza di KeyOrdResult
     */
    public RispostaControlli calcolaKeyOrdGenerica(CSChiaveFasc chiave) {
        RispostaControlli tmpControlli = new RispostaControlli();
        //
        String numero = chiave.getNumero();
        Long progressivoCalc = null;
        StringBuilder tmpKeyOrdCalc = new StringBuilder();
        if (!this.verificaLunghezzaNumChiave(numero)) {
            // se il numero è troppo lungo, lo tronco
            numero = numero.substring(0, maxLenNumero);
        }
        tmpKeyOrdCalc.append(StringUtils.leftPad(numero, PAD_NUM_20, "0"));

        // calcolo e verifico la chiave calcolata
        tmpControlli = this.produciChiaveOrd(chiave, tmpKeyOrdCalc);
        // tengo in ogni caso il risultato del calcolo - alla peggio è troncato a MAX_LEN_CHIAVEORD
        KeyOrdResult keyOrdResult = new KeyOrdResult();
        tmpControlli.setrObject(keyOrdResult);
        keyOrdResult.setKeyOrdCalcolata(tmpControlli.getrString());
        keyOrdResult.setProgressivoCalcolato(progressivoCalc);

        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private boolean verificaLunghezzaNumChiave(String stringa) {
        return (stringa.length() <= maxLenNumero);
    }

    private RispostaControlli verificheGeneriche(CSChiaveFasc chiave, String parte, ParteNumero prNumero) {
        RispostaControlli tmpControlli = new RispostaControlli();

        // test sulla lista di valori accettabili,
        if (prNumero.getListaValoriAccettabili() != null) {
            if (!Arrays.asList(prNumero.getListaValoriAccettabili()).contains(parte)) {
                this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_PARTE, prNumero.getNomeParte(),
                        prNumero.getDescValoriAccettabili()));
                return tmpControlli;
            }
        }

        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificheSpecifiche(CSChiaveFasc chiave, StringBuilder sbKeyOrd, String parte,
            ParteNumero prNumero) {
        RispostaControlli tmpControlli = null;

        // TEST specifici per i vari tipi,
        // + costruzione per parti della chiave di ordinamento
        // usando i vari padding
        switch (prNumero.getTipoCalcolo()) {
        case NUMERICO:
            tmpControlli = this.skipBlank(parte);
            if (!tmpControlli.isrBoolean()) {
                tmpControlli = this.verificaTipoNumerico(chiave, sbKeyOrd, parte, prNumero);
            }
            break;
        case NUMERICO_GENERICO:
            tmpControlli = this.skipBlank(parte);
            if (!tmpControlli.isrBoolean()) {
                tmpControlli = this.verificaTipoNumericoGen(chiave, sbKeyOrd, parte, prNumero);
            }
            break;
        case NUMERI_ROMANI:
            tmpControlli = this.skipBlank(parte);
            if (!tmpControlli.isrBoolean()) {
                tmpControlli = this.verificaTipoRomano(chiave, sbKeyOrd, parte, prNumero);
            }
            break;
        case ALFABETICO:
        case ALFANUMERICO:
        case GENERICO:
        case PARTE_GENERICO:
            tmpControlli = this.skipBlank(parte);
            if (!tmpControlli.isrBoolean()) {
                tmpControlli = this.verificaTipoGenerico(sbKeyOrd, parte, prNumero);
            }
            break;
        default:
            //
        }
        return tmpControlli;
    }

    // aggiunto per gestire quelle parti (ni_char = 0..N) in cui potrei non definire valore (vedi caso di vari livelli
    // di classificazione)
    // TODO: da valutare se un empty (e.g. NUMERICO di tipo CLASSIF) debba o meno alimentare con il padding la chiave da
    // ordinare dato che in fase di vericica
    // delle voci titolarie è stato, di fatto, skippato (essendo considerato come elemento opzionale)
    private RispostaControlli skipBlank(String parte) {
        RispostaControlli tmpControlli = null;
        tmpControlli = new RispostaControlli();
        tmpControlli.setrBoolean(StringUtils.isBlank(parte));
        return tmpControlli;
    }

    private RispostaControlli verificaTipoNumerico(CSChiaveFasc chiave, StringBuilder sbKeyOrd, String parte,
            ParteNumero prNumero) {
        RispostaControlli tmpControlli = new RispostaControlli();

        Integer num = null;
        try {
            num = Integer.parseInt(parte);
        } catch (NumberFormatException ex) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_NUMERO_TROPPO_GRANDE, prNumero.getNomeParte()));
            return tmpControlli;
        }
        tmpControlli.setrLong(num);

        // test sul range di valori
        if (prNumero.getMinValAccettabile() != null) {
            if (prNumero.getMinValAccettabile() > num || prNumero.getMaxValAccettabile() < num) {
                this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_MIN_MAX, prNumero.getNomeParte(),
                        prNumero.getDescValoriAccettabili()));
                return tmpControlli;
            }
        }

        // test per vedere se la parte coincide con Anno chiave
        if (prNumero.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_ANNO_PARTE, prNumero.getNomeParte()));
            return tmpControlli;
        }

        /*
         * Su ciascuna sottoparte esegue la normalizzazione: il sistema verifica se la sottoparte è NUMERICO o
         * NUMERICO_GENERICO o NUMERI_ROMANI esegue la normalizzazione riempiendo di 0 a sx fino al numero di caratteri
         * definiti sul parametro ni_char_pad_parte_classif per il periodo di validità del tipo fascicolo versato; se la
         * sottoparte è ALFABETICO o ALFANUMERICO o GENERICO esegue la normalizzazione riempiendo di spazi a dx fino al
         * numero di caratteri definiti sul parametro ni_char_pad_parte_classif per il periodo di validità del tipo
         * fascicolo versato.
         */
        // padding
        Long numPad = (prNumero.getMaxLen() != -1 ? prNumero.getMaxLen() : PAD_NUM_20);
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            numPad = prNumero.getNiPadParteClassif();
        }
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_SPAZIO_DX) {
            sbKeyOrd.append(StringUtils.rightPad(num.toString(), numPad.intValue(), " "));
        } else if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_0_A_SX
                || prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));
        }

        if (prNumero.getSeparatore() != null && prNumero.getSeparatore().length() > 0) {
            sbKeyOrd.append(prNumero.getSeparatore());
        }

        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaTipoNumericoGen(CSChiaveFasc chiave, StringBuilder sbKeyOrd, String parte,
            ParteNumero prNumero) {
        RispostaControlli tmpControlli = new RispostaControlli();

        // verifica se quanto recuperato contiene una sequenza contigua di cifre numeriche.
        // se c'è, la estrae e gestisce il tutto come se fosse una parte numerica "normale"
        Long num = null;
        Matcher m = patternNumericoGen.matcher(parte);
        if (m.find() && m.groupCount() == 1) {
            try {
                num = Long.parseLong(m.group(1));
            } catch (NumberFormatException ex) {
                this.impostaErrore(tmpControlli, String.format(MESS_ERR_NUMERO_TROPPO_GRANDE, prNumero.getNomeParte()));
                return tmpControlli;
            }
            tmpControlli.setrLong(num);
        } else {
            // se non c'è o ce ne sono più di una, segnala l'errore
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_NUMERO_NON_TROVATO, prNumero.getNomeParte()));
            return tmpControlli;
        }

        // test sul range di valori
        if (prNumero.getMinValAccettabile() != null) {
            if (prNumero.getMinValAccettabile() > num || prNumero.getMaxValAccettabile() < num) {
                this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_PARTE, prNumero.getNomeParte(),
                        prNumero.getDescValoriAccettabili()));
                return tmpControlli;
            }
        }

        // test per vedere se la parte coincide con Anno chiave
        if (prNumero.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_ANNO_PARTE, prNumero.getNomeParte()));
            return tmpControlli;
        }

        // padding
        Long numPad = (prNumero.getMaxLen() != -1 ? prNumero.getMaxLen() : PAD_NUM_20);
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            numPad = prNumero.getNiPadParteClassif();
        }
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_SPAZIO_DX) {
            sbKeyOrd.append(StringUtils.rightPad(num.toString(), numPad.intValue(), " "));
        } else if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_0_A_SX
                || prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));
        }

        if (prNumero.getSeparatore() != null && prNumero.getSeparatore().length() > 0) {
            sbKeyOrd.append(prNumero.getSeparatore());
        }

        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaTipoRomano(CSChiaveFasc chiave, StringBuilder sbKeyOrd, String parte,
            ParteNumero prNumero) {
        RispostaControlli tmpControlli = new RispostaControlli();
        RomanConverter tmpConverter = new RomanConverter();
        tmpConverter.setStrict(false);
        Long num = null;

        try {
            // viene trim-ata perché potrebbe avere spazi di padding
            tmpConverter.fromRomanValue(parte.trim());
            num = Long.valueOf(tmpConverter.toInt());
            tmpControlli.setrLong(num);
        } catch (NumberFormatException e) {
            this.impostaErrore(tmpControlli,
                    String.format(MESS_ERR_FORMATO_PARTE, prNumero.getNomeParte(), e.getMessage()));
            return tmpControlli;
        }

        // test per vedere se la parte coincide con Anno chiave
        if (prNumero.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
            this.impostaErrore(tmpControlli, String.format(MESS_ERR_ANNO_PARTE, prNumero.getNomeParte()));
            return tmpControlli;
        }

        // padding, come il numerico, ma il pad standard è di 4 zeri,
        // come nellla vecchia logica di calcolo.
        Long numPad = (prNumero.getMaxLen() != -1 ? prNumero.getMaxLen() : PAD_NUM_4);
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            numPad = prNumero.getNiPadParteClassif();
        }
        sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));

        if (prNumero.getSeparatore() != null && prNumero.getSeparatore().length() > 0) {
            sbKeyOrd.append(prNumero.getSeparatore());
        }

        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private RispostaControlli verificaTipoGenerico(StringBuilder sbKeyOrd, String parte, ParteNumero prNumero) {
        RispostaControlli tmpControlli = new RispostaControlli();
        tmpControlli.setrLong(-1);
        // ovviamente non ci sono numeri da estrarre
        // e neppure controlli da svolgere: la regexp globale ha
        // già filtrato i valori non ammessi

        // padding, valutato sempre come se fosse una stringa
        Long numPad = (prNumero.getMaxLen() != -1 ? prNumero.getMaxLen() : PAD_NUM_20);
        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            numPad = prNumero.getNiPadParteClassif();
        }

        if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_SPAZIO_DX
                || prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.FORMAT_CLASSIF) {
            sbKeyOrd.append(StringUtils.rightPad(parte, numPad.intValue(), " "));
        } else if (prNumero.getTiPadding() == ConfigNumFasc.TipiPadding.RIEMPI_0_A_SX) {
            sbKeyOrd.append(StringUtils.leftPad(parte, numPad.intValue(), "0"));
        } else {
            sbKeyOrd.append(parte);
        }

        if (prNumero.getSeparatore() != null && prNumero.getSeparatore().length() > 0) {
            sbKeyOrd.append(prNumero.getSeparatore());
        }

        //
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    // TODO: da verificare
    public RispostaControlli produciChiaveOrd(CSChiaveFasc chiave, StringBuilder sbKeyOrd) {
        RispostaControlli tmpControlli = new RispostaControlli();
        String tmpChiaveOrd = +chiave.getAnno() + "-" + sbKeyOrd.toString();
        if (tmpChiaveOrd.length() > KeySizeFascUtility.MAX_LEN_CHIAVEORD
                && chiave.getNumero().length() > KeySizeFascUtility.MAX_LEN_NUMERO_IN_CHIAVEORD) {
            tmpChiaveOrd = chiave.getAnno() + "-"
                    + chiave.getNumero().substring(0, KeySizeFascUtility.MAX_LEN_NUMERO_IN_CHIAVEORD) + "-"
                    + sbKeyOrd.toString();
        }
        if (tmpChiaveOrd.length() > KeySizeFascUtility.MAX_LEN_CHIAVEORD) {
            // se il risultato è troppo lungo, lo tronco e rendo errore
            // oltre alla chiave calcolata e troncata che potrebbe servire comunque
            tmpChiaveOrd = tmpChiaveOrd.substring(0, KeySizeFascUtility.MAX_LEN_CHIAVEORD);
            tmpControlli.setrString(tmpChiaveOrd);
            this.impostaErrore(tmpControlli, MESS_ERR_CHIAVECALC_TOOLONG);
            // return tmpControlli;
        }

        tmpControlli.setrString(tmpChiaveOrd);
        tmpControlli.setrBoolean(true);
        return tmpControlli;
    }

    private void impostaErrore(RispostaControlli rc, String causa) {
        rc.setrBoolean(false);
        if (descKeyVersata == null) {
            if (causa.equals(MESS_ERR_666)) {
                rc.setCodErr(MessaggiWSBundle.ERR_666);
                rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666, causa));

            } else {
                rc.setCodErr(MessaggiWSBundle.FASC_005_001);
                rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FASC_005_001, descKeyDaVerificare, causa));
            }
        } /*
           * else TODO: per il momento non esiste un errore su fascicoli collegati versati {
           * rc.setCodErr(MessaggiWSBundle.UD_004_005); rc.setDsErr(
           * MessaggiWSBundle.getString(MessaggiWSBundle.UD_004_005, descKeyVersata, descKeyDaVerificare, causa));
           * 
           * }
           */
    }

    //
    public class KeyOrdResult {

        private String keyOrdCalcolata;
        private Long progressivoCalcolato;

        public String getKeyOrdCalcolata() {
            return keyOrdCalcolata;
        }

        public void setKeyOrdCalcolata(String keyOrdCalcolata) {
            this.keyOrdCalcolata = keyOrdCalcolata;
        }

        public Long getProgressivoCalcolato() {
            return progressivoCalcolato;
        }

        public void setProgressivoCalcolato(Long progressivoCalcolato) {
            this.progressivoCalcolato = progressivoCalcolato;
        }

    }

}
