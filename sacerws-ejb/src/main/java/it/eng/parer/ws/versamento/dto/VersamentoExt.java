package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.Costanti.VersioneWS;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import java.util.EnumSet;

/**
 *
 * @author Fioravanti_F
 */
public class VersamentoExt extends AbsVersamentoExt {

    private static final long serialVersionUID = 5261426459498072293L;
    private String datiXml;
    private boolean simulaScrittura;
    private UnitaDocumentaria versamento;
    private IWSDesc descrizione;
    //
    private VersioneWS versioneCalc = null;
    private EnumSet<ModificatoriWS> modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

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
    public EnumSet<ModificatoriWS> getModificatoriWSCalc() {
        return this.modificatoriWS;
    }

    @Override
    public String getVersioneCalc() {
        return this.versioneCalc.getVersion();
    }

    public EnumSet<ModificatoriWS> getModificatoriWS() {
        return modificatoriWS;
    }

    public void setModificatoriWS(EnumSet<ModificatoriWS> modificatoriWS) {
        this.modificatoriWS = modificatoriWS;
    }

}
