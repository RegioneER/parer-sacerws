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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.eng.parer.ws.versFascicoli.utils.KeyOrdFascUtility;

/**
 *
 * @author sinatti_s
 */
public class ConfigNumFasc {

    private final List<ParteNumero> parti;
    private boolean generico;
    private String regExpCalc;
    private String descRegExp;
    long idAaNumeroFasc;

    public ConfigNumFasc(long idAaNumeroFasc) {
	this.idAaNumeroFasc = idAaNumeroFasc;
	parti = new ArrayList<>(0);
    }

    public ParteNumero aggiungiParte() {
	ParteNumero pra = new ParteNumero();
	parti.add(pra);
	return pra;
    }

    public void ElaboraParti() {
	StringBuilder tmpSb = new StringBuilder();
	StringBuilder tmpSbDesc = new StringBuilder();

	tmpSb.append("^"); // inizio stringa

	for (ParteNumero pra : parti) {
	    if (pra.minLen > 0 && pra.minLen == pra.maxLen) {
		// la parte è lunga esattamente N char, probabilmente non ha un separatore
		pra.quantificatore = String.format("{%d}", pra.minLen);
		pra.descQuantificatore = String.format("%d cifre", pra.minLen);
		pra.setLunghezzaFissa(true);
	    } else if (pra.maxLen != -1) {
		// la parte ha una lunghezza minima ed una lunghezza massima
		pra.quantificatore = String.format("{%d,%d}", pra.minLen, pra.maxLen);
		pra.descQuantificatore = String.format("da %d a %d cifre", pra.minLen, pra.maxLen);
	    } else {
		// la parte ha una lunghezza minima (potrebbe anche essere 0)
		pra.quantificatore = String.format("{%d,}", pra.minLen);
		pra.descQuantificatore = String.format("%d o più cifre", pra.minLen);
	    }

	    String tmpModificatore = "";

	    switch (pra.getTipoCalcolo()) {
	    case GENERICO:
		this.generico = true;
		// stringa generica
		// può essere usato se è definita come unica parte.
		// permette di versare quasi tutto
		tmpSb.append(String.format("(.%s)", pra.quantificatore));
		break;
	    case ALFABETICO:
		// stringa alfabetica
		// (+ spazio se non è usato come separatore o se è a lunghezza fissa)
		if (pra.isLunghezzaFissa() || !" ".equals(pra.getSeparatore())) {
		    tmpSb.append(String.format("([a-zA-Z ]%s)", pra.quantificatore));
		    tmpModificatore = " (oltre al carattere {SPAZIO})";
		} else {
		    tmpSb.append(String.format("([a-zA-Z]%s)", pra.quantificatore));
		}
		break;
	    case ALFANUMERICO:
		// stringa alfanumerica
		// (+ spazio se non è usato come separatore o se è a lunghezza fissa)
		if (pra.isLunghezzaFissa() || !" ".equals(pra.getSeparatore())) {
		    tmpSb.append(String.format("([a-zA-Z0-9 ]%s)", pra.quantificatore));
		    tmpModificatore = " (oltre al carattere {SPAZIO})";
		} else {
		    tmpSb.append(String.format("([a-zA-Z0-9]%s)", pra.quantificatore));
		}
		break;
	    case NUMERICO:
		// stringa numerica
		tmpSb.append(String.format("([0-9]%s)", pra.quantificatore));
		break;
	    case NUMERI_ROMANI:
		// stringa di numeri romani + spazio,
		// (se non è usato come separatore o se è a lunghezza fissa) per il padding (!)
		if (pra.isLunghezzaFissa() || !" ".equals(pra.getSeparatore())) {
		    tmpSb.append(String.format("([ivxlcdmIVXLCDM ]%s)", pra.quantificatore));
		    tmpModificatore = " (oltre al carattere {SPAZIO} usato come riempimento)";
		} else {
		    tmpSb.append(String.format("([ivxlcdmIVXLCDM]%s)", pra.quantificatore));
		}

		break;
	    case PARTE_GENERICO:
		// stringa generica
		// Se è a lunghezza fissa oppure se non è definito
		// il carattere di separazione (è probabilmente l'ultima parte)
		// ricerco un contenuto generico, altrimenti ricerco
		// un contenuto generico a meno del separatore
		if (pra.isLunghezzaFissa() || pra.getSeparatore() == null
			|| pra.getSeparatore().length() == 0) {
		    tmpSb.append(String.format("(.%s)", pra.quantificatore));
		} else {
		    // altrimenti il contenuto è generico a meno del carattere di separazione
		    // e del carattere di fine linea
		    tmpSb.append(
			    String.format("([^\\n%s]%s)", pra.getSeparatore(), pra.quantificatore));
		    tmpModificatore = String.format(" (tranne il carattere \"%s\")",
			    pra.getSeparatore());
		}
		break;
	    case NUMERICO_GENERICO:
		// stringa numerica, compresa tra due stringhe generiche,
		// eventualmente di lunghezza 0.
		// NOTA: viene gestito come una parte di tipo GENERICO, in seguito
		// verrà verificato se nel _group_ trovato è presente una sequenza numerica.
		//
		// Se è a lunghezza fissa oppure se non è definito
		// il carattere di separazione (è probabilmente l'ultima parte)
		// ricerco un contenuto generico, altrimenti ricerco
		// un contenuto generico a meno del separatore
		if (pra.isLunghezzaFissa() || pra.getSeparatore() == null
			|| pra.getSeparatore().length() == 0) {
		    tmpSb.append(String.format("(.%s)", pra.quantificatore));
		} else {
		    // altrimenti il contenuto è generico a meno del carattere di separazione
		    // e del carattere di fine linea
		    tmpSb.append(
			    String.format("([^\\n%s]%s)", pra.getSeparatore(), pra.quantificatore));
		    tmpModificatore = String.format(" (tranne il carattere \"%s\")",
			    pra.getSeparatore());
		}
		break;
	    }
	    tmpSbDesc.append(pra.getTipoCalcolo().descrivi());
	    tmpSbDesc.append(tmpModificatore);
	    tmpSbDesc.append(String.format(" (%s)", pra.descQuantificatore));

	    if (pra.getSeparatore() != null && pra.getSeparatore().length() > 0) {
		// aggiungo il separatore, preceduto da un escape,
		// per gestire separatori che sono anche
		// quantificatori o caratteri speciali (+ . * $ ^)
		tmpSb.append("\\").append(pra.separatore);

		// Se il separatiore è uno spazio, evidenzio la cosa nella descrizione
		// del pattern atteso da usare per i messaggi di errore
		tmpSbDesc.append(" ".equals(pra.separatore) ? "{SPAZIO}" : pra.separatore);
	    }
	}
	tmpSb.append("$"); // fine stringa

	regExpCalc = tmpSb.toString();
	descRegExp = tmpSbDesc.toString();

	if (this.generico && parti.size() > 1) {
	    throw new RuntimeException("Per il tipo GENERICO è ammessa una sola parte!!");
	}

    }

