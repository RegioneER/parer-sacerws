/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.utils;

import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.MessaggiWSFormat;

/**
 *
 * @author sinatti_s
 */
public class KeySizeFascUtility {

    public final static int MAX_LEN_NUMERO_IN_CHIAVEORD = 75;
    //
    public final static int MAX_LEN_CHIAVEORD = 100;
    public final static int MAX_LEN_URN = 254;
    public final static int MAX_LEN_FILENAME_ARK = 254;
    //
    private int maxLenNumero = 0;
    private int lenURN = 0;
    private int lenPath = 0;

    public KeySizeFascUtility(CSVersatore csv, CSChiaveFasc csc, String sistema) {

        int numKeyOrd = this.calcolaMaxLenNumeroKeyOrd(csc);
        int numUrn = this.calcolaMaxLenNumeroURN(csv, csc, sistema);
        maxLenNumero = numKeyOrd < numUrn ? numKeyOrd : numUrn;

        lenURN = this.calcolaURN(csv, csc, sistema).length();

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

    private int calcolaMaxLenNumeroKeyOrd(CSChiaveFasc chiave) {
        String tmpChiaveOrd;
        if (chiave.getNumero().length() <= MAX_LEN_NUMERO_IN_CHIAVEORD) {
            tmpChiaveOrd = +chiave.getAnno() + "-" + chiave.getNumero();
        } else {
            /*
             * prova a rifare il controllo troncando il <codice fascicolo> a 75 caratteri. codice fascicolo???
             */
            tmpChiaveOrd = chiave.getAnno() + "-" + chiave.getNumero().substring(0, MAX_LEN_NUMERO_IN_CHIAVEORD);
        }

        return MAX_LEN_CHIAVEORD - tmpChiaveOrd.length();
    }

    private int calcolaMaxLenNumeroURN(CSVersatore versatore, CSChiaveFasc chiave, String sistema) {
        return MAX_LEN_URN - this.calcolaURN(versatore, chiave, sistema).length();
    }

    private String calcolaURN(CSVersatore versatore, CSChiaveFasc chiave, String sistema) {
        String chiaveComp = MessaggiWSFormat.formattaChiaveFascicolo(versatore, chiave, sistema);
        String chiaveSIP = MessaggiWSFormat.formattaUrnIndiceSipFasc(chiaveComp,
                Costanti.UrnFormatter.URN_INDICE_SIP_V2);
        return chiaveSIP.toString();
    }

    // TODO: serve????
    /*
     * private int calcolaMaxLenNumeroPathTivoli(CSVersatore versatore, CSChiave chiave) { return MAX_LEN_FILENAME_ARK -
     * this.calcolaPathTivoli(versatore, chiave, tc).length(); }
     * 
     * 
     * private String calcolaPathTivoli(CSVersatore versatore, CSChiave chiave, TpiConfig tc) { String spData =
     * MessaggiWSFormat.formattaSubPathData(new Date()); // la data vera non conta; una volta formattate sono tutte
     * grandi uguali String spVersatore = MessaggiWSFormat.formattaSubPathVersatoreArk(versatore); String spUnitaDoc =
     * MessaggiWSFormat.formattaSubPathUnitaDocArk(chiave); String spNomeFile = MessaggiWSFormat.formattaNomeFileArk(
     * Costanti.CategoriaDocumento.Annotazione, 99, 1, 99); String tmpString = MessaggiWSFormat.formattaFilePathArk(
     * tc.getTpiRootTpi(), tc.getTpiRootArkVers(), spData, spVersatore, spUnitaDoc, spNomeFile);
     * 
     * return ""; }
     */

}
