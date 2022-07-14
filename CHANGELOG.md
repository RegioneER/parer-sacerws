
## 3.2.0 (31-05-2022)

### Novità: 2
- [#27249](https://parermine.regione.emilia-romagna.it/issues/27249) Aggiornamento libreria DSS 5.9
- [#27048](https://parermine.regione.emilia-romagna.it/issues/27048) Ottimizzazione job di Creazione elenchi di versamento - Producer coda IN_ATTESA_SCHED

## 3.1.6.1 (24-05-2022)

### Bugfix: 1
- [#27272](https://parermine.regione.emilia-romagna.it/issues/27272) Correzione procedura di gestione errori della verifica firma EIDAS nei casi di firme non valide che non contengono certificati di firma

## 3.1.6 (10-05-2022)

### Bugfix: 3
- [#27003](https://parermine.regione.emilia-romagna.it/issues/27003) Correzione dell'XSD WSRequestUnico.xsd delle specifiche di versamento UD 1.4
- [#26836](https://parermine.regione.emilia-romagna.it/issues/26836) Correzione popolamento soggetto della CA
- [#26757](https://parermine.regione.emilia-romagna.it/issues/26757) Correzione della risposta in caso di controllo certificato non consistente

### Novità: 1
- [#26624](https://parermine.regione.emilia-romagna.it/issues/26624) Modifica gestione dei log level sui processi che impattano la verifica firma

## 3.1.5 (27-04-2022)

### Novità: 1
- [#26508](https://parermine.regione.emilia-romagna.it/issues/26508) Gestione nuove codifiche profili per versamento fascicolo

## 3.1.4 (24-03-2022)

### Bugfix: 1
- [#26746](https://parermine.regione.emilia-romagna.it/issues/26746) Errore nell'xsd di validazione della response

## 3.1.3 (31-01-2022)

### Novità: 1
- [#26662](https://parermine.regione.emilia-romagna.it/issues/26662) aggiornamento librerie obsolete primo quadrimestre 2021

## 3.1.2 (13-01-2022)

### Bugfix: 1
- [#26579](https://parermine.regione.emilia-romagna.it/issues/26579) Correzione salvataggio nuovo profilo ud
## 3.1.1 (30-12-2021)

### Bugfix: 1
- [#26515](https://parermine.regione.emilia-romagna.it/issues/26515) Correzione rapporto di versamento indirizzo ip sistema versante

## 3.1.0 (20-12-2021)

### Novità: 1
- [#26169](https://parermine.regione.emilia-romagna.it/issues/26169) Introduzione gestione del nuovo profilo normativo

## 3.0.9 (20-12-2021)

### Bugfix: 2
- [#26453](https://parermine.regione.emilia-romagna.it/issues/26453) Gestione errore bloccante per formato file standard non presente
- [#26452](https://parermine.regione.emilia-romagna.it/issues/26452) Correzione gestione campo note del profilo generale su versamento fascicoli

## 3.0.8 (02-12-2021)

### Bugfix: 2
- [#26393](https://parermine.regione.emilia-romagna.it/issues/26393) Gestione errore codificato quando formato file standard non presente
- [#26369](https://parermine.regione.emilia-romagna.it/issues/26369) Correzione gestione date come parametro di query

## 3.0.7 (02-12-2021)

### Bugfix: 1
- [#26355](https://parermine.regione.emilia-romagna.it/issues/26355) Correzione errore controlli formali 

### Novità: 1
- [#26266](https://parermine.regione.emilia-romagna.it/issues/26266) Gestione delle casistiche di eccezioni generiche su recepimento delle riposta da servizio verifica firma

## 3.0.6.1 (26-11-2021)

### Bugfix: 1
- [#26323](https://parermine.regione.emilia-romagna.it/issues/26323) Correzione gestione date come parametro di query 
## 3.0.6

### EVO: 2
- [#25667](https://parermine.regione.emilia-romagna.it/issues/25667) Implementazione versione 1.5 dei servizi di versamento sincrono
- [#21932](https://parermine.regione.emilia-romagna.it/issues/21932) Completamento aggiornamento verifica firme 

### Bugfix: 1
- [#26013](https://parermine.regione.emilia-romagna.it/issues/26013) Verifica su riferimento temporale

### Novità: 6
- [#26270](https://parermine.regione.emilia-romagna.it/issues/26270) Gestire più di una marca temporale su un singolo componente
- [#25288](https://parermine.regione.emilia-romagna.it/issues/25288) Calcolo nuovo URN relativo al SIP nel versamento fascicolo
- [#24644](https://parermine.regione.emilia-romagna.it/issues/24644) Gestione errore codificato quando formato file standard non presente
- [#24557](https://parermine.regione.emilia-romagna.it/issues/24557) Aggiunta campo Utente nella versione 1.5 dei ws di versamento
- [#22406](https://parermine.regione.emilia-romagna.it/issues/22406) Gestione degli ocsp in fase di verifica firme
- [#21971](https://parermine.regione.emilia-romagna.it/issues/21971) Inserimento nuovi tag nell'Esito versamento

## 3.0.5 (19-11-2021)

### Bugfix: 1
- [#26281](https://parermine.regione.emilia-romagna.it/issues/26281) Disattivato aggiornamento di UD in stato IN VOLUME DI CONSERVAZIONE

## 3.0.4 (15-11-2021)

### Novità: 1
- [#24651](https://parermine.regione.emilia-romagna.it/issues/24651) Tracciamento nei log del parametro SimulaSalvataggioDatiInDB a true

## 3.0.3 (05-11-2021)

### Bugfix: 1
- [#26201](https://parermine.regione.emilia-romagna.it/issues/26201) correzione della procedura di versamento nel caso di  N componenti di cui alcuni firmati ed altri no

## 3.0.1.1 (26-10-2021)

### Bugfix: 1
- [#26119](https://parermine.regione.emilia-romagna.it/issues/26119) correzione della procedura di versamento nel caso di  N componenti di cui alcuni firmati ed altri no
## 3.0.2 (21-10-2021)

### Bugfix: 1
- [#25961](https://parermine.regione.emilia-romagna.it/issues/25961) Modificare urn Aggiornamento metadati UD

### Novità: 1
- [#25347](https://parermine.regione.emilia-romagna.it/issues/25347) Generazione report di verifica firma su object storage

## 3.0.1

### Bugfix: 3
- [#26030](https://parermine.regione.emilia-romagna.it/issues/26030) Correzione mancata presenza del file del certificato della CA e/o del firmatario e/o della CRL in fase di verifica firma del componente
- [#26016](https://parermine.regione.emilia-romagna.it/issues/26016) Correzione della gestione degli esiti in mancanza di  un formato standard
- [#23623](https://parermine.regione.emilia-romagna.it/issues/23623) Gestione disabilitazione verifica firma nella risposta 1.4

### Novità: 1
- [#25642](https://parermine.regione.emilia-romagna.it/issues/25642) Ottimizzazione meccanismo di retry 

## 3.0.0 (08-10-2021)

### Novità: 1
- [#26017](https://parermine.regione.emilia-romagna.it/issues/26017) unificazione contesto applicativo di sacerws

## 2.3.6-EIDAS-1.8 (30-09-2021)

### Bugfix: 4
- [#25213](https://parermine.regione.emilia-romagna.it/issues/25213) Correzione esito verifica in caso di utilizzo della data di firma come riferimento temporale
- [#23714](https://parermine.regione.emilia-romagna.it/issues/23714) Errata valorizzazione del riferimento temporale usato nella response al versamento
- [#23701](https://parermine.regione.emilia-romagna.it/issues/23701) Formato xades non conforme restituisce come formato asics
- [#23700](https://parermine.regione.emilia-romagna.it/issues/23700) Esiti non attesi per verifica su CRL e verifica con controllo crittografico disabilitato

## 2.3.6-EIDAS-1.7 (10-08-2021)

### Bugfix: 1
- [#25436](https://parermine.regione.emilia-romagna.it/issues/25436) Correzione dipendenze librerie JAXB e JAVAX ACTIVATION

## 2.3.6 (10-08-2021)

### Novità: 1
- [#23176](https://parermine.regione.emilia-romagna.it/issues/23176) Calcolo nuovi URN relativi ai SIP

## 2.3.5-EIDAS.1.6 (07-07-2021)

### Novità: 1
- [#25312](https://parermine.regione.emilia-romagna.it/issues/25312) Aggiunta parametro per abilitare la generazione dei report verifica firma 

## 2.3.5-EIDAS.1.5 (18-05-2021)

### Bugfix: 3
- [#24880](https://parermine.regione.emilia-romagna.it/issues/24880) Correzione esito verifica in caso di marca temporale
- [#24362](https://parermine.regione.emilia-romagna.it/issues/24362) Correzione valore persistito legato alla URL della CRL
- [#24322](https://parermine.regione.emilia-romagna.it/issues/24322) Correzione errore gestione esito WARNING con FL_ABILITA_CONTR_NON_FIRMATI = true

## 2.3.5 (18-05-2021)

### Bugfix: 5
- [#24842](https://parermine.regione.emilia-romagna.it/issues/24842) Correzione naming convention del componente/sottocomponente per versamenti di tipo FILE
- [#24446](https://parermine.regione.emilia-romagna.it/issues/24446) Correzione gestione dell'errore relativo all'ordine presentazione di un sottocomponente.
- [#24089](https://parermine.regione.emilia-romagna.it/issues/24089) Generazione javadoc
- [#22948](https://parermine.regione.emilia-romagna.it/issues/22948) Aggiornamento di UD in stato IN VOLUME DI CONSERVAZIONE
- [#22947](https://parermine.regione.emilia-romagna.it/issues/22947) Aggiornamento di UD in stato AIP FIRMATO

### Novità: 2
- [#22978](https://parermine.regione.emilia-romagna.it/issues/22978) Aggiunta di ulteriori  informazioni nel rapporto di versamento.
- [#15448](https://parermine.regione.emilia-romagna.it/issues/15448) Aggiunta memorizzazione dell'indirizzo IP e del sistema versante dell'utente versatore