    public static void impostaValoriAccettabili(ParteNumero parte, String valori) {
	parte.listaValoriAccettabili = null;
	parte.minValAccettabile = null;
	parte.maxValAccettabile = null;

	// formato della stringa che descrive un range di valori
	// esempio: <10>-<15>
	// ^\s*\<\s*<(\d+)\s*\>\s*-\s*\<\s*(\d+)\s*\>\s*$
	String regexRange = "^\\s*\\<\\s*(\\d+)\\s*\\>\\s*-\\s*\\<\\s*(\\d+)\\s*\\>\\s*$";

	//
	if (valori != null && valori.length() > 0) {
	    parte.descValoriAccettabili = valori;
	    // Create a Pattern object
	    Pattern r = Pattern.compile(regexRange);
	    // Now create matcher object.
	    Matcher m = r.matcher(valori);
	    if (m.find()) {
		parte.minValAccettabile = Long.parseLong(m.group(1));
		parte.maxValAccettabile = Long.parseLong(m.group(2));
	    } else {
		// se non coincide il range, cerca una stringa del tipo
		// val1,val2,val3,val4
		parte.listaValoriAccettabili = valori.split(",");
	    }
	}
    }

    //
    public List<ParteNumero> getParti() {
	return parti;
    }

    public String getRegExpCalc() {
	return regExpCalc;
    }

    public String getDescRegExp() {
	return descRegExp;
    }

    public long getIdAaNumeroFasc() {
	return idAaNumeroFasc;
    }

    public enum TipiPadding {
	FORMAT_CLASSIF, NO_RIEMPI, RIEMPI_0_A_SX, RIEMPI_SPAZIO_DX, NESSUNO
    }

    public class ParteNumero {

	private String nomeParte;
	private int numParte;
	private KeyOrdFascUtility.TipiCalcolo tipoCalcolo;
	private long minLen;
	private long maxLen;
	private boolean lunghezzaFissa;
	private TipiPadding tiPadding = TipiPadding.NESSUNO;
	private String separatore;
	private boolean matchAnnoChiave;
	private boolean matchClassif;
	private boolean usaComeProgressivo;
	private boolean usaComeSottoProgressivo;
	//
	private String descValoriAccettabili;
	private String[] listaValoriAccettabili;
	private Long minValAccettabile;
	private Long maxValAccettabile;
	//
	private String quantificatore;
	private String descQuantificatore;
	//
	private long niPadParteClassif;
	private String separatoreClassif;

