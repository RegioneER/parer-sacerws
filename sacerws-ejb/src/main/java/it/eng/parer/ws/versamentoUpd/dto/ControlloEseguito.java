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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
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

    private static final long serialVersionUID = 1L;

    // stabilito un "ordine" per famiglia a livello di codice
    // TODO : farlo su DB?!
    public enum FamigliaControllo {
        ERRORISISTEMA(new BigDecimal(1)), CONTROLLIGENERALI(new BigDecimal(2)), /**/
        CONTROLLIXSD(new BigDecimal(3)), CONTROLLIINTESTAZIONE(new BigDecimal(4)), /**/
        CONTROLLIUNITADOC(new BigDecimal(5)), CONTROLLICOLLEGAMENTO(new BigDecimal(6)), /**/
        CONTROLLICHIAVEDOCUMENTO(new BigDecimal(7)),
        CONTROLLICHIAVECOMPONENTE(new BigDecimal(8)), /**/
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
