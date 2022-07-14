package it.eng.parer.ws.versamentoMM.dto;

import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.xml.versReqMultiMedia.ComponenteType;

/**
 *
 * @author Fioravanti_F
 */
public class ComponenteMM {

    // tag <id>
    private String id;
    private ComponenteVers rifComponenteVers;
    // private FileBinario rifFileBinario;
    private ComponenteType myComponenteMM;
    private boolean forzaFirmeFormati = false;
    private boolean forzaHash = false;
    private long idFormatoFileCalc;
    private byte[] hashForzato;

    public boolean isForzaFirmeFormati() {
        return forzaFirmeFormati;
    }

    public void setForzaFirmeFormati(boolean forzaFirmeFormati) {
        this.forzaFirmeFormati = forzaFirmeFormati;
    }

    public boolean isForzaHash() {
        return forzaHash;
    }

    public void setForzaHash(boolean forzaHash) {
        this.forzaHash = forzaHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getIdFormatoFileCalc() {
        return idFormatoFileCalc;
    }

    public void setIdFormatoFileCalc(long idFormatoFileCalc) {
        this.idFormatoFileCalc = idFormatoFileCalc;
    }

    public ComponenteType getMyComponenteMM() {
        return myComponenteMM;
    }

    public void setMyComponenteMM(ComponenteType myComponenteMM) {
        this.myComponenteMM = myComponenteMM;
    }

    public ComponenteVers getRifComponenteVers() {
        return rifComponenteVers;
    }

    public void setRifComponenteVers(ComponenteVers rifComponenteVers) {
        this.rifComponenteVers = rifComponenteVers;
    }

    public byte[] getHashForzato() {
        return hashForzato;
    }

    public void setHashForzato(byte[] hashForzato) {
        this.hashForzato = hashForzato;
    }
}
