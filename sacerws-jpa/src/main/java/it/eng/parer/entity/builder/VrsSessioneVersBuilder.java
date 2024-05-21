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

package it.eng.parer.entity.builder;

import it.eng.parer.entity.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public final class VrsSessioneVersBuilder {
    private BigDecimal aaKeyUnitaDoc;
    private String cdErrPrinc;
    private String cdKeyDocVers;
    private String cdKeyUnitaDoc;
    private String cdRegistroKeyUnitaDoc;
    private String cdVersioneWs;
    private String dsErrPrinc;
    private Date dtApertura;
    private Date dtChiusura;
    private Date tsApertura;
    private Date tsChiusura;
    private String flSessioneErrNonRisolub;
    private String flSessioneErrVerif;
    private BigDecimal niFileErr;
    private String nmAmbiente;
    private String nmEnte;
    private String nmStrut;
    private String nmUserid;
    private String nmUseridWs;
    private String nmUtente;
    private String tiSessioneVers;
    private String cdIndIpClient;
    private String cdIndServer;
    private List<VrsDatiSessioneVers> vrsDatiSessioneVers;
    private List<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo;
    private AroDoc aroDoc;
    private AroUnitaDoc aroUnitaDoc;
    private OrgStrut orgStrut;
    private IamUser iamUser;

    private VrsSessioneVersBuilder() {
    }

    public static VrsSessioneVersBuilder builder() {
        return new VrsSessioneVersBuilder();
    }

    public VrsSessioneVersBuilder aaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
        return this;
    }

    public VrsSessioneVersBuilder cdErrPrinc(String cdErrPrinc) {
        this.cdErrPrinc = cdErrPrinc;
        return this;
    }

    public VrsSessioneVersBuilder cdKeyDocVers(String cdKeyDocVers) {
        this.cdKeyDocVers = cdKeyDocVers;
        return this;
    }

    public VrsSessioneVersBuilder cdKeyUnitaDoc(String cdKeyUnitaDoc) {
        this.cdKeyUnitaDoc = cdKeyUnitaDoc;
        return this;
    }

    public VrsSessioneVersBuilder cdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
        this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
        return this;
    }

    public VrsSessioneVersBuilder cdVersioneWs(String cdVersioneWs) {
        this.cdVersioneWs = cdVersioneWs;
        return this;
    }

    public VrsSessioneVersBuilder dsErrPrinc(String dsErrPrinc) {
        this.dsErrPrinc = dsErrPrinc;
        return this;
    }

    public VrsSessioneVersBuilder dtApertura(Date dtApertura) {
        this.dtApertura = dtApertura;
        return this;
    }

    public VrsSessioneVersBuilder dtChiusura(Date dtChiusura) {
        this.dtChiusura = dtChiusura;
        return this;
    }

    public VrsSessioneVersBuilder tsApertura(Date tsApertura) {
        this.tsApertura = tsApertura;
        return this;
    }

    public VrsSessioneVersBuilder tsChiusura(Date tsChiusura) {
        this.tsChiusura = tsChiusura;
        return this;
    }

    public VrsSessioneVersBuilder flSessioneErrNonRisolub(String flSessioneErrNonRisolub) {
        this.flSessioneErrNonRisolub = flSessioneErrNonRisolub;
        return this;
    }

    public VrsSessioneVersBuilder flSessioneErrVerif(String flSessioneErrVerif) {
        this.flSessioneErrVerif = flSessioneErrVerif;
        return this;
    }

    public VrsSessioneVersBuilder niFileErr(BigDecimal niFileErr) {
        this.niFileErr = niFileErr;
        return this;
    }

    public VrsSessioneVersBuilder nmAmbiente(String nmAmbiente) {
        this.nmAmbiente = nmAmbiente;
        return this;
    }

    public VrsSessioneVersBuilder nmEnte(String nmEnte) {
        this.nmEnte = nmEnte;
        return this;
    }

    public VrsSessioneVersBuilder nmStrut(String nmStrut) {
        this.nmStrut = nmStrut;
        return this;
    }

    public VrsSessioneVersBuilder nmUserid(String nmUserid) {
        this.nmUserid = nmUserid;
        return this;
    }

    public VrsSessioneVersBuilder nmUseridWs(String nmUseridWs) {
        this.nmUseridWs = nmUseridWs;
        return this;
    }

    public VrsSessioneVersBuilder nmUtente(String nmUtente) {
        this.nmUtente = nmUtente;
        return this;
    }

    public VrsSessioneVersBuilder tiSessioneVers(String tiSessioneVers) {
        this.tiSessioneVers = tiSessioneVers;
        return this;
    }

    public VrsSessioneVersBuilder cdIndIpClient(String cdIndIpClient) {
        this.cdIndIpClient = cdIndIpClient;
        return this;
    }

    public VrsSessioneVersBuilder cdIndServer(String cdIndServer) {
        this.cdIndServer = cdIndServer;
        return this;
    }

    public VrsSessioneVersBuilder vrsDatiSessioneVers(List<VrsDatiSessioneVers> vrsDatiSessioneVers) {
        this.vrsDatiSessioneVers = vrsDatiSessioneVers;
        return this;
    }

    public VrsSessioneVersBuilder vrsDatiSessioneVersKo(List<VrsDatiSessioneVersKo> vrsDatiSessioneVersKo) {
        this.vrsDatiSessioneVersKo = vrsDatiSessioneVersKo;
        return this;
    }

    public VrsSessioneVersBuilder aroDoc(AroDoc aroDoc) {
        this.aroDoc = aroDoc;
        return this;
    }

    public VrsSessioneVersBuilder aroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
        return this;
    }

    public VrsSessioneVersBuilder orgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
        return this;
    }

    public VrsSessioneVersBuilder iamUser(IamUser iamUser) {
        this.iamUser = iamUser;
        return this;
    }

    public VrsSessioneVers buildVrsSessioneVers() {
        VrsSessioneVers vrsSessioneVers = new VrsSessioneVers();
        vrsSessioneVers.setAaKeyUnitaDoc(aaKeyUnitaDoc);
        vrsSessioneVers.setCdErrPrinc(cdErrPrinc);
        vrsSessioneVers.setCdKeyDocVers(cdKeyDocVers);
        vrsSessioneVers.setCdKeyUnitaDoc(cdKeyUnitaDoc);
        vrsSessioneVers.setCdRegistroKeyUnitaDoc(cdRegistroKeyUnitaDoc);
        vrsSessioneVers.setCdVersioneWs(cdVersioneWs);
        vrsSessioneVers.setDsErrPrinc(dsErrPrinc);
        vrsSessioneVers.setDtApertura(dtApertura);
        vrsSessioneVers.setDtChiusura(dtChiusura);
        vrsSessioneVers.setTsApertura(tsApertura);
        vrsSessioneVers.setTsChiusura(tsChiusura);
        vrsSessioneVers.setFlSessioneErrNonRisolub(flSessioneErrNonRisolub);
        vrsSessioneVers.setFlSessioneErrVerif(flSessioneErrVerif);
        vrsSessioneVers.setNiFileErr(niFileErr);
        vrsSessioneVers.setNmAmbiente(nmAmbiente);
        vrsSessioneVers.setNmEnte(nmEnte);
        vrsSessioneVers.setNmStrut(nmStrut);
        vrsSessioneVers.setNmUserid(nmUserid);
        vrsSessioneVers.setNmUseridWs(nmUseridWs);
        vrsSessioneVers.setNmUtente(nmUtente);
        vrsSessioneVers.setTiSessioneVers(tiSessioneVers);
        vrsSessioneVers.setTiStatoSessioneVers("CHIUSA_OK");
        vrsSessioneVers.setCdIndIpClient(cdIndIpClient);
        vrsSessioneVers.setCdIndServer(cdIndServer);
        vrsSessioneVers.setVrsDatiSessioneVers(vrsDatiSessioneVers);
        vrsSessioneVers.setAroDoc(aroDoc);
        vrsSessioneVers.setAroUnitaDoc(aroUnitaDoc);
        vrsSessioneVers.setOrgStrut(orgStrut);
        vrsSessioneVers.setIamUser(iamUser);
        return vrsSessioneVers;
    }

    public VrsSessioneVersKo buildVrsSessioneVersKo() {
        VrsSessioneVersKo vrsSessioneVers = new VrsSessioneVersKo();
        vrsSessioneVers.setAaKeyUnitaDoc(aaKeyUnitaDoc);
        vrsSessioneVers.setCdErrPrinc(cdErrPrinc);
        vrsSessioneVers.setCdKeyDocVers(cdKeyDocVers);
        vrsSessioneVers.setCdKeyUnitaDoc(cdKeyUnitaDoc);
        vrsSessioneVers.setCdRegistroKeyUnitaDoc(cdRegistroKeyUnitaDoc);
        vrsSessioneVers.setCdVersioneWs(cdVersioneWs);
        vrsSessioneVers.setDsErrPrinc(dsErrPrinc);
        vrsSessioneVers.setDtApertura(dtApertura);
        vrsSessioneVers.setDtChiusura(dtChiusura);
        vrsSessioneVers.setTsApertura(tsApertura);
        vrsSessioneVers.setTsChiusura(tsChiusura);
        vrsSessioneVers.setFlSessioneErrNonRisolub(flSessioneErrNonRisolub);
        vrsSessioneVers.setFlSessioneErrVerif(flSessioneErrVerif);
        vrsSessioneVers.setNiFileErr(niFileErr);
        vrsSessioneVers.setNmAmbiente(nmAmbiente);
        vrsSessioneVers.setNmEnte(nmEnte);
        vrsSessioneVers.setNmStrut(nmStrut);
        vrsSessioneVers.setNmUserid(nmUserid);
        vrsSessioneVers.setNmUseridWs(nmUseridWs);
        vrsSessioneVers.setNmUtente(nmUtente);
        vrsSessioneVers.setTiSessioneVers(tiSessioneVers);
        vrsSessioneVers.setTiStatoSessioneVers("CHIUSA_ERR");
        vrsSessioneVers.setCdIndIpClient(cdIndIpClient);
        vrsSessioneVers.setCdIndServer(cdIndServer);
        vrsSessioneVers.setVrsDatiSessioneVersKos(vrsDatiSessioneVersKo);
        vrsSessioneVers.setAroDoc(aroDoc);
        vrsSessioneVers.setAroUnitaDoc(aroUnitaDoc);
        vrsSessioneVers.setOrgStrut(orgStrut);
        vrsSessioneVers.setIamUser(iamUser);
        return vrsSessioneVers;
    }
}
