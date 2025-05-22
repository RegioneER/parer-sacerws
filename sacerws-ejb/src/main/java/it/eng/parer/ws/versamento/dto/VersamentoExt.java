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

import java.util.EnumSet;
import java.util.Set;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.Costanti.VersioneWS;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;

/**
 *
 * @author Fioravanti_F
 */
public class VersamentoExt extends AbsVersamentoExt {

    private static final long serialVersionUID = 5261426459498072293L;
    private String datiXml;
    private boolean simulaScrittura;
    private transient UnitaDocumentaria versamento;
    private transient IWSDesc descrizione;
    //
    private VersioneWS versioneCalc = null;
    private Set<ModificatoriWS> modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

    @Override
    public void setDatiXml(String datiXml) {
	this.datiXml = datiXml;
    }

    @Override
    public String getDatiXml() {
	return datiXml;
    }

    @Override
    public boolean isSimulaScrittura() {
	return simulaScrittura;
    }

    @Override
    public void setSimulaScrittura(boolean simulaScrittura) {
	this.simulaScrittura = simulaScrittura;
    }

    @Override
    public IWSDesc getDescrizione() {
	return descrizione;
    }

    @Override
    public void setDescrizione(IWSDesc descrizione) {
	this.descrizione = descrizione;
    }

    public UnitaDocumentaria getVersamento() {
	return versamento;
    }

    public void setVersamento(UnitaDocumentaria versamento) {
	this.versamento = versamento;
    }

    //
    @Override
    public RispostaControlli checkVersioneRequest(String versione) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(true);

	versioneCalc = VersioneWS.evalute(versione);
	modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

	switch (versioneCalc) {
	case V1_5:
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25);
	    this.modificatoriWS.add(ModificatoriWS.TAG_MIGRAZIONE);
	    this.modificatoriWS.add(ModificatoriWS.TAG_DATISPEC_EXT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_ESTESI_1_3_OUT);

	    this.modificatoriWS.add(ModificatoriWS.TAG_RAPPORTO_VERS_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_LISTA_ERR_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_INFO_FIRME_EXT_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_CONSERV_ANTIC_ARCH_IN);
	    //
	    this.modificatoriWS.add(ModificatoriWS.TAG_ABILITA_FORZA_1_5);
	    //
	    this.modificatoriWS.add(ModificatoriWS.TAG_FIRMA_1_5);
	    this.modificatoriWS.add(ModificatoriWS.TAG_ESTESI_1_5_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_RAPPORTO_VERS_1_5);
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERSATORE_1_5);
	    // MEV#23176
	    this.modificatoriWS.add(ModificatoriWS.TAG_URN_SIP_1_5);
	    // end MEV#23176
	    this.modificatoriWS.add(ModificatoriWS.TAG_PROFILI_1_5);
	    break;
	case V1_4:
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25);
	    this.modificatoriWS.add(ModificatoriWS.TAG_MIGRAZIONE);
	    this.modificatoriWS.add(ModificatoriWS.TAG_DATISPEC_EXT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_ESTESI_1_3_OUT);

	    this.modificatoriWS.add(ModificatoriWS.TAG_RAPPORTO_VERS_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_LISTA_ERR_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_INFO_FIRME_EXT_OUT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_CONSERV_ANTIC_ARCH_IN);
	    break;
	case V1_3:
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25);
	    this.modificatoriWS.add(ModificatoriWS.TAG_MIGRAZIONE);
	    this.modificatoriWS.add(ModificatoriWS.TAG_DATISPEC_EXT);
	    this.modificatoriWS.add(ModificatoriWS.TAG_ESTESI_1_3_OUT);
	    break;
	case V1_25:
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERIFICA_FORMATI_1_25);
	    break;
	case V1_2:
	    this.modificatoriWS.add(ModificatoriWS.TAG_VERIFICA_FORMATI_OLD);
	    break;
	}

	return rispostaControlli;
    }

    @Override
    public Set<ModificatoriWS> getModificatoriWSCalc() {
	return this.modificatoriWS;
    }

    @Override
    public String getVersioneCalc() {
	return this.versioneCalc.getVersion();
    }

    public Set<ModificatoriWS> getModificatoriWS() {
	return modificatoriWS;
    }

    public void setModificatoriWS(Set<ModificatoriWS> modificatoriWS) {
	this.modificatoriWS = modificatoriWS;
    }

}
