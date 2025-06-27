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

package it.eng.parer.ws.versamento.dto;

import java.util.Date;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import it.eng.parer.firma.dto.CompDocMock;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipoAlgoritmoRappr;
import it.eng.parer.ws.versamentoMM.dto.ComponenteMM;

/**
 *
 * @author Fioravanti_F
 */
public class ComponenteVers implements java.io.Serializable, IDatiSpecEntity, IProfiloEntity {

    /**
     *
     */
    private static final long serialVersionUID = -3775154355306395315L;

    // tag <id>
    private String id;
    private String chiaveComp;
    // nome del file da salvare, nel caso si debba usare la memorizzazione basata su
    // file system
    private String nomeFileArk;
    private String descFormatoFileVers;
    //
    private String flRifTempDataFirmaVers;

    //
    private boolean datiLetti;
    private boolean presenteRifMeta;
    private boolean nonAccettareForzaFormato;
    private boolean formatoFileVersNonAmmesso;
    // flg
    private boolean hashForzato = false;
    private boolean hashVersNonDefinito = false;

    // costanti
    private CostantiDB.TipoAlgoritmoRappr algoritmoRappr;
    private CostantiDB.TipiHash hashVersatoAlgoritmo;
    private CostantiDB.TipiEncBinari hashVersatoEncoding;

    private long IdTipoComponente;
    private long idTipoRappresentazioneComponente;
    // se idTipoRappresentazioneComponente è valorizzato,
    // tengo anche i formati di file attesi per contenuto e convertitore
    private long idFormatoFileDocCont;
    private long idFormatoFileDocConv;
    //
    private String tipoUso;
    // Proprietà necessarie per controllo su firme e marche
    private Date tmRifTempVers;

    // riferimeno all'UD che contiene eventualmente il foglio-stile o xslt
    private long idUnitaDocRif;
    //
    private long idFormatoFileVers;
    // id salvataggio di se stesso su db
    private long idRecDB;
    // ordine di presentazione del componente nel documento
    private long ordinePresentazione;
    // riferimento al record dell'XSD dei dati specifici e di migrazione
    private long idRecXsdDatiSpec;
    private long idRecXsdDatiSpecMigrazione;
    //
    private byte[] hashCalcolato;
    //
    private byte[] hashVersato;

    //
    // riferimento alle classi di xml di risposta utilizzate per salvare le
    // informazioni di dimensione file
    // NOTA BENE: solo uno tra componente e sottocomponente è valorizzato
    private transient it.eng.parer.ws.xml.versResp.ECComponenteType rifComponenteResp;
    private transient it.eng.parer.ws.xml.versResp.ECSottoComponenteType rifSottoComponenteResp;

    // riferimento all'entity del componente
    // private AroCompDoc acdEntity;
    private transient CompDocMock acdEntity;
    // MEV#18660
    private transient VerificaFirmaWrapper vfWrapper;

    // dati specifici del componente o del sottocomnponente
    private Map<String, DatoSpecifico> datiSpecifici;
    private Map<String, DatoSpecifico> datiSpecificiMigrazione;

    // riferimento al ComponenteMM: questo è non null solo se il versamento è di
    // tipo VersamentoMM
    private transient ComponenteMM rifComponenteMM = null;
    // i componenti sono:
    // 1.SottoComponente = NULL
    // 2.rifComponenteVersPadre = NULL
    // i sottocomponenti sono:
    // 1.SottoComponente è valorizzato
    // 2.rifComponenteVersPadre è valorizzato
    private transient it.eng.parer.ws.xml.versReq.ComponenteType myComponente;
    private transient it.eng.parer.ws.xml.versReq.SottoComponenteType mySottoComponente;
    //
    private DocumentoVers rifDocumentoVers;
    private ComponenteVers rifComponenteVersPadre;
    // riferimento al file binario versato nella servlet
    // oppure dal versamento Multimedia
    private FileBinario rifFileBinario;
    //
    private TipiSupporto tipoSupporto;

    //
    private String urnPartComponenteNiOrdDoc = "";

