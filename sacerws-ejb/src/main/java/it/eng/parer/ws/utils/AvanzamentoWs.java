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
 */
package it.eng.parer.ws.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
public class AvanzamentoWs implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AvanzamentoWs.class);

    public enum Funzioni {

        AnnullamentoUD, VersamentoSync, VersamentoFascicolo, AggiuntaDocumentiSync, Recupero, // deve essere tolto!!!
        RichiestaStato, Restituzione, VersamentoAsyncInit, VersamentoAsyncFile, VersamentoAsyncChiusura, RecuperoWeb,
        AggiornamentoVersamento
    }

    public enum CheckPoints {

        Inizio, TrasferimentoPayloadIn, VerificaStrutturaChiamataWs, VerificaSemantica, VerificaCoerenzaComponentiFile,
        VerificaFirmeComponenti, VerificaFormatoFileComponenti, CopiaBackendStaging, SalvataggioDatiVersati,
        SalvataggioSessioneWS, InvioMessaggioCodaJMS, CreazioneRisposta, InvioRisposta, Pulizia, Fine
    }

    //
    private String instanceName;
    private Funzioni funzione;
    //
    private CheckPoints checkPoint;
    private String fase;
    //
    private String chNumero;
    private String chAnno;
    private String chRegistro;
    //
    private String vrsAmbiente;
    private String vrsEnte;
    private String vrsStruttura;
    private String vrsUser;
    //
    private String documento;
    private String componente;

    // costruttore privato, la classe non è direttamente istanziabile
    private AvanzamentoWs() {
        this.reset();
    }

    // factory
    public static AvanzamentoWs nuovoAvanzamentoWS(String instance, Funzioni funz) {
        AvanzamentoWs tmpAvanzamentoWS = null;
        /*
         * Vedi https://redmine.ente.regione.emr.it/issues/21627
         */
        tmpAvanzamentoWS = new AvanzamentoWs();
        tmpAvanzamentoWS.instanceName = instance;
        tmpAvanzamentoWS.funzione = funz;

        return tmpAvanzamentoWS;
    }

    public AvanzamentoWs logAvanzamento() {

        StringBuilder tmpBuilder = new StringBuilder();

        if (log.isInfoEnabled()) {
            tmpBuilder.append(String.format("I: %s ; ", this.getInstanceName()));
            tmpBuilder.append(String.format("F: %s ; ", this.getFunzione().toString()));
            tmpBuilder.append(String.format("CP: %s ; ", this.getCheckPoint().toString()));

            if (!this.getFase().isEmpty()) {
                tmpBuilder.append(String.format("F: %s ; ", this.getFase()));
            }

            if (!this.getVrsAmbiente().isEmpty()) {
                tmpBuilder.append(String.format("Amb: %s ; ", this.getVrsAmbiente()));
                tmpBuilder.append(String.format("Ente: %s ; ", this.getVrsEnte()));
                tmpBuilder.append(String.format("Strutt: %s ; ", this.getVrsStruttura()));
            }

            if (!this.getVrsUser().isEmpty()) {
                tmpBuilder.append(String.format("U: %s ; ", this.getVrsUser()));
            }

            if (!this.getChAnno().isEmpty()) {
                tmpBuilder.append(String.format("Anno: %s ; ", this.getChAnno()));
                tmpBuilder.append(String.format("Num: %s ; ", this.getChNumero()));
                tmpBuilder.append(String.format("Reg: %s ; ", this.getChRegistro()));
            }

            if (!this.getDocumento().isEmpty()) {
                tmpBuilder.append(String.format("Doc: %s ; ", this.getDocumento()));
            }

            if (!this.getComponente().isEmpty()) {
                tmpBuilder.append(String.format("Comp: %s ; ", this.getComponente()));
            }

            log.info(tmpBuilder.toString());
        }

        return this;
    }

    public AvanzamentoWs reset() {
        checkPoint = CheckPoints.Inizio;
        chNumero = "";
        chAnno = "";
        chRegistro = "";
        vrsAmbiente = "";
        vrsEnte = "";
        vrsStruttura = "";
        vrsUser = "";
        this.resetFase();
        return this;
    }

    public AvanzamentoWs resetFase() {
        fase = "";
        documento = "";
        componente = "";
        return this;
    }

    /*
     * getter e setter (i setter sono coerenti con il modello Fluent di Martin Fowler
     * http://en.wikipedia.org/wiki/Fluent_interface http://en.wikipedia.org/wiki/Method_chaining
     *
     * )
     */
    public String getInstanceName() {
        return instanceName;
    }

    public AvanzamentoWs setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public Funzioni getFunzione() {
        return funzione;
    }

    public AvanzamentoWs setFunzione(Funzioni funzione) {
        this.funzione = funzione;
        return this;
    }

    public CheckPoints getCheckPoint() {
        return checkPoint;
    }

    public AvanzamentoWs setCheckPoint(CheckPoints checkPoint) {
        this.checkPoint = checkPoint;
        return this;
    }

    public String getFase() {
        return fase;
    }

    public AvanzamentoWs setFase(String fase) {
        this.fase = fase;
        return this;
    }

    public String getChNumero() {
        return chNumero;
    }

    public AvanzamentoWs setChNumero(String chNumero) {
        this.chNumero = chNumero;
        return this;
    }

    public String getChAnno() {
        return chAnno;
    }

    public AvanzamentoWs setChAnno(String chAnno) {
        this.chAnno = chAnno;
        return this;
    }

    public String getChRegistro() {
        return chRegistro;
    }

    public AvanzamentoWs setChRegistro(String chRegistro) {
        this.chRegistro = chRegistro;
        return this;
    }

    public String getVrsAmbiente() {
        return vrsAmbiente;
    }

    public AvanzamentoWs setVrsAmbiente(String vrsAmbiente) {
        this.vrsAmbiente = vrsAmbiente;
        return this;
    }

    public String getVrsEnte() {
        return vrsEnte;
    }

    public AvanzamentoWs setVrsEnte(String vrsEnte) {
        this.vrsEnte = vrsEnte;
        return this;
    }

    public String getVrsStruttura() {
        return vrsStruttura;
    }

    public AvanzamentoWs setVrsStruttura(String vrsStruttura) {
        this.vrsStruttura = vrsStruttura;
        return this;
    }

    public String getVrsUser() {
        return vrsUser;
    }

    public AvanzamentoWs setVrsUser(String vrsUser) {
        this.vrsUser = vrsUser;
        return this;
    }

    public String getDocumento() {
        return documento;
    }

    public AvanzamentoWs setDocumento(String documento) {
        this.documento = documento;
        return this;
    }

    public String getComponente() {
        return componente;
    }

    public AvanzamentoWs setComponente(String componente) {
        this.componente = componente;
        return this;
    }
}
