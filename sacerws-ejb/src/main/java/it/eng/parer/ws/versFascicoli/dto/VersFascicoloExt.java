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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.dto;

import java.util.EnumSet;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.VersioneWS;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;

/**
 *
 * @author fioravanti_f
 */
public class VersFascicoloExt extends AbsVersFascicoloExt {

    private static final long serialVersionUID = 5261426459498072293L;
    private String datiXml;
    private boolean simulaScrittura;
    private IndiceSIPFascicolo versamento;

    private IWSDesc descrizione;
    private VersioneWS versioneCalc = null;
    private EnumSet<Costanti.ModificatoriWS> modificatoriWS = EnumSet
	    .noneOf(Costanti.ModificatoriWS.class);

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

    public IndiceSIPFascicolo getVersamento() {
	return versamento;
    }

    public void setVersamento(IndiceSIPFascicolo versamento) {
	this.versamento = versamento;
    }

    @Override
    public RispostaControlli checkVersioneRequest(String versione) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(true);

	versioneCalc = VersioneWS.evalute(versione);
	modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

	switch (versioneCalc) {
	case V1_1:
	    // MEV#25288
	    this.modificatoriWS.add(Costanti.ModificatoriWS.TAG_URN_SIP_FASC_1_1);
	    // end MEV#25288
	    break;
	}
	return rispostaControlli;
    }

    @Override
    public EnumSet<Costanti.ModificatoriWS> getModificatoriWSCalc() {
	return this.modificatoriWS;
    }

    @Override
    public String getVersioneCalc() {
	return this.versioneCalc.getVersion();
    }

    public EnumSet<Costanti.ModificatoriWS> getModificatoriWS() {
	return modificatoriWS;
    }

    public void setModificatoriWS(EnumSet<Costanti.ModificatoriWS> modificatoriWS) {
	this.modificatoriWS = modificatoriWS;
    }
}