    public ComponenteVers() {
	//
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getChiaveComp() {
	return chiaveComp;
    }

    public void setChiaveComp(String chiaveComp) {
	this.chiaveComp = chiaveComp;
    }

    public boolean isDatiLetti() {
	return datiLetti;
    }

    public void setDatiLetti(boolean datiLetti) {
	this.datiLetti = datiLetti;
    }

    public TipiSupporto getTipoSupporto() {
	return tipoSupporto;
    }

    public void setTipoSupporto(TipiSupporto tipoSupporto) {
	this.tipoSupporto = tipoSupporto;
    }

    public boolean isPresenteRifMeta() {
	return presenteRifMeta;
    }

    public void setPresenteRifMeta(boolean presenteRifMeta) {
	this.presenteRifMeta = presenteRifMeta;
    }

    public ComponenteMM getRifComponenteMM() {
	return rifComponenteMM;
    }

    public void setRifComponenteMM(ComponenteMM rifComponenteMM) {
	this.rifComponenteMM = rifComponenteMM;
    }

    public it.eng.parer.ws.xml.versReq.ComponenteType getMyComponente() {
	return myComponente;
    }

    public void setMyComponente(it.eng.parer.ws.xml.versReq.ComponenteType myComponente) {
	this.myComponente = myComponente;
    }

    public it.eng.parer.ws.xml.versReq.SottoComponenteType getMySottoComponente() {
	return mySottoComponente;
    }

    public void setMySottoComponente(
	    it.eng.parer.ws.xml.versReq.SottoComponenteType mySottoComponente) {
	this.mySottoComponente = mySottoComponente;
    }

    public DocumentoVers getRifDocumentoVers() {
	return rifDocumentoVers;
    }

    public void setRifDocumentoVers(DocumentoVers rifDocumentoVers) {
	this.rifDocumentoVers = rifDocumentoVers;
    }

    public ComponenteVers getRifComponenteVersPadre() {
	return rifComponenteVersPadre;
    }

    public void setRifComponenteVersPadre(ComponenteVers rifComponenteVersPadre) {
	this.rifComponenteVersPadre = rifComponenteVersPadre;
    }

    public long getIdTipoComponente() {
	return IdTipoComponente;
    }

    public void setIdTipoComponente(long IdTipoComponente) {
	this.IdTipoComponente = IdTipoComponente;
    }

    public long getIdTipoRappresentazioneComponente() {
	return idTipoRappresentazioneComponente;
    }

    public void setIdTipoRappresentazioneComponente(long idTipoRappresentazioneComponente) {
	this.idTipoRappresentazioneComponente = idTipoRappresentazioneComponente;
    }

    public long getIdFormatoFileDocCont() {
	return idFormatoFileDocCont;
    }

    public void setIdFormatoFileDocCont(long idFormatoFileDocCont) {
	this.idFormatoFileDocCont = idFormatoFileDocCont;
    }

    public long getIdFormatoFileDocConv() {
	return idFormatoFileDocConv;
    }

    public void setIdFormatoFileDocConv(long idFormatoFileDocConv) {
	this.idFormatoFileDocConv = idFormatoFileDocConv;
    }

    public boolean isNonAccettareForzaFormato() {
	return nonAccettareForzaFormato;
    }

    public void setNonAccettareForzaFormato(boolean nonAccettareForzaFormato) {
	this.nonAccettareForzaFormato = nonAccettareForzaFormato;
    }

    public TipoAlgoritmoRappr getAlgoritmoRappr() {
	return algoritmoRappr;
    }

    public void setAlgoritmoRappr(TipoAlgoritmoRappr algoritmoRappr) {
	this.algoritmoRappr = algoritmoRappr;
    }

    public String getTipoUso() {
	return tipoUso;
    }

    public void setTipoUso(String tipoUso) {
	this.tipoUso = tipoUso;
    }

    public Date getTmRifTempVers() {
	return tmRifTempVers;
    }

    public void setTmRifTempVers(Date tmRifTempVers) {
	this.tmRifTempVers = tmRifTempVers;
    }

    public String getFlRifTempDataFirmaVers() {
	return flRifTempDataFirmaVers;
    }

    public void setFlRifTempDataFirmaVers(String flRifTempDataFirmaVers) {
	this.flRifTempDataFirmaVers = flRifTempDataFirmaVers;
    }

    public long getIdFormatoFileVers() {
	return idFormatoFileVers;
    }

    public void setIdFormatoFileVers(long idFormatoFileVers) {
	this.idFormatoFileVers = idFormatoFileVers;
    }

    public boolean isFormatoFileVersNonAmmesso() {
	return formatoFileVersNonAmmesso;
    }

    public void setFormatoFileVersNonAmmesso(boolean formatoFileVersNonAmmesso) {
	this.formatoFileVersNonAmmesso = formatoFileVersNonAmmesso;
    }

    public String getDescFormatoFileVers() {
	return descFormatoFileVers;
    }

    public void setDescFormatoFileVers(String descFormatoFileVers) {
	this.descFormatoFileVers = descFormatoFileVers;
    }

    public long getIdRecDB() {
	return idRecDB;
    }

    public void setIdRecDB(long idRecDB) {
	this.idRecDB = idRecDB;
    }

    public long getOrdinePresentazione() {
	return ordinePresentazione;
    }

    public void setOrdinePresentazione(long ordinePresentazione) {
	this.ordinePresentazione = ordinePresentazione;
    }

    public FileBinario getRifFileBinario() {
	return rifFileBinario;
    }

    public void setRifFileBinario(FileBinario rifFileBinario) {
	this.rifFileBinario = rifFileBinario;
    }

    public byte[] getHashCalcolato() {
	return hashCalcolato;
    }

    public void setHashCalcolato(byte[] hashCalcolato) {
	this.hashCalcolato = hashCalcolato;
    }

    public boolean isHashForzato() {
	return hashForzato;
    }

    public void setHashForzato(boolean hashForzato) {
	this.hashForzato = hashForzato;
    }

    public boolean isHashVersNonDefinito() {
	return hashVersNonDefinito;
    }

    public void setHashVersNonDefinito(boolean hashVersNonDefinito) {
	this.hashVersNonDefinito = hashVersNonDefinito;
    }

    public byte[] getHashVersato() {
	return hashVersato;
    }

    public void setHashVersato(byte[] hashVersato) {
	this.hashVersato = hashVersato;
    }

    public CostantiDB.TipiHash getHashVersatoAlgoritmo() {
	return hashVersatoAlgoritmo;
    }

    public void setHashVersatoAlgoritmo(CostantiDB.TipiHash hashVersatoAlgoritmo) {
	this.hashVersatoAlgoritmo = hashVersatoAlgoritmo;
    }

    public CostantiDB.TipiEncBinari getHashVersatoEncoding() {
	return hashVersatoEncoding;
    }

    public void setHashVersatoEncoding(CostantiDB.TipiEncBinari hashVersatoEncoding) {
	this.hashVersatoEncoding = hashVersatoEncoding;
    }

    public it.eng.parer.ws.xml.versResp.ECComponenteType getRifComponenteResp() {
	return rifComponenteResp;
    }

    public void setRifComponenteResp(
	    it.eng.parer.ws.xml.versResp.ECComponenteType rifComponenteResp) {
	this.rifComponenteResp = rifComponenteResp;
    }

    public it.eng.parer.ws.xml.versResp.ECSottoComponenteType getRifSottoComponenteResp() {
	return rifSottoComponenteResp;
    }

    public void setRifSottoComponenteResp(
	    it.eng.parer.ws.xml.versResp.ECSottoComponenteType rifSottoComponenteResp) {
	this.rifSottoComponenteResp = rifSottoComponenteResp;
    }

    public CompDocMock withAcdEntity() {
	if (acdEntity == null) {
	    acdEntity = new CompDocMock();
	}
	return acdEntity;
    }

    public long getIdUnitaDocRif() {
	return idUnitaDocRif;
    }

    public void setIdUnitaDocRif(long idUnitaDocRif) {
	this.idUnitaDocRif = idUnitaDocRif;
    }

    public Map<String, DatoSpecifico> getDatiSpecifici() {
	return datiSpecifici;
    }

    @Override
    public void setDatiSpecifici(Map<String, DatoSpecifico> datiSpecifici) {
	this.datiSpecifici = datiSpecifici;
    }

    @Override
    public Map<String, DatoSpecifico> getDatiSpecificiMigrazione() {
	return datiSpecificiMigrazione;
    }

    @Override
    public void setDatiSpecificiMigrazione(Map<String, DatoSpecifico> datiSpecificiMigrazione) {
	this.datiSpecificiMigrazione = datiSpecificiMigrazione;
    }

    @Override
    public long getIdRecXsdDatiSpec() {
	return idRecXsdDatiSpec;
    }

    @Override
    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
	this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    @Override
    public long getIdRecXsdDatiSpecMigrazione() {
	return idRecXsdDatiSpecMigrazione;
    }

    @Override
    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpecMigrazione) {
	this.idRecXsdDatiSpecMigrazione = idRecXsdDatiSpecMigrazione;
    }