	public String getNomeParte() {
	    return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
	    this.nomeParte = nomeParte;
	}

	public int getNumParte() {
	    return numParte;
	}

	public void setNumParte(int numParte) {
	    this.numParte = numParte;
	}

	public KeyOrdFascUtility.TipiCalcolo getTipoCalcolo() {
	    return tipoCalcolo;
	}

	public void setTipoCalcolo(KeyOrdFascUtility.TipiCalcolo tipoCalcolo) {
	    this.tipoCalcolo = tipoCalcolo;
	}

	public long getMinLen() {
	    return minLen;
	}

	public void setMinLen(long minLen) {
	    this.minLen = minLen;
	}

	public long getMaxLen() {
	    return maxLen;
	}

	public void setMaxLen(long maxLen) {
	    this.maxLen = maxLen;
	}

	public boolean isLunghezzaFissa() {
	    return lunghezzaFissa;
	}

	public void setLunghezzaFissa(boolean lunghezzaFissa) {
	    this.lunghezzaFissa = lunghezzaFissa;
	}

	public TipiPadding getTiPadding() {
	    return tiPadding;
	}

	public void setTiPadding(TipiPadding tiPadding) {
	    this.tiPadding = tiPadding;
	}

	public String getSeparatore() {
	    return separatore;
	}

	public void setSeparatore(String separatore) {
	    this.separatore = separatore;
	}

	public boolean isMatchAnnoChiave() {
	    return matchAnnoChiave;
	}

	public void setMatchAnnoChiave(boolean matchAnnoChiave) {
	    this.matchAnnoChiave = matchAnnoChiave;
	}

	public boolean isUsaComeProgressivo() {
	    return usaComeProgressivo;
	}

	public void setUsaComeProgressivo(boolean usaComeProgressivo) {
	    this.usaComeProgressivo = usaComeProgressivo;
	}

	public boolean isUsaComeSottoProgressivo() {
	    return usaComeSottoProgressivo;
	}

	public void setUsaComeSottoProgressivo(boolean usaComeSottoProgressivo) {
	    this.usaComeSottoProgressivo = usaComeSottoProgressivo;
	}

	public boolean isMatchClassif() {
	    return matchClassif;
	}

	public void setMatchClassif(boolean matchClassif) {
	    this.matchClassif = matchClassif;
	}

	public String getDescValoriAccettabili() {
	    return descValoriAccettabili;
	}

	public void setDescValoriAccettabili(String descValoriAccettabili) {
	    this.descValoriAccettabili = descValoriAccettabili;
	}

	public String[] getListaValoriAccettabili() {
	    return listaValoriAccettabili;
	}

	public void setListaValoriAccettabili(String[] listaValoriAccettabili) {
	    this.listaValoriAccettabili = listaValoriAccettabili;
	}

	public Long getMinValAccettabile() {
	    return minValAccettabile;
	}

	public void setMinValAccettabile(Long minValAccettabile) {
	    this.minValAccettabile = minValAccettabile;
	}

	public Long getMaxValAccettabile() {
	    return maxValAccettabile;
	}

	public void setMaxValAccettabile(Long maxValAccettabile) {
	    this.maxValAccettabile = maxValAccettabile;
	}

	public String getQuantificatore() {
	    return quantificatore;
	}

	public void setQuantificatore(String quantificatore) {
	    this.quantificatore = quantificatore;
	}

	public String getDescQuantificatore() {
	    return descQuantificatore;
	}

	public void setDescQuantificatore(String descQuantificatore) {
	    this.descQuantificatore = descQuantificatore;
	}

	public long getNiPadParteClassif() {
	    return niPadParteClassif;
	}

	public void setNiPadParteClassif(long niPadParteClassif) {
	    this.niPadParteClassif = niPadParteClassif;
	}

	public String getSeparatoreClassif() {
	    return separatoreClassif;
	}

	public void setSeparatoreClassif(String separatoreClassif) {
	    this.separatoreClassif = separatoreClassif;
	}

    }

    public enum TiParte {
	ANNO("ANNO"), PROGR("PROGR_FASC"), CLASSIF("CLASSIF"), PROGSUB("PROGR_SUB_FASC");

	private String descrizione;

	TiParte(String descrizione) {
	    this.descrizione = descrizione;
	}

	public String descrizione() {
	    return this.descrizione;
	}
    }

    public class ConfigNumLivTitol {

	String sep;
	String fmt;

	public String getSep() {
	    return sep;
	}

	public void setSep(String sep) {
	    this.sep = sep;
	}

	public String getFmt() {
	    return fmt;
	}

	public void setFmt(String fmt) {
	    this.fmt = fmt;
	}

    }

}
