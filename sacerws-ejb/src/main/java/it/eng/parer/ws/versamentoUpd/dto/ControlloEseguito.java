/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.ws.versamento.dto.VoceDiErrore;

/**
 *
 * @author sinatti_s
 */
public class ControlloEseguito implements java.io.Serializable {

    // stabilito un "ordine" per famiglia a livello di codice
    // TODO : farlo su DB?!
    public enum FamigliaControllo {
        ERRORISISTEMA(new BigDecimal(1)), CONTROLLIGENERALI(new BigDecimal(2)), /**/
        CONTROLLIXSD(new BigDecimal(3)), CONTROLLIINTESTAZIONE(new BigDecimal(4)), /**/
        CONTROLLIUNITADOC(new BigDecimal(5)), CONTROLLICOLLEGAMENTO(new BigDecimal(6)), /**/
        CONTROLLICHIAVEDOCUMENTO(new BigDecimal(7)), CONTROLLICHIAVECOMPONENTE(new BigDecimal(8)), /**/
        CONTROLLIDOCUMENTO(new BigDecimal(9)), CONTROLLICOMPONENTE(new BigDecimal(10));

        private BigDecimal niOrd;

        private FamigliaControllo(BigDecimal niOrd) {
            this.niOrd = niOrd;
        }

        public BigDecimal getNiOrd() {
            return niOrd;
        }

    }

    //
    private String cdControllo;
    //
    private FamigliaControllo famiglia;
    private VoceDiErrore.TipiEsitoErrore esito;
    private BigDecimal niOrd; // numero ordine
    private List<VoceDiErrore> errori = new ArrayList<VoceDiErrore>(0);
    //
    private String dsControllo;
    //

    public String getDsControllo() {
        return dsControllo;
    }

    public void setDsControllo(String dsControllo) {
        this.dsControllo = dsControllo;
    }

    public VoceDiErrore.TipiEsitoErrore getEsito() {
        return esito;
    }

    public void setEsito(VoceDiErrore.TipiEsitoErrore esito) {
        this.esito = esito;
    }

    public List<VoceDiErrore> getErrori() {
        return errori;
    }

    public void setErrori(List<VoceDiErrore> errori) {
        this.errori = errori;
    }

    public BigDecimal getNiOrd() {
        return niOrd;
    }

    public void setNiOrd(BigDecimal niOrd) {
        this.niOrd = niOrd;
    }

    public FamigliaControllo getFamiglia() {
        return famiglia;
    }

    public void setFamiglia(FamigliaControllo famiglia) {
        this.famiglia = famiglia;
    }

    public String getCdControllo() {
        return cdControllo;
    }

    public void setCdControllo(String cdControllo) {
        this.cdControllo = cdControllo;
    }

}