    public String getNomeFileArk() {
	return nomeFileArk;
    }

    public void setNomeFileArk(String nomeFileArk) {
	this.nomeFileArk = nomeFileArk;
    }

    public VerificaFirmaWrapper getVfWrapper() {
	if (vfWrapper == null) {
	    vfWrapper = new VerificaFirmaWrapper();
	}
	return vfWrapper;
    }

    public void setVfWrapper(VerificaFirmaWrapper vfWrapper) {
	this.vfWrapper = vfWrapper;
    }

    public enum TipiSupporto {

	FILE, METADATI, RIFERIMENTO
    }

    /**
     * Helper method per capire se sia necessario o meno effettuare il salvataggio delle buste
     * crittografiche
     *
     * @return true se il componente è firmato/marcato; false altrimenti.
     */
    public boolean hasBusteOnVerificaFirmaWrapper() {
	return !getVfWrapper().getVFBusta().isEmpty();
    }

    public String getUrnPartComponenteNiOrdDoc() {
	return urnPartComponenteNiOrdDoc;
    }

    public void setUrnPartComponenteNiOrdDoc(String urnPartComponenteNiOrdDoc) {
	this.urnPartComponenteNiOrdDoc = urnPartComponenteNiOrdDoc;
    }

    @Override
    public Long getIdRecUsoXsdProfiloNormativo() {
	throw new NotImplementedException(
		"getIdRecUsoXsdProfiloNormativo non implementato per il componente");
    }

    @Override
    public void setIdRecUsoXsdProfiloNormativo(Long idRecUsoXsdProfiloNormativo) {
	throw new NotImplementedException(
		"setIdRecUsoXsdProfiloNormativo non implementato per il componente");
    }

    @Override
    public String getDatiC14NProfNormXml() {
	throw new NotImplementedException(
		"getDatiC14NProfNormXml non implementato per il componente");
    }

    @Override
    public void setDatiC14NProfNormXml(String datiC14NProfNormXml) {
	throw new NotImplementedException(
		"setDatiC14NProfNormXml non implementato per il componente");
    }

}
