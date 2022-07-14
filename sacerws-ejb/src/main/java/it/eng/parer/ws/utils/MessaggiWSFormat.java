/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import java.time.ZonedDateTime;
import static it.eng.parer.util.DateUtilsConverter.convert;

/**
 *
 * @author Fioravanti_F
 */
public class MessaggiWSFormat {

    private MessaggiWSFormat() {
        throw new IllegalStateException("Utility class");
    }

    //
    public static String bonificaUrnPerNomeFile(String urn) {
        return urn.replaceAll("[^A-Za-z0-9\\. _-]", "_");
    }

    public static String bonificaID(String iD) {
        return iD.replace(" ", "_");
    }

    // MEV#16490
    public static String formattaUrnPartVersatore(CSVersatore versatore) {
        return formattaUrnPartVersatore(versatore, false, Costanti.UrnFormatter.VERS_FMT_STRING);
    }

    public static String formattaUrnPartVersatore(CSVersatore versatore, boolean toNormalize, String fmtUsed) {
        if (!toNormalize) {
            return MessageFormat.format(
                    fmtUsed, StringUtils.isNotBlank(versatore.getSistemaConservazione())
                            ? versatore.getSistemaConservazione() : versatore.getAmbiente(),
                    versatore.getEnte(), versatore.getStruttura());
        } else {
            return MessageFormat.format(fmtUsed,
                    StringUtils.isNotBlank(versatore.getSistemaConservazione())
                            ? MessaggiWSFormat.normalizingKey(versatore.getSistemaConservazione())
                            : MessaggiWSFormat.normalizingKey(versatore.getAmbiente()),
                    MessaggiWSFormat.normalizingKey(versatore.getEnte()),
                    MessaggiWSFormat.normalizingKey(versatore.getStruttura()));
        }
    }

    public static String formattaUrnPartUnitaDoc(CSChiave chiave) {
        return formattaUrnPartUnitaDoc(chiave, false, Costanti.UrnFormatter.UD_FMT_STRING);
    }

    public static String formattaUrnPartUnitaDoc(CSChiave chiave, boolean toNormalize, String fmtUsed) {
        if (!toNormalize) {
            return MessageFormat.format(fmtUsed, chiave.getTipoRegistro(), chiave.getAnno().toString(),
                    chiave.getNumero());
        } else {
            return MessageFormat.format(fmtUsed, MessaggiWSFormat.normalizingKey(chiave.getTipoRegistro()),
                    chiave.getAnno().toString(), MessaggiWSFormat.normalizingKey(chiave.getNumero()));
        }
    }

    public static String formattaUrnPartDocumento(CategoriaDocumento categoria, int progressivo) {
        return formattaUrnPartDocumento(categoria, progressivo, false, Costanti.UrnFormatter.DOC_FMT_STRING,
                Costanti.UrnFormatter.PADNODIGITS_FMT);
    }

    public static String formattaUrnPartDocumento(CategoriaDocumento categoria, int progressivo, boolean pgpad,
            String fmtUsed, String padfmtUsed) {
        return MessageFormat.format(fmtUsed, categoria.getValoreDb(),
                pgpad ? String.format(padfmtUsed, progressivo) : progressivo);
    }

    //
    public static String formattaChiaveDocumento(String chiaveUd, CategoriaDocumento categoria, int progressivo) {
        return formattaChiaveDocumento(chiaveUd, categoria, progressivo, false,
                Costanti.UrnFormatter.CHIAVE_DOC_FMT_STRING, Costanti.UrnFormatter.PADNODIGITS_FMT);
    }

    //
    public static String formattaChiaveDocumento(String chiaveUd, CategoriaDocumento categoria, int progressivo,
            boolean pgpad, String fmtUsed, String padfmtUsed) {
        return MessageFormat.format(Costanti.UrnFormatter.CHIAVE_DOC_FMT_STRING, chiaveUd,
                formattaUrnPartDocumento(categoria, progressivo, pgpad, fmtUsed, padfmtUsed));
    }

    /*
     * OLD ONE (in questo caso non si è "generalizzato" dato che la logica di calcola della chiave del componentente è
     * del tutto nuova) Vedi sotto : è di fatto cambiata la firma del metodo !
     */
    public static String formattaChiaveComponente(String chiaveDoc, int progressivoStrutDoc, long progressivoComp) {
        return MessageFormat.format(Costanti.UrnFormatter.CHIAVE_COMP_FMT_STRING, chiaveDoc, progressivoStrutDoc,
                progressivoComp);
    }

