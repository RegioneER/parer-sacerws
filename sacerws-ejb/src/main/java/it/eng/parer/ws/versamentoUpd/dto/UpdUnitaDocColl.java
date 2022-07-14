/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import it.eng.parer.ws.versamento.dto.UnitaDocColl;
import it.eng.parer.ws.xml.versUpdReq.ChiaveType;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sinatti_s
 */
public class UpdUnitaDocColl extends UnitaDocColl implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5321793990793683759L;

    public final static int MAX_LEN_DESCRIZIONE = 254;

    private ChiaveType aggChiave;
    /*
     * questa variabile contiene l'insieme in ordine di inserimento di tutte le descrizioni dei collegamenti che puntano
     * ad una stessa ud. dal momento che l'implementazione di LinkedHashSet è un set, le descrizioni duplicate vengono
     * automaticamente ignorate
     */
    private Set<String> descrizioni = new LinkedHashSet<>();

    //
    public void aggiungiDescrizioneUnivoca(String descrizione) {
        descrizioni.add(descrizione);
    }

    public String generaDescrizione() {
        // la descrizione del collegamento è la somma delle descrizioni uniche
        // di tutti i riferimenti alla UD oggetto del collegamento.
        return StringUtils.join(descrizioni.toArray(), "-");
    }

    public ChiaveType getAggChiave() {
        return aggChiave;
    }

    public void setAggChiave(ChiaveType aggChiave) {
        this.aggChiave = aggChiave;
    }

}
