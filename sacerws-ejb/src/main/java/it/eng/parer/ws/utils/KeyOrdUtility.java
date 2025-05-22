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
package it.eng.parer.ws.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versamento.dto.ConfigRegAnno;

/**
 *
 * @author Fioravanti_F
 */
public class KeyOrdUtility {

    public enum TipiCalcolo {

	ALFABETICO("{Alfabetico}"), ALFANUMERICO("{Alfanumerico}"), NUMERICO("{Numerico}"), /**/
	NUMERICO_GENERICO("{Numerico Generico}"), NUMERI_ROMANI("{Numeri Romani}"), /**/
	PARTE_GENERICO("{Generico}"), GENERICO("{Formato Generico}");

	private String descrizione;

	private TipiCalcolo(String descrizione) {
	    this.descrizione = descrizione;
	}

	public String descrivi() {
	    return descrizione;
	}
    }

    //
    private final ConfigRegAnno configRegAnno;
    private final Pattern patternCompilato;
    private final Pattern patternNumericoGen;
    private String descKeyDaVerificare;
    private String descKeyUdVersata;
    private int maxLenNumero;

    private static final int PAD_NUM_4 = 4;
    private static final int PAD_NUM_12 = 12;

    // questa regexp ^[^\d]*(\d+)[^\d]*$
    private static final String REGEXP_NUMERICO_GEN = "^[^\\d]*(\\d+)[^\\d]*$";

    private static final String MESS_ERR_CHIAVE_TOOLONG = "La lunghezza del tag [numero] della chiave non può superare %s caratteri";
    private static final String MESS_ERR_FORMATO_CHIAVE = "La chiave deve essere rappresentata come: ";
    private static final String MESS_ERR_FORMATO_PARTE = "La parte %s non coincide con i valori ammessi (%s)";
    private static final String MESS_ERR_ANNO_PARTE = "La parte %s non coincide con l'anno dell'UD";
    private static final String MESS_ERR_REGISTRO_PARTE = "La parte %s non coincide con il registro dell'UD";
    private static final String MESS_ERR_NUMERO_TROPPO_GRANDE = "La parte %s definisce un numero troppo grande (max 9.223.372.036.854.775.807)";
    private static final String MESS_ERR_NUMERO_NON_TROVATO = "La parte %s non contiene una sequenza di numeri contigui";
    private static final String MESS_ERR_CHIAVECALC_TOOLONG = "La lunghezza di <registro>-<anno>-<numero normalizzato> non deve superare i 100 caratteri";

    public KeyOrdUtility(ConfigRegAnno config, int maxLenNumero) {
	this.configRegAnno = config;
	this.maxLenNumero = maxLenNumero;
	// prepara il test globale sulla regexp
	String regex = configRegAnno.getRegExpCalc();
	// questa compilazione nel costruttore è utile nel job di controllo,
	// in cui il metodo verificaChiave viene invocato molte volte usando
	// sempre lo stesso pattern
	patternCompilato = Pattern.compile(regex);
	// compilo questa regexp in ogni caso:
	// il costo iniziale è relativamente basso, ma se la devo usare in modo
	// ripetuto come nel job di controllo, il risparmio può essere sensibile
	patternNumericoGen = Pattern.compile(REGEXP_NUMERICO_GEN);
    }

    public RispostaControlli verificaChiave(CSChiave chiave) {
	return this.verificaChiave(chiave, null);
    }

