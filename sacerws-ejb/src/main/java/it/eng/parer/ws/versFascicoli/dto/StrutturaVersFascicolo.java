/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.dto;

import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.IDatiSpecEntity;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author fioravanti_f
 */
public class StrutturaVersFascicolo implements java.io.Serializable, IDatiSpecEntity {

    private Date dataVersamento;

    private long idStruttura;
    private long idUser;
    private long idTipoFascicolo;
    private long idAATipoFasc;
    private long idIamAbilTipoDato;
    private long idOrgEnteConv;

    private boolean versatoreVerificato = false;

    private String versioneIndiceSipNonVerificata = "N/A";
    private String tipoFascicoloNonverificato;
    private CSVersatore versatoreNonverificato;
    private CSChiaveFasc chiaveNonVerificata;

    private String urnPartChiaveFascicolo;
    private String urnPartChiaveFascicoloNormalized;

    private List<Long> unitaDocElencate;

    private long idRecXsdProfiloArchivistico;
    private long idRecxsdProfiloGenerale;
    private DatiXmlProfiloGenerale datiXmlProfiloGenerale;
    private DatiXmlProfiloArchivistico datiXmlProfiloArchivistico;

    private long idRecXsdDatiSpec;
    private long idRecXsdDatiSpecMigrazione;

    private HashMap<String, DatoSpecifico> datiSpecifici;
    private HashMap<String, DatoSpecifico> datiSpecificiMigrazione;

    private FlControlliFasc flControlliFasc;
    private ConfigNumFasc configNumFasc;
    private String keyOrdCalcolata;
    private Long progressivoCalcolato;
    private boolean warningFormatoNumero;

    private List<FascicoloLink> fascicoliLinked;

    private Long idVoceTitol;

    public Date getDataVersamento() {
        return dataVersamento;
    }

    public void setDataVersamento(Date dataVersamento) {
        this.dataVersamento = dataVersamento;
    }

    public long getIdStruttura() {
        return idStruttura;
    }

    public void setIdStruttura(long idStruttura) {
        this.idStruttura = idStruttura;
    }

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public long getIdTipoFascicolo() {
        return idTipoFascicolo;
    }

    public void setIdTipoFascicolo(long idTipoFascicolo) {
        this.idTipoFascicolo = idTipoFascicolo;
    }

    public long getIdAATipoFasc() {
        return idAATipoFasc;
    }

    public void setIdAATipoFasc(long idAATipoFasc) {
        this.idAATipoFasc = idAATipoFasc;
    }

    public long getIdIamAbilTipoDato() {
        return idIamAbilTipoDato;
    }

    public void setIdIamAbilTipoDato(long idIamAbilTipoDato) {
        this.idIamAbilTipoDato = idIamAbilTipoDato;
    }

    public boolean isVersatoreVerificato() {
        return versatoreVerificato;
    }

    public void setVersatoreVerificato(boolean versatoreVerificato) {
        this.versatoreVerificato = versatoreVerificato;
    }

    public String getVersioneIndiceSipNonVerificata() {
        return versioneIndiceSipNonVerificata;
    }

    public void setVersioneIndiceSipNonVerificata(String versioneIndiceSipNonVerificata) {
        this.versioneIndiceSipNonVerificata = versioneIndiceSipNonVerificata;
    }

    public String getTipoFascicoloNonverificato() {
        return tipoFascicoloNonverificato;
    }

    public void setTipoFascicoloNonverificato(String tipoFascicoloNonverificato) {
        this.tipoFascicoloNonverificato = tipoFascicoloNonverificato;
    }

    public CSVersatore getVersatoreNonverificato() {
        return versatoreNonverificato;
    }

    public void setVersatoreNonverificato(CSVersatore versatoreNonverificato) {
        this.versatoreNonverificato = versatoreNonverificato;
    }

    public CSChiaveFasc getChiaveNonVerificata() {
        return chiaveNonVerificata;
    }

    public void setChiaveNonVerificata(CSChiaveFasc chiaveNonVerificata) {
        this.chiaveNonVerificata = chiaveNonVerificata;
    }

    public String getUrnPartChiaveFascicolo() {
        return urnPartChiaveFascicolo;
    }

    public void setUrnPartChiaveFascicolo(String urnPartChiaveFascicolo) {
        this.urnPartChiaveFascicolo = urnPartChiaveFascicolo;
    }

