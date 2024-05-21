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

package it.eng.parer.firma.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class CompDocMock {

    private String cdEncodingHashFileCalc;
    private String cdEncodingHashFileVers;
    private String dlUrnCompVers;
    private String dsAlgoHashFileCalc;
    private String dsAlgoHashFileVers;
    private String dsEsitoVerifFirmeDtVers;
    private String dsFormatoRapprCalc;
    private String dsFormatoRapprEstesoCalc;
    private String dsHashFileCalc;
    private String dsHashFileContr;
    private String dsHashFileVers;
    private String dsIdCompVers;
    private String dsMsgEsitoContrFormato;
    private String dsMsgEsitoVerifFirme;
    private String dsNomeCompVers;
    private String dsNomeFileArk;
    private String dsRifTempVers;
    private String dsUrnCompCalc;
    private String flCompFirmato;
    private String flNoCalcFmtVerifFirme;
    private String flNoCalcHashFile;
    private String flRifTempDataFirmaVers;
    private BigDecimal idStrut;
    private BigDecimal niOrdCompDoc;
    private BigDecimal niSizeFileCalc;
    private String tiEsitoContrFormatoFile;
    private String tiEsitoContrHashVers;
    private String tiEsitoVerifFirme;
    private String tiEsitoVerifFirmeDtVers;
    private String tiSupportoComp;
    private ZonedDateTime tmRifTempVers;
    private CompDocMock aroCompDoc;

    private BigDecimal idDecFormatoFileStandard = null;

    public CompDocMock() {
    }

    public String getCdEncodingHashFileCalc() {
        return this.cdEncodingHashFileCalc;
    }

    public void setCdEncodingHashFileCalc(String cdEncodingHashFileCalc) {
        this.cdEncodingHashFileCalc = cdEncodingHashFileCalc;
    }

    public String getCdEncodingHashFileVers() {
        return this.cdEncodingHashFileVers;
    }

    public void setCdEncodingHashFileVers(String cdEncodingHashFileVers) {
        this.cdEncodingHashFileVers = cdEncodingHashFileVers;
    }

    public String getDlUrnCompVers() {
        return this.dlUrnCompVers;
    }

    public void setDlUrnCompVers(String dlUrnCompVers) {
        this.dlUrnCompVers = dlUrnCompVers;
    }

    public String getDsAlgoHashFileCalc() {
        return this.dsAlgoHashFileCalc;
    }

    public void setDsAlgoHashFileCalc(String dsAlgoHashFileCalc) {
        this.dsAlgoHashFileCalc = dsAlgoHashFileCalc;
    }

    public String getDsAlgoHashFileVers() {
        return this.dsAlgoHashFileVers;
    }

    public void setDsAlgoHashFileVers(String dsAlgoHashFileVers) {
        this.dsAlgoHashFileVers = dsAlgoHashFileVers;
    }

    public String getDsEsitoVerifFirmeDtVers() {
        return this.dsEsitoVerifFirmeDtVers;
    }

    public void setDsEsitoVerifFirmeDtVers(String dsEsitoVerifFirmeDtVers) {
        this.dsEsitoVerifFirmeDtVers = dsEsitoVerifFirmeDtVers;
    }

    public String getDsFormatoRapprCalc() {
        return this.dsFormatoRapprCalc;
    }

    public void setDsFormatoRapprCalc(String dsFormatoRapprCalc) {
        this.dsFormatoRapprCalc = dsFormatoRapprCalc;
    }

    public String getDsFormatoRapprEstesoCalc() {
        return this.dsFormatoRapprEstesoCalc;
    }

    public void setDsFormatoRapprEstesoCalc(String dsFormatoRapprEstesoCalc) {
        this.dsFormatoRapprEstesoCalc = dsFormatoRapprEstesoCalc;
    }

    public String getDsHashFileCalc() {
        return this.dsHashFileCalc;
    }

    public void setDsHashFileCalc(String dsHashFileCalc) {
        this.dsHashFileCalc = dsHashFileCalc;
    }

    public String getDsHashFileContr() {
        return this.dsHashFileContr;
    }

    public void setDsHashFileContr(String dsHashFileContr) {
        this.dsHashFileContr = dsHashFileContr;
    }

    public String getDsHashFileVers() {
        return this.dsHashFileVers;
    }

    public void setDsHashFileVers(String dsHashFileVers) {
        this.dsHashFileVers = dsHashFileVers;
    }

    public String getDsIdCompVers() {
        return this.dsIdCompVers;
    }

    public void setDsIdCompVers(String dsIdCompVers) {
        this.dsIdCompVers = dsIdCompVers;
    }

    public String getDsMsgEsitoContrFormato() {
        return this.dsMsgEsitoContrFormato;
    }

    public void setDsMsgEsitoContrFormato(String dsMsgEsitoContrFormato) {
        this.dsMsgEsitoContrFormato = dsMsgEsitoContrFormato;
    }

    public String getDsMsgEsitoVerifFirme() {
        return this.dsMsgEsitoVerifFirme;
    }

    public void setDsMsgEsitoVerifFirme(String dsMsgEsitoVerifFirme) {
        this.dsMsgEsitoVerifFirme = dsMsgEsitoVerifFirme;
    }

    public String getDsNomeCompVers() {
        return this.dsNomeCompVers;
    }

    public void setDsNomeCompVers(String dsNomeCompVers) {
        this.dsNomeCompVers = dsNomeCompVers;
    }

    public String getDsNomeFileArk() {
        return dsNomeFileArk;
    }

    public void setDsNomeFileArk(String dsNomeFileArk) {
        this.dsNomeFileArk = dsNomeFileArk;
    }

    public String getDsRifTempVers() {
        return this.dsRifTempVers;
    }

    public void setDsRifTempVers(String dsRifTempVers) {
        this.dsRifTempVers = dsRifTempVers;
    }

    public String getDsUrnCompCalc() {
        return this.dsUrnCompCalc;
    }

    public void setDsUrnCompCalc(String dsUrnCompCalc) {
        this.dsUrnCompCalc = dsUrnCompCalc;
    }

    public String getFlCompFirmato() {
        return this.flCompFirmato;
    }

    public void setFlCompFirmato(String flCompFirmato) {
        this.flCompFirmato = flCompFirmato;
    }

    public String getFlNoCalcFmtVerifFirme() {
        return this.flNoCalcFmtVerifFirme;
    }

    public void setFlNoCalcFmtVerifFirme(String flNoCalcFmtVerifFirme) {
        this.flNoCalcFmtVerifFirme = flNoCalcFmtVerifFirme;
    }

    public String getFlNoCalcHashFile() {
        return this.flNoCalcHashFile;
    }

    public void setFlNoCalcHashFile(String flNoCalcHashFile) {
        this.flNoCalcHashFile = flNoCalcHashFile;
    }

    public String getFlRifTempDataFirmaVers() {
        return this.flRifTempDataFirmaVers;
    }

    public void setFlRifTempDataFirmaVers(String flRifTempDataFirmaVers) {
        this.flRifTempDataFirmaVers = flRifTempDataFirmaVers;
    }

    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    public BigDecimal getNiOrdCompDoc() {
        return this.niOrdCompDoc;
    }

    public void setNiOrdCompDoc(BigDecimal niOrdCompDoc) {
        this.niOrdCompDoc = niOrdCompDoc;
    }

    public BigDecimal getNiSizeFileCalc() {
        return this.niSizeFileCalc;
    }

    public void setNiSizeFileCalc(BigDecimal niSizeFileCalc) {
        this.niSizeFileCalc = niSizeFileCalc;
    }

    public String getTiEsitoContrFormatoFile() {
        return this.tiEsitoContrFormatoFile;
    }

    public void setTiEsitoContrFormatoFile(String tiEsitoContrFormatoFile) {
        this.tiEsitoContrFormatoFile = tiEsitoContrFormatoFile;
    }

    public String getTiEsitoContrHashVers() {
        return this.tiEsitoContrHashVers;
    }

    public void setTiEsitoContrHashVers(String tiEsitoContrHashVers) {
        this.tiEsitoContrHashVers = tiEsitoContrHashVers;
    }

    public String getTiEsitoVerifFirme() {
        return this.tiEsitoVerifFirme;
    }

    public void setTiEsitoVerifFirme(String tiEsitoVerifFirme) {
        this.tiEsitoVerifFirme = tiEsitoVerifFirme;
    }

    public String getTiEsitoVerifFirmeDtVers() {
        return this.tiEsitoVerifFirmeDtVers;
    }

    public void setTiEsitoVerifFirmeDtVers(String tiEsitoVerifFirmeDtVers) {
        this.tiEsitoVerifFirmeDtVers = tiEsitoVerifFirmeDtVers;
    }

    public String getTiSupportoComp() {
        return this.tiSupportoComp;
    }

    public void setTiSupportoComp(String tiSupportoComp) {
        this.tiSupportoComp = tiSupportoComp;
    }

    public ZonedDateTime getTmRifTempVers() {
        return this.tmRifTempVers;
    }

    public void setTmRifTempVers(ZonedDateTime tmRifTempVers) {
        this.tmRifTempVers = tmRifTempVers;
    }

    public CompDocMock getAroCompDoc() {
        return this.aroCompDoc;
    }

    public void setAroCompDoc(CompDocMock aroCompDoc) {
        this.aroCompDoc = aroCompDoc;
    }

    public BigDecimal getIdDecFormatoFileStandard() {
        return idDecFormatoFileStandard;
    }

    public void setIdDecFormatoFileStandard(BigDecimal idDecFormatoFileStandard) {
        this.idDecFormatoFileStandard = idDecFormatoFileStandard;
    }

}