    //
    public static String formattaChiaveComponente(String chiaveDoc, long ordinePresentazione, String fmtUsed,
            String padfmtUsed) {
        return MessageFormat.format(Costanti.UrnFormatter.CHIAVE_COMP_FMT_STRING_V2,
                formattaUrnPartComponente(chiaveDoc, ordinePresentazione, fmtUsed, padfmtUsed));
    }

    //
    public static String formattaUrnPartComponente(String urnBase, long ordinePresentazione, String fmtUsed,
            String padfmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase, String.format(padfmtUsed, ordinePresentazione));
    }

    //
    public static String formattaSubPathData(Date data) {
        SimpleDateFormat f = new SimpleDateFormat(Costanti.UrnFormatter.SPATH_DATA_FMT_STRING);
        return f.format(data);
    }

    public static String formattaSubPathData(ZonedDateTime data) {
        return formattaSubPathData(convert(data));
    }

    public static String formattaSubPathVersatoreArk(CSVersatore versatore) {
        return MessageFormat.format(Costanti.UrnFormatter.SPATH_VERS_FMT_STRING, versatore.getAmbiente(),
                versatore.getEnte(), versatore.getStruttura());
    }

    public static String formattaSubPathUnitaDocArk(CSChiave chiave) {
        return MessageFormat.format(Costanti.UrnFormatter.SPATH_UD_FMT_STRING, chiave.getTipoRegistro(),
                chiave.getAnno().toString(), chiave.getNumero());
    }

    /**
     * @deprecated
     *
     *             Vecchia metodologia con cui definire il nome del file scritto su filesystem. Vedi metodo
     *             {@link #formattaNomeFileArk(String)}
     *
     * @param categoria
     *            categoria documento
     * @param progressivo
     *            numero progressivo
     * @param progressivoStrutDoc
     *            progressivo struttura
     * @param progressivoComp
     *            progressivo compontente
     *
     * @return nome del file scritto su filesystem
     */
    @Deprecated
    public static String formattaNomeFileArk(CategoriaDocumento categoria, int progressivo, int progressivoStrutDoc,
            long progressivoComp) {
        return MessageFormat.format(Costanti.UrnFormatter.SPATH_COMP_FMT_STRING, categoria.getValoreDb(), progressivo,
                progressivoStrutDoc, progressivoComp);
    }

    // NEW URN
    public static String formattaNomeFileArk(String urnBaseOrUrnPartComp) {
        Pattern p = Pattern.compile(Costanti.UrnFormatter.SPATH_COMP_FILENAME_REGEXP);
        Matcher m = p.matcher(urnBaseOrUrnPartComp);
        try {
            // find
            m.find();
            // replace
            return m.group(0).replace(Costanti.UrnFormatter.URN_STD_SEPARATOR,
                    Costanti.UrnFormatter.SPATH_FILE_STD_SEPARATOR_V2);
        } catch (Exception e) {
            throw new SacerWsRuntimeException(
                    "Errore durante estrazione da URN " + urnBaseOrUrnPartComp + " per definire il nome file", e,
                    SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    public static String formattaFilePathArk(String root, String rootVers, String pathData, String pathVersatore,
            String pathUd, String nomeFile) {
        return MessageFormat.format(Costanti.UrnFormatter.SPATH_FILE_FMT_STRING, root, rootVers, pathData,
                pathVersatore, pathUd, nomeFile);
    }

    public static String formattaFileLogRetrieve(CSVersatore versatore, CSChiave chiave) {
        return MessageFormat.format(Costanti.UrnFormatter.FNAME_LOG_TSM_RETRIEVE, versatore.getAmbiente(),
                versatore.getEnte(), versatore.getStruttura(), chiave.getTipoRegistro(), chiave.getAnno().toString(),
                chiave.getNumero());
    }

    public static Long formattaKeyPartAnnoMeseVers(Date dtVersamento) {
        Calendar date = Calendar.getInstance();
        date.setTime(dtVersamento);
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        return (long) year * 100 + month;
    }

    public static Long formattaKeyPartAnnoMeseVers(ZonedDateTime dtVersamento) {
        return formattaKeyPartAnnoMeseVers(convert(dtVersamento));
    }

    //
    public static String formattaBaseUrnUnitaDoc(String versatore, String unitaDoc) {
        return MessageFormat.format(Costanti.UrnFormatter.URN_UD_FMT_STRING, versatore, unitaDoc);
    }

    public static String formattaBaseUrnDoc(String versatore, String unitaDoc, String documento) {
        return formattaBaseUrnDoc(versatore, unitaDoc, documento, Costanti.UrnFormatter.URN_DOC_FMT_STRING);
    }

    public static String formattaBaseUrnDoc(String versatore, String unitaDoc, String documento, String fmtUsed) {
        return MessageFormat.format(fmtUsed, versatore, unitaDoc, documento);
    }

    //
    public static String formattaUrnDocUniDoc(String urnBase) {
        return MessageFormat.format(Costanti.UrnFormatter.URN_DOC_UNI_DOC_FMT_STRING, urnBase);
    }

    // old URN
    public static String formattaUrnIndiceSip(String urnBase) {
        return formattaUrnIndiceSip(urnBase, Costanti.UrnFormatter.URN_INDICE_SIP_FMT_STRING); // default
    }

    public static String formattaUrnIndiceSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnPiSip(String urnBase) {
        return formattaUrnPiSip(urnBase, Costanti.UrnFormatter.URN_PI_SIP_FMT_STRING);
    }

    public static String formattaUrnPiSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnEsitoVers(String urnBase) {
        return formattaUrnEsitoVers(urnBase, Costanti.UrnFormatter.URN_ESITO_VERS_FMT_STRING);
    }

    public static String formattaUrnEsitoVers(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    // old urn
    public static String formattaUrnRappVers(String urnBase) {
        return formattaUrnRappVers(urnBase, Costanti.UrnFormatter.URN_RAPP_VERS_FMT_STRING);
    }

    public static String formattaUrnRappVers(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    //
    // FASCICOLI
    //
    public static String formattaChiaveFascicolo(CSVersatore versatore, CSChiaveFasc chiave) {
        return formattaChiaveFascicolo(versatore, chiave, null);
    }

    // base
    public static String formattaChiaveFascicolo(CSVersatore versatore, CSChiaveFasc chiave, String sistema) {
        return formattaChiaveFascicolo(versatore, chiave, sistema, false);
    }

    public static String formattaChiaveFascicolo(CSVersatore versatore, CSChiaveFasc chiave, String sistema,
            boolean toNormalize) {
        if (!toNormalize) {
            return MessageFormat.format(Costanti.UrnFormatter.CHIAVE_FASC_FMT_STRING,
                    StringUtils.isNotBlank(sistema) ? sistema : versatore.getAmbiente(), versatore.getEnte(),
                    versatore.getStruttura(), chiave.getAnno().toString(), chiave.getNumero());
        } else {
            return MessageFormat.format(Costanti.UrnFormatter.CHIAVE_FASC_FMT_STRING,
                    StringUtils.isNotBlank(sistema) ? sistema : versatore.getAmbiente(),
                    MessaggiWSFormat.normalizingKey(versatore.getEnte()),
                    MessaggiWSFormat.normalizingKey(versatore.getStruttura()), chiave.getAnno().toString(),
                    chiave.getNumero());
        }
    }

    public static String formattaUrnIndiceSipFasc(String urnBase) {
        return formattaUrnIndiceSip(urnBase, Costanti.UrnFormatter.URN_INDICE_SIP_FASC_FMT_STRING);
    }

    public static String formattaUrnIndiceSipFasc(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnRappVersFasc(String urnBase) {
        return formattaUrnRappVersFasc(urnBase, Costanti.UrnFormatter.URN_RAPP_VERS_FASC_FMT_STRING);
    }

    public static String formattaUrnRappVersFasc(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnRappNegFasc(String urnBase) {
        return MessageFormat.format(Costanti.UrnFormatter.URN_RAPP_NEG_FASC_FMT_STRING, urnBase);
    }

    public static String formattaUrnPartFasc(CSChiaveFasc chiave) {
        return MessageFormat.format(Costanti.UrnFormatter.FASC_FMT_STRING, chiave.getAnno().toString(),
                chiave.getNumero());
    }

    // AGGIORNAMENTO UD
    public static String formattaBaseUrnUpdUnitaDoc(String versatore, String unitaDoc) {
        return formattaBaseUrnUpdUnitaDoc(versatore, unitaDoc, Costanti.UrnFormatter.UPD_FMT_STRING);
    }

    public static String formattaBaseUrnUpdUnitaDoc(String versatore, String unitaDoc, String fmtUsed) {
        return MessageFormat.format(fmtUsed, versatore, unitaDoc, -1);
    }

    // OLD URN
    public static String formattaBaseUrnUpdUnitaDoc(String versatore, String unitaDoc, long progressivo) {
        return formattaBaseUrnUpdUnitaDoc(versatore, unitaDoc, progressivo, false);
    }

    // NEW URN
    /*
     * Al passaggio dei nuovi URN (V2) sarà sufficiente cambiare il valore del boolean (vedi sopra)
     */
    public static String formattaBaseUrnUpdUnitaDoc(String versatore, String unitaDoc, long progressivo,
            boolean pgpad) {
        return formattaBaseUrnUpdUnitaDoc(versatore, unitaDoc, progressivo, pgpad, Costanti.UrnFormatter.UPD_FMT_STRING,
                Costanti.UrnFormatter.PADNODIGITS_FMT);
    }

    public static String formattaBaseUrnUpdUnitaDoc(String versatore, String unitaDoc, long progressivo, boolean pgpad,
            String fmtUsed, String padFmtUsed) {
        return MessageFormat.format(fmtUsed, versatore, unitaDoc,
                pgpad ? String.format(padFmtUsed, progressivo) : progressivo);
    }

    public static String formattaUrnPartRappVersUpd(String urnBase) {
        return formattaUrnPartRappVersUpd(urnBase, Costanti.UrnFormatter.URN_RAPP_VERS_FMT_STRING);
    }

    public static String formattaUrnPartRappVersUpd(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnIndiceSipUpd(String urnBase) {
        return formattaUrnIndiceSipUpd(urnBase, Costanti.UrnFormatter.URN_INDICE_SIP_FMT_STRING);
    }

    public static String formattaUrnIndiceSipUpd(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnReportVerificaFirma(String urnBase, String fmtUsed) {
        return formattaUrnReportVerificaFirma(urnBase, fmtUsed, false);
    }

    private static String formattaUrnReportVerificaFirma(String urnBase, String fmtUsed, boolean toNormalize) {
        return MessageFormat.format(fmtUsed, toNormalize ? MessaggiWSFormat.normalizingKey(urnBase) : urnBase);
    }

    public static String formattaUrnReportVerificaFirma(String urnBase, long pgBusta, String fmtUsed) {
        return formattaUrnReportVerificaFirma(urnBase, pgBusta, fmtUsed, false);
    }

    private static String formattaUrnReportVerificaFirma(String urnBase, long pgBusta, String fmtUsed,
            boolean toNormalize) {
        return MessageFormat.format(fmtUsed, toNormalize ? MessaggiWSFormat.normalizingKey(urnBase) : urnBase, pgBusta);
    }

    /* AWS : RULES FOR CD_KEY_FILE SU COMPONENTE */
    /*
     * Nota importante : la regola scelta si basa sulle dinaniche previste per gli URN ma a differenza di quest'ultime
     * anziché riportare il numero (che può contentere caratteri speciali) utilizza di base l'ID del componete
     * 
     * Su extra si possono aggiungere ulteriori parametri (in coda) sulla base del formatter (fmtUsed) che viene passato
     * al metodo.
     */
    public static String formattaComponenteCdKeyFile(CSVersatore versatore, CSChiave chiave, long idComponente,
            Optional<Object> extra, String fmtUsed) {
        return MessageFormat.format(fmtUsed,
                StringUtils.isNotBlank(versatore.getSistemaConservazione())
                        ? MessaggiWSFormat.normalizingKey(versatore.getSistemaConservazione())
                        : MessaggiWSFormat.normalizingKey(versatore.getAmbiente()),
                MessaggiWSFormat.normalizingKey(versatore.getEnte()),
                MessaggiWSFormat.normalizingKey(versatore.getStruttura()),
                MessaggiWSFormat.normalizingKey(chiave.getTipoRegistro()), chiave.getAnno().toString(),
                String.valueOf(idComponente), extra.isPresent() ? String.valueOf(extra.get()) : StringUtils.EMPTY);
    }

    /*
     * Restituisce una stringa normalizzata secondo le regole cel codice UD normalizzato sostituendo tutti i caratteri
     * accentati con i corrispondenti non accentati e ammettendo solo lettere, numeri, '.', '-' e '_'. Tutto il resto
     * viene convertito in '_'.
     */
    public static String normalizingKey(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replace(" ", "_")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("[^A-Za-z0-9\\. _-]", "_");
    }

}