    public RispostaControlli verificaChiave(CSChiave chiaveDaVerificare,
	    String descChiaveUdVersata) {
	RispostaControlli tmpControlli = new RispostaControlli();
	//
	// descKeyDaVerificare = MessaggiWSFormat.formattaUrnPartUnitaDoc(chiaveDaVerificare);
	descKeyDaVerificare = MessaggiWSFormat.formattaUrnPartUnitaDoc(chiaveDaVerificare, false,
		Costanti.UrnFormatter.UD_FMT_STRING);
	descKeyUdVersata = descChiaveUdVersata;
	String numero = chiaveDaVerificare.getNumero();
	Long progressivoCalc = null;
	StringBuilder tmpKeyOrdCalc = new StringBuilder();

	if (!this.verificaLunghezzaNumChiave(numero)) {
	    this.impostaErrore(tmpControlli, String.format(MESS_ERR_CHIAVE_TOOLONG, maxLenNumero));
	    return tmpControlli;
	}

	// test per parti, dopo aver verificato la regexp globale
	Matcher m = patternCompilato.matcher(numero);
	if (m.find() && m.groupCount() == configRegAnno.getParti().size()) {
	    // inizio loop sulle parti trovate
	    for (int iteratore = 0; iteratore < m.groupCount(); iteratore++) {
		String parte = m.group(iteratore + 1);
		ConfigRegAnno.ParteRegAnno prAnno = configRegAnno.getParti().get(iteratore);

		// test validi per tutti i tipi
		tmpControlli = this.verificheGeneriche(chiaveDaVerificare, parte, prAnno);
		if (!tmpControlli.isrBoolean()) {
		    return tmpControlli;
		}

		// TEST specifici per i vari tipi,
		tmpControlli = this.verificheSpecifiche(chiaveDaVerificare, tmpKeyOrdCalc, parte,
			prAnno);
		if (!tmpControlli.isrBoolean()) {
		    return tmpControlli;
		}

		// se la parte è un numero (decimale o romano) e la devo tenere come prog
		// calcolato...
		if (prAnno.isUsaComeProgressivo() && tmpControlli.getrLong() != -1) {
		    progressivoCalc = tmpControlli.getrLong();
		}
	    }
	} else {
	    // questo caso (regexp che match-a ma numero di gruppi trovato diverso dall'atteso)
	    // si presenta se la configurazione del periodo ha definito un criterio ambiguo.
	    // tecnicamente sarebbe un errore di configurazione (interno, con 666)
	    // ma così il messaggio è più bello...
	    this.impostaErrore(tmpControlli,
		    MESS_ERR_FORMATO_CHIAVE + configRegAnno.getDescRegExp());
	    return tmpControlli;
	}

	// sono uscito vivo dai test, calcolo e verifico la chiave calcolata
	tmpControlli = this.produciChiaveOrd(chiaveDaVerificare, tmpKeyOrdCalc);
	if (!tmpControlli.isrBoolean()) {
	    // se la chiave calcolata è troppo lunga, rendo errore
	    return tmpControlli;
	}
	//
	KeyOrdResult keyOrdResult = new KeyOrdResult();
	tmpControlli.setrObject(keyOrdResult);
	keyOrdResult.setProgressivoCalcolato(progressivoCalc);
	keyOrdResult.setKeyOrdCalcolata(tmpControlli.getrString());
	tmpControlli.setrBoolean(true);

	return tmpControlli;
    }

    /**
     * Produce una chiave di ordinamento generica e "sicura", considerando il numero come se avesse
     * il formato GENERICO e troncando il risultato nel caso superasse i limiti di lunghezza. In
     * ogni caso non rende un errore di validazione della chiave ma, ovviamente, il risultato
     * potrebbe essere impreciso.
     *
     * @param chiave la chiave dell'UD di cui calcolare la chiave ordinamento
     *
     * @return istanza di RispostaControlli contenente un'istanza di KeyOrdResult
     */
    public RispostaControlli calcolaKeyOrdGenerica(CSChiave chiave) {
	RispostaControlli tmpControlli = new RispostaControlli();
	//
	String numero = chiave.getNumero();
	Long progressivoCalc = null;
	StringBuilder tmpKeyOrdCalc = new StringBuilder();
	if (!this.verificaLunghezzaNumChiave(numero)) {
	    // se il numero è troppo lungo, lo tronco
	    numero = numero.substring(0, maxLenNumero);
	}
	//
	tmpKeyOrdCalc.append(StringUtils.leftPad(numero, PAD_NUM_12, "0"));

	// calcolo e verifico la chiave calcolata
	tmpControlli = this.produciChiaveOrd(chiave, tmpKeyOrdCalc);
	// tengo in ogni caso il risultato del calcolo - alla peggio è troncato a MAX_LEN_CHIAVEORD
	KeyOrdResult keyOrdResult = new KeyOrdResult();
	tmpControlli.setrObject(keyOrdResult);
	keyOrdResult.setKeyOrdCalcolata(tmpControlli.getrString());
	keyOrdResult.setProgressivoCalcolato(progressivoCalc);

	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private boolean verificaLunghezzaNumChiave(String stringa) {
	return (stringa.length() <= maxLenNumero);
    }

    private RispostaControlli verificheGeneriche(CSChiave chiave, String parte,
	    ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = new RispostaControlli();

	// test sulla lista di valori accettabili,
	if (prAnno.getListaValoriAccettabili() != null) {
	    if (!Arrays.asList(prAnno.getListaValoriAccettabili()).contains(parte)) {
		this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_PARTE,
			prAnno.getNomeParte(), prAnno.getDescValoriAccettabili()));
		return tmpControlli;
	    }
	}

