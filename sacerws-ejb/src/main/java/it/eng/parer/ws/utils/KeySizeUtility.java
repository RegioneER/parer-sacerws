/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;

import java.util.Date;

/**
 *
 * @author fioravanti_f
 */
public class KeySizeUtility {

    public final static class TpiConfig {

        private String tpiRootTpi;
        private String tpiRootArkVers;

        public TpiConfig(String tpiRootTpi, String tpiRootArkVers) {
            this.tpiRootTpi = tpiRootTpi;
            this.tpiRootArkVers = tpiRootArkVers;
        }

        public String getTpiRootTpi() {
            return tpiRootTpi;
        }

        public void setTpiRootTpi(String tpiRootTpi) {
            this.tpiRootTpi = tpiRootTpi;
        }

        public String getTpiRootArkVers() {
            return tpiRootArkVers;
        }

        public void setTpiRootArkVers(String tpiRootArkVers) {
            this.tpiRootArkVers = tpiRootArkVers;
        }

    }

    public final static int MAX_LEN_REGISTRO_IN_CHIAVEORD = 75;
    //
    public final static int MAX_LEN_CHIAVEORD = 100;
    public final static int MAX_LEN_URN = 254;
    public final static int MAX_LEN_FILENAME_ARK = 254;
    //
    private int maxLenNumero = 0;
    private int lenURN = 0;
    private int lenPath = 0;

    public KeySizeUtility(CSVersatore csv, CSChiave csc, TpiConfig tc) {
        CSChiave tmpChiaveSenzaNumero = new CSChiave();
        tmpChiaveSenzaNumero.setTipoRegistro(csc.getTipoRegistro());
        tmpChiaveSenzaNumero.setAnno(csc.getAnno());
        tmpChiaveSenzaNumero.setNumero("");

        int numKeyOrd = this.calcolaMaxLenNumeroKeyOrd(tmpChiaveSenzaNumero);
        int numUrn = this.calcolaMaxLenNumeroURN(csv, tmpChiaveSenzaNumero);
        maxLenNumero = numKeyOrd < numUrn ? numKeyOrd : numUrn;

        lenURN = this.calcolaURN(csv, csc).length();

        if (tc != null) {
            int numPath = this.calcolaMaxLenNumeroPathTivoli(csv, tmpChiaveSenzaNumero, tc);
            maxLenNumero = maxLenNumero < numPath ? maxLenNumero : numPath;
            lenPath = this.calcolaPathTivoli(csv, csc, tc).length();
        }
    }

    public int getMaxLenNumero() {
        return maxLenNumero;
    }

    public int getLenURN() {
        return lenURN;
    }

    public int getLenPath() {
        return lenPath;
    }

    private int calcolaMaxLenNumeroKeyOrd(CSChiave chiave) {
        String tmpChiaveOrd;
        if (chiave.getTipoRegistro().length() <= MAX_LEN_REGISTRO_IN_CHIAVEORD) {
            tmpChiaveOrd = chiave.getTipoRegistro() + "-" + chiave.getAnno() + "-";
        } else {
            tmpChiaveOrd = chiave.getTipoRegistro().substring(0, MAX_LEN_REGISTRO_IN_CHIAVEORD) + "-" + chiave.getAnno()
                    + "-";
        }

        return MAX_LEN_CHIAVEORD - tmpChiaveOrd.length();
    }

    private int calcolaMaxLenNumeroURN(CSVersatore versatore, CSChiave chiave) {
        return MAX_LEN_URN - this.calcolaURN(versatore, chiave).length();
    }

    private String calcolaURN(CSVersatore versatore, CSChiave chiave) {
        // new URN
        String tmpUrnPartVersatore = MessaggiWSFormat.formattaUrnPartVersatore(versatore);
        String descChiaveUD = MessaggiWSFormat.formattaUrnPartUnitaDoc(chiave);
        /*
         * String chiaveDoc = MessaggiWSFormat.formattaChiaveDocumento( descChiaveUD,
         * Costanti.CategoriaDocumento.Annotazione, 99);
         */
        String chiaveDoc = MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD, CategoriaDocumento.Documento, 99,
                true, Costanti.UrnFormatter.DOC_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT);
        /*
         * String chiaveComp = MessaggiWSFormat.formattaChiaveComponente( chiaveDoc, 1, 99);
         */
        String chiaveComp = MessaggiWSFormat.formattaChiaveComponente(chiaveDoc, 99,
                Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT);
        StringBuilder tmpString = new StringBuilder();
        tmpString.append("urn:");
        tmpString.append(tmpUrnPartVersatore);
        tmpString.append(":");
        tmpString.append(chiaveComp);

        return tmpString.toString();
    }

    private int calcolaMaxLenNumeroPathTivoli(CSVersatore versatore, CSChiave chiave, TpiConfig tc) {
        return MAX_LEN_FILENAME_ARK - this.calcolaPathTivoli(versatore, chiave, tc).length();
    }

    private String calcolaPathTivoli(CSVersatore versatore, CSChiave chiave, TpiConfig tc) {
        String spData = MessaggiWSFormat.formattaSubPathData(new Date());
        // la data vera non conta; una volta formattate sono tutte grandi uguali
        String spVersatore = MessaggiWSFormat.formattaSubPathVersatoreArk(versatore);
        String spUnitaDoc = MessaggiWSFormat.formattaSubPathUnitaDocArk(chiave);
        String spNomeFile = MessaggiWSFormat.formattaNomeFileArk(this.calcolaURN(versatore, chiave));
        String tmpString = MessaggiWSFormat.formattaFilePathArk(tc.getTpiRootTpi(), tc.getTpiRootArkVers(), spData,
                spVersatore, spUnitaDoc, spNomeFile);

        return tmpString.toString();
    }

}