    public List<Long> getUnitaDocElencate() {
        return unitaDocElencate;
    }

    public void setUnitaDocElencate(List<Long> unitaDocElencate) {
        this.unitaDocElencate = unitaDocElencate;
    }

    public long getIdRecXsdProfiloArchivistico() {
        return idRecXsdProfiloArchivistico;
    }

    public void setIdRecXsdProfiloArchivistico(long idRecXsdProfiloArchivistico) {
        this.idRecXsdProfiloArchivistico = idRecXsdProfiloArchivistico;
    }

    public long getIdRecxsdProfiloGenerale() {
        return idRecxsdProfiloGenerale;
    }

    public void setIdRecxsdProfiloGenerale(long idRecxsdProfiloGenerale) {
        this.idRecxsdProfiloGenerale = idRecxsdProfiloGenerale;
    }

    public DatiXmlProfiloGenerale getDatiXmlProfiloGenerale() {
        return datiXmlProfiloGenerale;
    }

    public void setDatiXmlProfiloGenerale(DatiXmlProfiloGenerale datiXmlProfiloGenerale) {
        this.datiXmlProfiloGenerale = datiXmlProfiloGenerale;
    }

    public DatiXmlProfiloArchivistico getDatiXmlProfiloArchivistico() {
        return datiXmlProfiloArchivistico;
    }

    public void setDatiXmlProfiloArchivistico(DatiXmlProfiloArchivistico datiXmlProfiloArchivistico) {
        this.datiXmlProfiloArchivistico = datiXmlProfiloArchivistico;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecifici() {

        return datiSpecifici;
    }

    @Override
    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecificiMigrazione() {

        return datiSpecificiMigrazione;
    }

    @Override
    public void setDatiSpecificiMigrazione(HashMap<String, DatoSpecifico> datiSpecificiMigrazione) {
        this.datiSpecificiMigrazione = datiSpecificiMigrazione;
    }

    @Override
    public long getIdRecXsdDatiSpec() {

        return idRecXsdDatiSpec;
    }

    @Override
    public long getIdRecXsdDatiSpecMigrazione() {

        return idRecXsdDatiSpecMigrazione;
    }

    @Override
    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    @Override
    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpecMigrazione = idRecXsdDatiSpec;
    }

    public FlControlliFasc getFlControlliFasc() {
        return flControlliFasc;
    }

    public void setFlControlliFasc(FlControlliFasc flControlliFasc) {
        this.flControlliFasc = flControlliFasc;
    }

    public ConfigNumFasc getConfigNumFasc() {
        return configNumFasc;
    }

    public void setConfigNumFasc(ConfigNumFasc configNumFasc) {
        this.configNumFasc = configNumFasc;
    }

    public Long getProgressivoCalcolato() {
        return progressivoCalcolato;
    }

    public void setProgressivoCalcolato(Long progressivoCalcolato) {
        this.progressivoCalcolato = progressivoCalcolato;
    }

    public boolean isWarningFormatoNumero() {
        return warningFormatoNumero;
    }

    public void setWarningFormatoNumero(boolean warningFormatoNumero) {
        this.warningFormatoNumero = warningFormatoNumero;
    }

    public String getKeyOrdCalcolata() {
        return keyOrdCalcolata;
    }

    public void setKeyOrdCalcolata(String keyOrdCalcolata) {
        this.keyOrdCalcolata = keyOrdCalcolata;
    }

    public List<FascicoloLink> getFascicoliLinked() {
        return fascicoliLinked;
    }

    public void setFascicoliLinked(List<FascicoloLink> fascicoliLinked) {
        this.fascicoliLinked = fascicoliLinked;
    }

    public long getIdOrgEnteConv() {
        return idOrgEnteConv;
    }

    public void setIdOrgEnteConv(long idOrgEnteConv) {
        this.idOrgEnteConv = idOrgEnteConv;
    }

    public Long getIdVoceTitol() {
        return idVoceTitol;
    }

    public void setIdVoceTitol(Long idVoceTitol) {
        this.idVoceTitol = idVoceTitol;
    }

    public String getUrnPartChiaveFascicoloNormalized() {
        return urnPartChiaveFascicoloNormalized;
    }

    public void setUrnPartChiaveFascicoloNormalized(String urnPartChiaveFascicoloNormalized) {
        this.urnPartChiaveFascicoloNormalized = urnPartChiaveFascicoloNormalized;
    }

}