	// test per vedere se la parte coincide con Registro chiave
	if (prAnno.isMatchRegistro() && !parte.trim().equals(chiave.getTipoRegistro().trim())) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_REGISTRO_PARTE, prAnno.getNomeParte()));
	    return tmpControlli;
	}
	//
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private RispostaControlli verificheSpecifiche(CSChiave chiave, StringBuilder sbKeyOrd,
	    String parte, ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = null;

	// TEST specifici per i vari tipi,
	// + costruzione per parti della chiave di ordinamento
	// usando i vari padding
	switch (prAnno.getTipoCalcolo()) {
	case NUMERICO:
	    tmpControlli = this.verificaTipoNumerico(chiave, sbKeyOrd, parte, prAnno);
	    break;
	case NUMERICO_GENERICO:
	    tmpControlli = this.verificaTipoNumericoGen(chiave, sbKeyOrd, parte, prAnno);
	    break;
	case NUMERI_ROMANI:
	    tmpControlli = this.verificaTipoRomano(chiave, sbKeyOrd, parte, prAnno);
	    break;
	case ALFABETICO:
	case ALFANUMERICO:
	case GENERICO:
	case PARTE_GENERICO:
	    tmpControlli = this.verificaTipoGenerico(sbKeyOrd, parte, prAnno);
	    break;
	default:
	    //
	}
	return tmpControlli;
    }

    private RispostaControlli verificaTipoNumerico(CSChiave chiave, StringBuilder sbKeyOrd,
	    String parte, ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = new RispostaControlli();

	Long num = null;
	try {
	    num = Long.parseLong(parte);
	} catch (NumberFormatException ex) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_NUMERO_TROPPO_GRANDE, prAnno.getNomeParte()));
	    return tmpControlli;
	}
	tmpControlli.setrLong(num);

	// test sul range di valori
	if (prAnno.getMinValAccettabile() != null) {
	    if (prAnno.getMinValAccettabile() > num || prAnno.getMaxValAccettabile() < num) {
		this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_PARTE,
			prAnno.getNomeParte(), prAnno.getDescValoriAccettabili()));
		return tmpControlli;
	    }
	}

	// test per vedere se la parte coincide con Anno chiave
	if (prAnno.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_ANNO_PARTE, prAnno.getNomeParte()));
	    return tmpControlli;
	}

	// padding
	Long numPad = (prAnno.getMaxLen() != -1 ? prAnno.getMaxLen() : PAD_NUM_12);
	sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));
	if (prAnno.getSeparatore() != null && prAnno.getSeparatore().length() > 0) {
	    sbKeyOrd.append(prAnno.getSeparatore());
	}
	//
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private RispostaControlli verificaTipoNumericoGen(CSChiave chiave, StringBuilder sbKeyOrd,
	    String parte, ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = new RispostaControlli();

	// verifica se quanto recuperato contiene una sequenza contigua di cifre numeriche.
	// se c'è, la estrae e gestisce il tutto come se fosse una parte numerica "normale"
	Long num = null;
	Matcher m = patternNumericoGen.matcher(parte);
	if (m.find() && m.groupCount() == 1) {
	    try {
		num = Long.parseLong(m.group(1));
	    } catch (NumberFormatException ex) {
		this.impostaErrore(tmpControlli,
			String.format(MESS_ERR_NUMERO_TROPPO_GRANDE, prAnno.getNomeParte()));
		return tmpControlli;
	    }
	    tmpControlli.setrLong(num);
	} else {
	    // se non c'è o ce ne sono più di una, segnala l'errore
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_NUMERO_NON_TROVATO, prAnno.getNomeParte()));
	    return tmpControlli;
	}

	// test sul range di valori
	if (prAnno.getMinValAccettabile() != null) {
	    if (prAnno.getMinValAccettabile() > num || prAnno.getMaxValAccettabile() < num) {
		this.impostaErrore(tmpControlli, String.format(MESS_ERR_FORMATO_PARTE,
			prAnno.getNomeParte(), prAnno.getDescValoriAccettabili()));
		return tmpControlli;
	    }
	}

	// test per vedere se la parte coincide con Anno chiave
	if (prAnno.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_ANNO_PARTE, prAnno.getNomeParte()));
	    return tmpControlli;
	}

	// padding
	Long numPad = (prAnno.getMaxLen() != -1 ? prAnno.getMaxLen() : PAD_NUM_12);
	sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));
	if (prAnno.getSeparatore() != null && prAnno.getSeparatore().length() > 0) {
	    sbKeyOrd.append(prAnno.getSeparatore());
	}
	//
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private RispostaControlli verificaTipoRomano(CSChiave chiave, StringBuilder sbKeyOrd,
	    String parte, ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = new RispostaControlli();
	RomanConverter tmpConverter = new RomanConverter();
	tmpConverter.setStrict(false);
	Long num = null;

	try {
	    // viene trim-ata perché potrebbe avere spazi di padding
	    tmpConverter.fromRomanValue(parte.trim());
	    num = Long.valueOf(tmpConverter.toInt());
	    tmpControlli.setrLong(num);
	} catch (NumberFormatException e) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_FORMATO_PARTE, prAnno.getNomeParte(), e.getMessage()));
	    return tmpControlli;
	}

	// test per vedere se la parte coincide con Anno chiave
	if (prAnno.isMatchAnnoChiave() && !Objects.equals(num, chiave.getAnno())) {
	    this.impostaErrore(tmpControlli,
		    String.format(MESS_ERR_ANNO_PARTE, prAnno.getNomeParte()));
	    return tmpControlli;
	}

	// padding, come il numerico, ma il pad standard è di 4 zeri,
	// come nellla vecchia logica di calcolo.
	Long numPad = (prAnno.getMaxLen() != -1 ? prAnno.getMaxLen() : PAD_NUM_4);
	sbKeyOrd.append(StringUtils.leftPad(num.toString(), numPad.intValue(), "0"));
	if (prAnno.getSeparatore() != null && prAnno.getSeparatore().length() > 0) {
	    sbKeyOrd.append(prAnno.getSeparatore());
	}
	//
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private RispostaControlli verificaTipoGenerico(StringBuilder sbKeyOrd, String parte,
	    ConfigRegAnno.ParteRegAnno prAnno) {
	RispostaControlli tmpControlli = new RispostaControlli();
	tmpControlli.setrLong(-1);
	// ovviamente non ci sono numeri da estrarre
	// e neppure controlli da svolgere: la regexp globale ha
	// già filtrato i valori non ammessi

	// padding, valutato sempre come se fosse una stringa
	if (prAnno.getMaxLen() != -1) {
	    sbKeyOrd.append(
		    StringUtils.rightPad(parte, ((Long) prAnno.getMaxLen()).intValue(), " "));
	} else if (prAnno.getTiPadding() == ConfigRegAnno.TipiPadding.RIEMPI_0_A_SX_LESS12) {
	    sbKeyOrd.append(StringUtils.leftPad(parte, PAD_NUM_12, "0"));
	} else {
	    sbKeyOrd.append(parte);
	}
	if (prAnno.getSeparatore() != null && prAnno.getSeparatore().length() > 0) {
	    sbKeyOrd.append(prAnno.getSeparatore());
	}
	//
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private RispostaControlli produciChiaveOrd(CSChiave chiave, StringBuilder sbKeyOrd) {
	RispostaControlli tmpControlli = new RispostaControlli();
	String tmpChiaveOrd = chiave.getTipoRegistro() + "-" + chiave.getAnno() + "-"
		+ sbKeyOrd.toString();
	if (tmpChiaveOrd.length() > KeySizeUtility.MAX_LEN_CHIAVEORD && chiave.getTipoRegistro()
		.length() > KeySizeUtility.MAX_LEN_REGISTRO_IN_CHIAVEORD) {
	    tmpChiaveOrd = chiave.getTipoRegistro().substring(0,
		    KeySizeUtility.MAX_LEN_REGISTRO_IN_CHIAVEORD) + "-" + chiave.getAnno() + "-"
		    + sbKeyOrd.toString();
	}
	if (tmpChiaveOrd.length() > KeySizeUtility.MAX_LEN_CHIAVEORD) {
	    // se il risultato è troppo lungo, lo tronco e rendo errore
	    // oltre alla chiave calcolata e troncata che potrebbe servire comunque
	    tmpChiaveOrd = tmpChiaveOrd.substring(0, KeySizeUtility.MAX_LEN_CHIAVEORD);
	    tmpControlli.setrString(tmpChiaveOrd);
	    this.impostaErrore(tmpControlli, MESS_ERR_CHIAVECALC_TOOLONG);
	    return tmpControlli;
	}

	tmpControlli.setrString(tmpChiaveOrd);
	tmpControlli.setrBoolean(true);
	return tmpControlli;
    }

    private void impostaErrore(RispostaControlli rc, String causa) {
	rc.setrBoolean(false);
	if (descKeyUdVersata == null) {
	    if (causa.equals(MESS_ERR_CHIAVECALC_TOOLONG)) {
		rc.setCodErr(MessaggiWSBundle.UD_007_002);
		rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_007_002,
			descKeyDaVerificare));

	    } else {
		rc.setCodErr(MessaggiWSBundle.UD_007_001);
		rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_007_001,
			descKeyDaVerificare, causa));
	    }
	} else {
	    rc.setCodErr(MessaggiWSBundle.UD_004_005);
	    rc.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_004_005, descKeyUdVersata,
		    descKeyDaVerificare, causa));

	}
    }

    //
    public class KeyOrdResult {

	private String keyOrdCalcolata;
	private Long progressivoCalcolato;

	public String getKeyOrdCalcolata() {
	    return keyOrdCalcolata;
	}

	public void setKeyOrdCalcolata(String keyOrdCalcolata) {
	    this.keyOrdCalcolata = keyOrdCalcolata;
	}

	public Long getProgressivoCalcolato() {
	    return progressivoCalcolato;
	}

	public void setProgressivoCalcolato(Long progressivoCalcolato) {
	    this.progressivoCalcolato = progressivoCalcolato;
	}

    }
}
