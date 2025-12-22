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
package it.eng.parer.ws.versamento.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fioravanti_F
 */
public class SyncFakeSessn implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public enum TipiSessioneVersamento {

        AGGIUNGI_DOCUMENTO, VERSAMENTO
    }

    private boolean salvaSessione = true;
    private TipiSessioneVersamento tipoSessioneVers;
    private String tipoDatiSessioneVers;
    private String ipChiamante;
    private String loginName;
    private String password;
    private long idUser = 0;
    private String versioneWS;
    //
    private String datiIndiceSipXml;
    private String urnIndiceSipXml;
    private String hashIndiceSipXml;
    private String datiDaSalvareIndiceSip;
    private String datiC14NIndiceSip;
    //
    private String datiPackInfoSipXml;
    private String urnPackInfoSipXml;
    private String hashPackInfoSipXml;
    private String datiC14NPackInfoSipXml;
    //
    private String urnEsitoVersamento;
    //
    private String datiRapportoVersamento;
    private String urnRapportoVersamento;
    private String hashRapportoVersamento;
    //
    private boolean xmlOk;
    private ZonedDateTime tmApertura;
    private ZonedDateTime tmChiusura;
    private List<FileBinario> fileBinari;

    /**
     * Costruttore
     */
    public SyncFakeSessn() {
        fileBinari = new ArrayList<FileBinario>();
        xmlOk = false;
    }

    /*
     *
     */
    /**
     * @return the salvaSessione
     */
    public boolean isSalvaSessione() {
        return salvaSessione;
    }

    /**
     * @param salvaSessione the salvaSessione to set
     */
    public void setSalvaSessione(boolean salvaSessione) {
        this.salvaSessione = salvaSessione;
    }

    /**
     * @return the tipoSessioneVers
     */
    public TipiSessioneVersamento getTipoSessioneVers() {
        return tipoSessioneVers;
    }

    /**
     * @param tipoSessioneVers the tipoSessioneVers to set
     */
    public void setTipoSessioneVers(TipiSessioneVersamento tipoSessioneVers) {
        this.tipoSessioneVers = tipoSessioneVers;
    }

    /**
     * @return the tipoDatiSessioneVers
     */
    public String getTipoDatiSessioneVers() {
        return tipoDatiSessioneVers;
    }

    /**
     * @param tipoDatiSessioneVers the tipoDatiSessioneVers to set
     */
    public void setTipoDatiSessioneVers(String tipoDatiSessioneVers) {
        this.tipoDatiSessioneVers = tipoDatiSessioneVers;
    }

    public String getIpChiamante() {
        return ipChiamante;
    }

    public void setIpChiamante(String ipChiamante) {
        this.ipChiamante = ipChiamante;
    }

    /**
     * @return the loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param loginName the loginName to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName.replaceAll("[\\u0000-\\u001F]", "");
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password.replaceAll("[\\u0000-\\u001F]", "");
    }

    /**
     * @return the idUser
     */
    public long getIdUser() {
        return idUser;
    }

    /**
     * @param idUser the idUser to set
     */
    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    /**
     * @return the versioneWS
     */
    public String getVersioneWS() {
        return versioneWS;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param versioneWS the versioneWS to set
     */
    public void setVersioneWS(String versioneWS) {
        this.versioneWS = versioneWS.replaceAll("[\\u0000-\\u001F]", "");
    }

    /**
     * @return the datiIndiceSipXml
     */
    public String getDatiIndiceSipXml() {
        return datiIndiceSipXml;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param datiIndiceSipXml the datiIndiceSipXml to set
     */
    public void setDatiIndiceSipXml(String datiIndiceSipXml) {
        this.datiIndiceSipXml = datiIndiceSipXml.replaceAll("[\\u0000-\\u001F]", "");
    }

    public String getUrnIndiceSipXml() {
        return urnIndiceSipXml;
    }

    public void setUrnIndiceSipXml(String urnIndiceSipXml) {
        this.urnIndiceSipXml = urnIndiceSipXml;
    }

    public String getHashIndiceSipXml() {
        return hashIndiceSipXml;
    }

    public void setHashIndiceSipXml(String hashIndiceSipXml) {
        this.hashIndiceSipXml = hashIndiceSipXml;
    }

    public String getDatiDaSalvareIndiceSip() {
        return datiDaSalvareIndiceSip;
    }

    public void setDatiDaSalvareIndiceSip(String datiDaSalvareIndiceSip) {
        this.datiDaSalvareIndiceSip = datiDaSalvareIndiceSip;
    }

    public String getDatiC14NIndiceSip() {
        return datiC14NIndiceSip;
    }

    public void setDatiC14NIndiceSip(String datiC14NIndiceSip) {
        this.datiC14NIndiceSip = datiC14NIndiceSip;
    }

    public String getDatiPackInfoSipXml() {
        return datiPackInfoSipXml;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param datiPackInfoSipXml the datiIndiceSipXml to set
     */
    public void setDatiPackInfoSipXml(String datiPackInfoSipXml) {
        this.datiPackInfoSipXml = datiPackInfoSipXml.replaceAll("[\\u0000-\\u001F]", "");
    }

    public String getUrnPackInfoSipXml() {
        return urnPackInfoSipXml;
    }

    public void setUrnPackInfoSipXml(String urnPackInfoSipXml) {
        this.urnPackInfoSipXml = urnPackInfoSipXml;
    }

    public String getHashPackInfoSipXml() {
        return hashPackInfoSipXml;
    }

    public void setHashPackInfoSipXml(String hashPackInfoSipXml) {
        this.hashPackInfoSipXml = hashPackInfoSipXml;
    }

    public String getUrnEsitoVersamento() {
        return urnEsitoVersamento;
    }

    public void setUrnEsitoVersamento(String urnEsitoVersamento) {
        this.urnEsitoVersamento = urnEsitoVersamento;
    }

    public String getDatiRapportoVersamento() {
        return datiRapportoVersamento;
    }

    public void setDatiRapportoVersamento(String datiRapportoVersamento) {
        this.datiRapportoVersamento = datiRapportoVersamento;
    }

    public String getUrnRapportoVersamento() {
        return urnRapportoVersamento;
    }

    public void setUrnRapportoVersamento(String urnRapportoVersamento) {
        this.urnRapportoVersamento = urnRapportoVersamento;
    }

    public String getHashRapportoVersamento() {
        return hashRapportoVersamento;
    }

    public void setHashRapportoVersamento(String hashRapportoVersamento) {
        this.hashRapportoVersamento = hashRapportoVersamento;
    }

    /**
     * @return the xmlOk
     */
    public boolean isXmlOk() {
        return xmlOk;
    }

    /**
     * @param xmlOk the xmlOk to set
     */
    public void setXmlOk(boolean xmlOk) {
        this.xmlOk = xmlOk;
    }

    /**
     * @return the tmpApertura
     */
    public ZonedDateTime getTmApertura() {
        return tmApertura;
    }

    /**
     * @param tmApertura the tmpApertura to set
     */
    public void setTmApertura(ZonedDateTime tmApertura) {
        this.tmApertura = tmApertura;
    }

    public ZonedDateTime getTmChiusura() {
        return tmChiusura;
    }

    public void setTmChiusura(ZonedDateTime tmChiusura) {
        this.tmChiusura = tmChiusura;
    }

    /**
     * @return the fileBinari
     */
    public List<FileBinario> getFileBinari() {
        return fileBinari;
    }

    public String getDatiC14NPackInfoSipXml() {
        return datiC14NPackInfoSipXml;
    }

    public void setDatiC14NPackInfoSipXml(String datiC14NPackInfoSipXml) {
        this.datiC14NPackInfoSipXml = datiC14NPackInfoSipXml;
    }
}
